package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.firebase.FirebaseManager
import com.example.data.gemini.GeminiChatRepository
import com.example.data.gemini.GeminiChatResponse
import com.example.data.model.ComputerTraining
import com.example.data.model.Notice
import com.example.data.model.ServiceApplication
import com.example.data.model.UserData
import com.example.data.repository.UdcRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(
    val sender: String, // "citizen", "assistant"
    val message: String,
    val searchSources: List<Pair<String, String>> = emptyList(),
    val searchQueries: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface AuthState {
    object Loading : AuthState
    object LoggedOut : AuthState
    data class LoggedIn(val user: UserData) : AuthState
}

class UDCViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "UDCViewModel"
    private val sharedPrefs = application.getSharedPreferences("udc_user_prefs", Context.MODE_PRIVATE)

    // Repository & Database references
    private val database = AppDatabase.getDatabase(application)
    private val repository = UdcRepository(
        database.serviceApplicationDao(),
        database.noticeDao(),
        database.computerTrainingDao(),
        database.userAccountDao()
    )

    // Auth state Management
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Service applications cache (observed according to user type)
    private val _serviceApplications = MutableStateFlow<List<ServiceApplication>>(emptyList())
    val serviceApplications: StateFlow<List<ServiceApplication>> = _serviceApplications.asStateFlow()

    // Notice Board cache
    val notices: StateFlow<List<Notice>> = repository.getAllNotices()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Computer Training registrations cache (observed according to user type)
    private val _trainingRegistrations = MutableStateFlow<List<ComputerTraining>>(emptyList())
    val trainingRegistrations: StateFlow<List<ComputerTraining>> = _trainingRegistrations.asStateFlow()

    // AI Chat Bot
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "assistant",
                message = "Welcome to the Bangladesh Union Information & Digital Services directory! 🇧🇩\n" +
                          "I am your official UDC consultant. Ask me about **birth certificates, land records (porcha), citizen certificates, passport assistance, or utility payments**. I consult live Google Search results for up-to-date regional rules and fees."
            )
        )
    )
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Firebase Credentials Settings Monitoring
    private val _firebaseConfigured = MutableStateFlow(false)
    val firebaseConfigured: StateFlow<Boolean> = _firebaseConfigured.asStateFlow()

    init {
        // Initialize Firebase safely on startup
        viewModelScope.launch {
            FirebaseManager.initializeSafe(getApplication())
            _firebaseConfigured.value = FirebaseManager.isInitialized
            loadPersistedUser()
            seedInitialNoticesIfNeeded()
        }
    }

    // Attempt dynamically initializing Firebase from the UI Setup Configuration Custom Portal
    fun configureFirebaseDynamically(apiKey: String, appId: String, projectId: String) {
        viewModelScope.launch {
            FirebaseManager.initializeSafe(
                context = getApplication(),
                apiKey = apiKey,
                appId = appId,
                projectId = projectId
            )
            _firebaseConfigured.value = FirebaseManager.isInitialized
        }
    }

    private fun loadPersistedUser() {
        val email = sharedPrefs.getString("email", null)
        val name = sharedPrefs.getString("name", null)
        val role = sharedPrefs.getString("role", null)
        val upazila = sharedPrefs.getString("upazila", "") ?: ""
        val union = sharedPrefs.getString("union", "") ?: ""
        val phone = sharedPrefs.getString("phone", "") ?: ""

        if (email != null && name != null && role != null) {
            val user = UserData(email, name, role, upazila, union, phone)
            _authState.value = AuthState.LoggedIn(user)
            observeUserResources(user)
        } else {
            _authState.value = AuthState.LoggedOut
        }
    }

    fun signup(
        email: String,
        passwordPlain: String,
        name: String,
        role: String,
        upazila: String,
        union: String,
        phone: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existing = repository.getUserByEmail(email)
                if (existing != null) {
                    onFailure("An account with this email already exists!")
                    return@launch
                }

                val userAccount = com.example.data.model.UserAccount(
                    email = email,
                    passwordHash = passwordPlain,
                    displayName = name,
                    role = role,
                    upazilaName = upazila,
                    unionName = union,
                    phone = phone
                )
                repository.insertUser(userAccount)

                // Optional: Attempt registering to Firebase Auth if helper is configured
                try {
                    FirebaseManager.auth?.createUserWithEmailAndPassword(email, passwordPlain)
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase Auth sign up fallback ignored: ${e.message}")
                }

                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Registration failed")
            }
        }
    }

    fun login(email: String, name: String, role: String, upazila: String, union: String, phone: String) {
        // Legacy support delegation - saves a default account if not exists, then logs in
        viewModelScope.launch {
            try {
                val existing = repository.getUserByEmail(email)
                if (existing == null) {
                    repository.insertUser(
                        com.example.data.model.UserAccount(
                            email = email,
                            passwordHash = "123456",
                            displayName = name,
                            role = role,
                            upazilaName = upazila,
                            unionName = union,
                            phone = phone
                        )
                    )
                }
                loginWithEmailAndPassword(email, "123456", {}, {})
            } catch (e: Exception) {
                Log.e(TAG, "Legacy login error: ${e.message}")
            }
        }
    }

    fun loginWithEmailAndPassword(
        email: String,
        passwordPlain: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userAccount = repository.getUserByEmail(email)
                if (userAccount == null) {
                    onFailure("No account found with this email!")
                    return@launch
                }
                if (userAccount.passwordHash != passwordPlain) {
                    onFailure("Incorrect password!")
                    return@launch
                }

                // Optional: Try Firebase Authentication sign in
                try {
                    FirebaseManager.auth?.signInWithEmailAndPassword(email, passwordPlain)
                } catch (ex: Exception) {
                    Log.w(TAG, "Firebase Auth sign in fallback ignored: ${ex.message}")
                }

                val user = UserData(
                    email = userAccount.email,
                    displayName = userAccount.displayName,
                    role = userAccount.role,
                    upazilaName = userAccount.upazilaName,
                    unionName = userAccount.unionName,
                    phone = userAccount.phone
                )

                sharedPrefs.edit().apply {
                    putString("email", user.email)
                    putString("name", user.displayName)
                    putString("role", user.role)
                    putString("upazila", user.upazilaName)
                    putString("union", user.unionName)
                    putString("phone", user.phone)
                    apply()
                }

                _authState.value = AuthState.LoggedIn(user)
                observeUserResources(user)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Authentication failed")
            }
        }
    }

    fun logout() {
        FirebaseManager.auth?.signOut()
        sharedPrefs.edit().clear().apply()
        _authState.value = AuthState.LoggedOut
        _serviceApplications.value = emptyList()
        _trainingRegistrations.value = emptyList()
    }

    private fun observeUserResources(user: UserData) {
        viewModelScope.launch {
            if (user.role == "Entrepreneur") {
                // Entrepreneurs manage all applications at the Union Digital Centre
                repository.getAllApplications().collect { list ->
                    _serviceApplications.value = list
                }
            } else {
                // Citizens only see their own application history
                repository.getApplicationsByCitizen(user.email).collect { list ->
                    _serviceApplications.value = list
                }
            }
        }

        viewModelScope.launch {
            if (user.role == "Entrepreneur") {
                repository.getAllRegistrations().collect { list ->
                    _trainingRegistrations.value = list
                }
            } else {
                repository.getRegistrationsByTrainee(user.email).collect { list ->
                    _trainingRegistrations.value = list
                }
            }
        }
    }

    // --- Services Database actions ---

    fun submitServiceApplication(
        serviceType: String,
        details: String,
        fee: Double,
        payMethod: String,
        txnId: String
    ) {
        val userState = _authState.value
        if (userState is AuthState.LoggedIn) {
            viewModelScope.launch(Dispatchers.IO) {
                val app = ServiceApplication(
                    citizenEmail = userState.user.email,
                    citizenName = userState.user.displayName,
                    serviceType = serviceType,
                    applicantDetails = details,
                    feeAmount = fee,
                    paymentMethod = payMethod,
                    transactionId = txnId,
                    applicationStatus = "Submitted"
                )
                repository.insertApplication(app)
            }
        }
    }

    fun updateApplicationStatus(appId: Int, newStatus: String, remarks: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val app = repository.getApplicationById(appId)
            if (app != null) {
                val updated = app.copy(
                    applicationStatus = newStatus,
                    remarks = remarks
                )
                repository.updateApplication(updated)
            }
        }
    }

    fun publishNotice(title: String, content: String, category: String, isImportant: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val notice = Notice(
                title = title,
                content = content,
                category = category,
                isImportant = isImportant
            )
            repository.insertNotice(notice)
        }
    }

    fun deleteNotice(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNotice(id)
        }
    }

    fun registerForTraining(courseName: String, traineePhone: String) {
        val userState = _authState.value
        if (userState is AuthState.LoggedIn) {
            viewModelScope.launch(Dispatchers.IO) {
                val reg = ComputerTraining(
                    traineeName = userState.user.displayName,
                    traineeEmail = userState.user.email,
                    traineePhone = traineePhone,
                    courseName = courseName
                )
                repository.insertRegistration(reg)
            }
        }
    }

    fun updateTrainingStatus(regId: Int, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Find and update item. For simplicity we can collect and update via query or update direct
            _trainingRegistrations.value.find { it.id == regId }?.let { reg ->
                val updated = reg.copy(registrationStatus = newStatus)
                repository.updateRegistration(updated)
            }
        }
    }

    // --- AI Assistant Consultation ---

    fun sendConsultantMessage(messageText: String) {
        if (messageText.isBlank()) return

        val userMsg = ChatMessage(sender = "citizen", message = messageText)
        _chatHistory.value = _chatHistory.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            try {
                // Call Google Search Grounded Gemini Model
                val result = GeminiChatRepository.getAIResponseOfUDC(messageText)
                val assistantMsg = ChatMessage(
                    sender = "assistant",
                    message = result.text,
                    searchSources = result.searchSources,
                    searchQueries = result.searchQueries
                )
                _chatHistory.value = _chatHistory.value + assistantMsg
            } catch (e: Exception) {
                _chatHistory.value = _chatHistory.value + ChatMessage(
                    sender = "assistant",
                    message = "System consultant is temporarily offline: ${e.message}. Please try again later."
                )
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            ChatMessage(
                sender = "assistant",
                message = "Consultant sessions reset successfully! Ask me anything regarding UDC service registrations."
            )
        )
    }

    // Seed database with mock startup announcements so the first loading looks outstanding!
    private suspend fun seedInitialNoticesIfNeeded() {
        if (repository.getAllNotices().first().isEmpty()) {
            withContext(Dispatchers.IO) {
                repository.insertNotice(
                    Notice(
                        title = "Free Digital Literacy Training Camp 💻",
                        content = "Union Digital Centre (UDC) is launching a free 2-week Computer Office Application and Internet usage training camp starting next Sunday. Registration is now open on our app's Computer Training Tab for all residents. Limited seats available!",
                        category = "Computer Course",
                        isImportant = true
                    )
                )
                repository.insertNotice(
                    Notice(
                        title = "Government Smart Card Distribution Event Card 💳",
                        content = "The Local Government Division will distribute Smart National Identity Cards (NID) at our Union Parisad Hall starting Tuesday, June 23rd. Please bring your original voter registration slip or regular NID copy for receipt. Timing: 9:00 AM to 4:00 PM.",
                        category = "Govt Service",
                        isImportant = false
                    )
                )
                repository.insertNotice(
                    Notice(
                        title = "Agriculture Input Subsidy Cards Distribution 🌾",
                        content = "Subsidy cards and digital agriculture guidance will be provided at the Digital Centre to selected local farmers. Supported by the Directorate of Agriculture Extension. Registration verified via local Porcha.",
                        category = "Agriculture",
                        isImportant = false
                    )
                )
            }
        }
    }
}
