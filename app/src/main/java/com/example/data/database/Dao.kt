package com.example.data.database

import androidx.room.*
import com.example.data.model.ComputerTraining
import com.example.data.model.Notice
import com.example.data.model.ServiceApplication
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceApplicationDao {
    @Query("SELECT * FROM service_applications ORDER BY submissionTimestamp DESC")
    fun getAllApplications(): Flow<List<ServiceApplication>>

    @Query("SELECT * FROM service_applications WHERE citizenEmail = :email ORDER BY submissionTimestamp DESC")
    fun getApplicationsByCitizen(email: String): Flow<List<ServiceApplication>>

    @Query("SELECT * FROM service_applications WHERE id = :id")
    suspend fun getApplicationById(id: Int): ServiceApplication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ServiceApplication): Long

    @Update
    suspend fun updateApplication(application: ServiceApplication)

    @Query("DELETE FROM service_applications WHERE id = :id")
    suspend fun deleteApplication(id: Int)
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY date DESC")
    fun getAllNotices(): Flow<List<Notice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: Notice): Long

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: Int)
}

@Dao
interface ComputerTrainingDao {
    @Query("SELECT * FROM computer_trainings ORDER BY registrationTimestamp DESC")
    fun getAllRegistrations(): Flow<List<ComputerTraining>>

    @Query("SELECT * FROM computer_trainings WHERE traineeEmail = :email ORDER BY registrationTimestamp DESC")
    fun getRegistrationsByTrainee(email: String): Flow<List<ComputerTraining>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistration(registration: ComputerTraining): Long

    @Update
    suspend fun updateRegistration(registration: ComputerTraining)
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE email = :email")
    suspend fun getUserByEmail(email: String): com.example.data.model.UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: com.example.data.model.UserAccount)
}

