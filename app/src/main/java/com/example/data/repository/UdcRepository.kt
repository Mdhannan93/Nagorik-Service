package com.example.data.repository

import android.util.Log
import com.example.data.database.ComputerTrainingDao
import com.example.data.database.NoticeDao
import com.example.data.database.ServiceApplicationDao
import com.example.data.firebase.FirebaseManager
import com.example.data.model.ComputerTraining
import com.example.data.model.Notice
import com.example.data.model.ServiceApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.android.gms.tasks.Task

import com.example.data.database.UserAccountDao
import com.example.data.model.UserAccount

class UdcRepository(
    private val serviceDao: ServiceApplicationDao,
    private val noticeDao: NoticeDao,
    private val trainingDao: ComputerTrainingDao,
    private val userDao: UserAccountDao
) {
    private val TAG = "UdcRepository"

    // --- User Accounts ---
    suspend fun getUserByEmail(email: String): UserAccount? = userDao.getUserByEmail(email)
    suspend fun insertUser(user: UserAccount) = userDao.insertUser(user)

    // --- Service Applications ---
    fun getAllApplications(): Flow<List<ServiceApplication>> = serviceDao.getAllApplications()

    fun getApplicationsByCitizen(email: String): Flow<List<ServiceApplication>> =
        serviceDao.getApplicationsByCitizen(email)

    suspend fun getApplicationById(id: Int): ServiceApplication? = serviceDao.getApplicationById(id)

    suspend fun insertApplication(application: ServiceApplication): Long {
        val id = serviceDao.insertApplication(application)
        val addedApplication = application.copy(id = id.toInt())
        syncApplicationToFirestore(addedApplication)
        return id
    }

    suspend fun updateApplication(application: ServiceApplication) {
        serviceDao.updateApplication(application)
        syncApplicationToFirestore(application)
    }

    suspend fun deleteApplication(id: Int) {
        serviceDao.deleteApplication(id)
        deleteApplicationFromFirestore(id)
    }

    // --- Notices ---
    fun getAllNotices(): Flow<List<Notice>> = noticeDao.getAllNotices()

    suspend fun insertNotice(notice: Notice): Long {
        val id = noticeDao.insertNotice(notice)
        val addedNotice = notice.copy(id = id.toInt())
        syncNoticeToFirestore(addedNotice)
        return id
    }

    suspend fun deleteNotice(id: Int) {
        noticeDao.deleteNotice(id)
        deleteNoticeFromFirestore(id)
    }

    // --- Computer Trainings ---
    fun getAllRegistrations(): Flow<List<ComputerTraining>> = trainingDao.getAllRegistrations()

    fun getRegistrationsByTrainee(email: String): Flow<List<ComputerTraining>> =
        trainingDao.getRegistrationsByTrainee(email)

    suspend fun insertRegistration(registration: ComputerTraining): Long {
        val id = trainingDao.insertRegistration(registration)
        val addedReg = registration.copy(id = id.toInt())
        syncRegistrationToFirestore(addedReg)
        return id
    }

    suspend fun updateRegistration(registration: ComputerTraining) {
        trainingDao.updateRegistration(registration)
        syncRegistrationToFirestore(registration)
    }

    // --- Firestore Syncing Helpers ---
    private suspend fun syncApplicationToFirestore(app: ServiceApplication) {
        val firestore = FirebaseManager.firestore ?: return
        try {
            firestore.collection("applications")
                .document(app.id.toString())
                .set(app)
                .awaitSafe()
            Log.d(TAG, "Application successfully synced to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Firestore application sync failure: ${e.message}")
        }
    }

    private suspend fun deleteApplicationFromFirestore(id: Int) {
        val firestore = FirebaseManager.firestore ?: return
        try {
            firestore.collection("applications")
                .document(id.toString())
                .delete()
                .awaitSafe()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore application delete failure: ${e.message}")
        }
    }

    private suspend fun syncNoticeToFirestore(notice: Notice) {
        val firestore = FirebaseManager.firestore ?: return
        try {
            firestore.collection("notices")
                .document(notice.id.toString())
                .set(notice)
                .awaitSafe()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore notice sync failure: ${e.message}")
        }
    }

    private suspend fun deleteNoticeFromFirestore(id: Int) {
        val firestore = FirebaseManager.firestore ?: return
        try {
            firestore.collection("notices")
                .document(id.toString())
                .delete()
                .awaitSafe()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore notice delete failure: ${e.message}")
        }
    }

    private suspend fun syncRegistrationToFirestore(reg: ComputerTraining) {
        val firestore = FirebaseManager.firestore ?: return
        try {
            firestore.collection("registrations")
                .document(reg.id.toString())
                .set(reg)
                .awaitSafe()
        } catch (e: Exception) {
            Log.e(TAG, "Firestore registration sync failure: ${e.message}")
        }
    }
}

// Custom Extension to wait on Play Services / Firebase Tasks without library conflicts
suspend fun <T> Task<T>.awaitSafe(): T? = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: RuntimeException("Task failed under Play Services"))
        }
    }
}
