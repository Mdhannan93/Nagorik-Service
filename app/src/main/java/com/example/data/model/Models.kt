package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "service_applications")
data class ServiceApplication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val citizenEmail: String,
    val citizenName: String,
    val serviceType: String,
    val applicantDetails: String, // Stored as a simple readable formatted string
    val feeAmount: Double,
    val paymentMethod: String,
    val transactionId: String,
    val applicationStatus: String = "Submitted", // "Submitted", "Under Review", "Processing", "Approved", "Completed", "Rejected"
    val submissionTimestamp: Long = System.currentTimeMillis(),
    val remarks: String = ""
)

@Serializable
@Entity(tableName = "notices")
data class Notice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val date: Long = System.currentTimeMillis(),
    val category: String, // "Govt Service", "Health Campaign", "Computer Course", "Agriculture"
    val isImportant: Boolean = false
)

@Serializable
@Entity(tableName = "computer_trainings")
data class ComputerTraining(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val traineeName: String,
    val traineeEmail: String,
    val traineePhone: String,
    val courseName: String, // "Basic Office & Internet", "Graphic Design & Freelancing", "Web Development Basic"
    val registrationStatus: String = "Registered", // "Registered", "Approved", "Ongoing", "Completed"
    val registrationTimestamp: Long = System.currentTimeMillis()
)

@Serializable
data class UserData(
    val email: String,
    val displayName: String,
    val role: String, // "Citizen" or "Entrepreneur"
    val upazilaName: String = "",
    val unionName: String = "",
    val phone: String = ""
)

@Serializable
@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val displayName: String,
    val role: String, // "Citizen" or "Entrepreneur"
    val upazilaName: String = "",
    val unionName: String = "",
    val phone: String = ""
)

@Serializable
data class UdcService(
    val id: String,
    val name: String,
    val banglaName: String,
    val category: String, // "Certificates & Government Docs", "Land & Registry Services", "Financial & Utility Bills", "Assistance, Visa & Passport", "Education & Training Courses"
    val description: String,
    val feeDetails: String,
    val sampleFormIndex: Int, // Form template matching mapping in UI
    val keywords: List<String> = emptyList()
) {
    companion object {
        val defaultServices = listOf(
            UdcService(
                id = "birth_reg",
                name = "Birth Registration",
                banglaName = "জন্ম নিবন্ধন আবেদন",
                category = "Certificates & Government Docs",
                description = "New birth certificate registration, application correction, verification, or duplicate copy processing.",
                feeDetails = "৳ 50 BDT dynamic fee",
                sampleFormIndex = 0,
                keywords = listOf("birth", "jonmo", "nibondhon", "certificate", "child", "baby", "correction")
            ),
            UdcService(
                id = "citizen_cert",
                name = "Citizen Certificate",
                banglaName = "নাগরিকত্ব ও উত্তরাধিকার সনদ",
                category = "Certificates & Government Docs",
                description = "Character, inheritance (warish), or general local citizen status verification certificate from Union Parishad.",
                feeDetails = "৳ 20 BDT processing fee",
                sampleFormIndex = 2,
                keywords = listOf("citizen", "chairman", "certificate", "nagorik", "shonod", "warish", "inheritance", "character")
            ),
            UdcService(
                id = "porcha",
                name = "Land Record (Porcha)",
                banglaName = "খতিয়ান বা পরচা নকলের আবেদন",
                category = "Land & Registry Services",
                description = "Official certified copies of SA, CS, RS, or BS ledger land records directly verified by Assistant Commissioner (Land) / DC Office.",
                feeDetails = "৳ 100 BDT certified fee",
                sampleFormIndex = 1,
                keywords = listOf("land", "record", "porcha", "khatian", "mouza", "jomi", "bhumi", "ledger")
            ),
            UdcService(
                id = "land_tax",
                name = "Land Development Tax",
                banglaName = "ভূমি উন্নয়ন কর ও নামজারি সহায়তা",
                category = "Land & Registry Services",
                description = "Digital land development tax (Bhumi kar) payment assistance, dakhila registration, and online namzari filing assistance.",
                feeDetails = "৳ 80 BDT assistance fee",
                sampleFormIndex = 1,
                keywords = listOf("tax", "land tax", "bhumi", "dakhila", "namzari", "mutation", "jomi ledger")
            ),
            UdcService(
                id = "utility_bill",
                name = "Utility Bill Payment",
                banglaName = "ইউটিলিটি বিল পরিশোধ (বিদ্যুৎ/গ্যাস/পানি)",
                category = "Financial & Utility Bills",
                description = "Digital instant payment assistance for REB, DESCO electricity bills, Titas gas, or WASA water bills without standing in long queues.",
                feeDetails = "৳ Bill amount + 10 BDT surcharge",
                sampleFormIndex = 4,
                keywords = listOf("bill", "utility", "desco", "titan", "reb", "electricity", "gas", "water", "payment", "smart")
            ),
            UdcService(
                id = "allowance",
                name = "Social Safety Net Allowance",
                banglaName = "বয়স্ক ও বিধবা সামাজিক নিরাপত্তা ভাতা",
                category = "Financial & Utility Bills",
                description = "Direct application assistance for Government Social Safety nets: Old Age allowance, Widow allowance, and Disability allowance.",
                feeDetails = "৳ 30 BDT processing fee",
                sampleFormIndex = 2,
                keywords = listOf("allowance", "safety net", "bhata", "old", "widow", "disability", "government fund")
            ),
            UdcService(
                id = "passport",
                name = "Passport Online Submission",
                banglaName = "ই-পাসপোর্ট অনলাইন আবেদন সহায়তা",
                category = "Assistance, Visa & Passport",
                description = "Official E-Passport enrollment form completion, online date appointment scheduling, application slip generation, and fee receipt assistance.",
                feeDetails = "৳ 150 BDT enrollment assistance",
                sampleFormIndex = 3,
                keywords = listOf("passport", "visa", "slip", "enrollment", "travel", "appointment", "immigration")
            ),
            UdcService(
                id = "it_training",
                name = "Computer Training Course",
                banglaName = "ডিজিটাল কম্পিউটার ও ফ্রিল্যান্সিং প্রশিক্ষণ",
                category = "Education & Training Courses",
                description = "Registrations of Union Digital Centre flagship 3-month/6-month Microsoft Office and freelancing literacy programs.",
                feeDetails = "৳ 200 BDT token registrar fee",
                sampleFormIndex = 5,
                keywords = listOf("computer", "training", "course", "literacy", "office", "internet", "graphic", "coding", "school")
            )
        )
    }
}
