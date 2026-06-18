package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ComputerTraining
import com.example.data.model.Notice
import com.example.data.model.ServiceApplication
import com.example.data.model.UserData
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.UDCViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenDashboard(
    user: UserData,
    viewModel: UDCViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Union Digital Centre Service",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${user.unionName} UP • Citizen Portal",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Union icon",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PostAdd, "Apply") },
                    label = { Text("Apply", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_apply")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.ReceiptLong, "Track") },
                    label = { Text("Track", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_track")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.School, "Notices") },
                    label = { Text("Notices", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_notices")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.LiveHelp, "AI Help") },
                    label = { Text("AI Help", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_ai")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("Settings", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ApplyServicesTab(viewModel, snackbarHostState)
                1 -> TrackApplicationsTab(viewModel)
                2 -> NoticesTrainingTab(viewModel, snackbarHostState)
                3 -> AIChatConsultantTab(viewModel)
                4 -> PortalSettingsTab(user, viewModel, onLogout, snackbarHostState)
            }
        }
    }
}

// ======================== TAB 0: APPLY FOR SERVICES ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyServicesTab(viewModel: UDCViewModel, snackbarHostState: SnackbarHostState) {
    val serviceTypes = listOf(
        "Birth Registration (জন্ম নিবন্ধন)",
        "Land Record (Porcha/খতিয়ান পোর্চা)",
        "Citizen Certificate (নাগরিকত্ব সনদ)",
        "Passport Online Submission Assistance",
        "Utility Bill Payment (বিদ্যুৎ/গ্যাস/পানি বিল)"
    )

    // Advanced search states
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val allServices = com.example.data.model.UdcService.defaultServices
    var selectedUdcService by remember { mutableStateOf(allServices[0]) }

    val filteredServices = remember(searchQuery, selectedCategory) {
        allServices.filter { service ->
            val matchesCategory = selectedCategory == "All" || service.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    service.name.contains(searchQuery, ignoreCase = true) ||
                    service.banglaName.contains(searchQuery, ignoreCase = true) ||
                    service.description.contains(searchQuery, ignoreCase = true) ||
                    service.keywords.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
    }

    // Dynamic legacy-bridge selection computed state
    val selectedService = when (selectedUdcService.id) {
        "birth_reg" -> serviceTypes[0]
        "porcha" -> serviceTypes[1]
        "land_tax" -> serviceTypes[1]
        "citizen_cert" -> serviceTypes[2]
        "allowance" -> serviceTypes[2]
        "passport" -> serviceTypes[3]
        "utility_bill" -> serviceTypes[4]
        else -> serviceTypes[0]
    }

    // Universal fields
    var applicantName by remember { mutableStateOf("") }
    var extraDetails by remember { mutableStateOf("") }
    var txnId by remember { mutableStateOf("") }
    var payMethod by remember { mutableStateOf("bKash") }
    var paymentPhone by remember { mutableStateOf("") }

    // Service specific fields
    var birthDate by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }

    var khatianNo by remember { mutableStateOf("") }
    var plotNo by remember { mutableStateOf("") }
    var MouzaName by remember { mutableStateOf("") }

    var nidNo by remember { mutableStateOf("") }
    var certPurpose by remember { mutableStateOf("") }

    var passportSlipNo by remember { mutableStateOf("") }

    var billType by remember { mutableStateOf("Electricity (DESCO/REB)") }
    var billAccountNo by remember { mutableStateOf("") }
    var billAmount by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Determine processing fee
    val processingFee = when (selectedUdcService.id) {
        "birth_reg" -> 50.0 // Birth
        "porcha" -> 100.0 // Porcha
        "land_tax" -> 80.0 // Land tax
        "citizen_cert" -> 20.0 // Citizen Cert
        "allowance" -> 30.0 // Allowance
        "passport" -> 150.0 // Passport
        "utility_bill" -> { // Utility bill
            val amt = billAmount.toDoubleOrNull() ?: 0.0
            amt + 10.0 // Surcharge
        }
        else -> 50.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Union Digital Services Directory (ইউডিডি)",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        // OUTSTANDING SEARCH & CATEGORIES MODULE
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search bar Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search services (e.g. birth, bill, khatian, পরচা)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_search_bar"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Category selection list view
                Text(
                    text = "Browse Service Category (শাখা নির্বাচন করুন)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                val categories = listOf(
                    "All",
                    "Certificates & Government Docs",
                    "Land & Registry Services",
                    "Financial & Utility Bills",
                    "Assistance, Visa & Passport",
                    "Education & Training Courses"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(if (category == "All") "All (সব)" else category, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Horizontal filtered matching services
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Available Services (${filteredServices.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            if (filteredServices.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No services matched your query. Try different keywords like 'jonmo', 'record', or select 'All'!",
                            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredServices.forEach { service ->
                        val isSelected = selectedUdcService.id == service.id
                        Card(
                            onClick = { selectedUdcService = service },
                            modifier = Modifier
                                .width(240.dp)
                                .height(150.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = service.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = service.banglaName,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = service.description,
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp,
                                        maxLines = 3,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = service.feeDetails,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "Select",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedUdcService.id == "it_training") {
            // Inform students that Computer training has its own dedicated Tab!
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🎓 Computer Course Admission Portal",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "To enroll in the Computer Literacy and Freelancing course, click the third tab ('Notices') on your bottom navigation bar, scroll to 'Computer Course Registrar', select your desired course, and register instantly!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        // Dynamic Form Fields according to service selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant).let { CardDefaults.cardElevation(defaultElevation = 2.dp) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Application Information Verification",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = applicantName,
                    onValueChange = { applicantName = it },
                    label = { Text("Applicant's Name (আবেদনকারীর নাম)") },
                    modifier = Modifier.fillMaxWidth().testTag("form_applicant_name"),
                    singleLine = true
                )

                when (selectedService) {
                    serviceTypes[0] -> { // Birth Registration
                        OutlinedTextField(
                            value = birthDate,
                            onValueChange = { birthDate = it },
                            label = { Text("Date of Birth (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth().testTag("form_dob"),
                            placeholder = { Text("e.g. 1998-05-12") }
                        )
                        OutlinedTextField(
                            value = fatherName,
                            onValueChange = { fatherName = it },
                            label = { Text("Father's Name (পিতার নাম)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = motherName,
                            onValueChange = { motherName = it },
                            label = { Text("Mother's Name (মাতার নাম)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    serviceTypes[1] -> { // Land Record / Porcha
                        OutlinedTextField(
                            value = MouzaName,
                            onValueChange = { MouzaName = it },
                            label = { Text("Mouza Name (মৌজার নাম)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = khatianNo,
                                onValueChange = { khatianNo = it },
                                label = { Text("Khatian No (খতিয়ান নং)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = plotNo,
                                onValueChange = { plotNo = it },
                                label = { Text("Daag/Plot No (দাগ নং)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    serviceTypes[2] -> { // Citizen Cert
                        OutlinedTextField(
                            value = nidNo,
                            onValueChange = { nidNo = it },
                            label = { Text("NID / Birth Registration No") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = certPurpose,
                            onValueChange = { certPurpose = it },
                            label = { Text("Purpose of Certificate") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Passport Application / Job") }
                        )
                    }
                    serviceTypes[3] -> { // Passport assist
                        OutlinedTextField(
                            value = passportSlipNo,
                            onValueChange = { passportSlipNo = it },
                            label = { Text("Online Enrollment ID / Slip Number") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. E-P1049281") }
                        )
                        OutlinedTextField(
                            value = extraDetails,
                            onValueChange = { extraDetails = it },
                            label = { Text("Additional Instructions / Request Details") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                    serviceTypes[4] -> { // Utility bill
                        // Segmented bill type selector
                        Text("Utility Provider Type:", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("DESCO (Elec)", "Titas (Gas)", "WASA (Water)").forEach { provider ->
                                FilterChip(
                                    selected = billType.startsWith(provider.substring(0, 4)),
                                    onClick = { billType = provider },
                                    label = { Text(provider, fontSize = 11.sp) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = billAccountNo,
                            onValueChange = { billAccountNo = it },
                            label = { Text("Billing Account Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = billAmount,
                            onValueChange = { billAmount = it },
                            label = { Text("Bill Original Due Amount (BDT)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Processing fee panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Processing Fee:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "(Includes standard government charges & UDC fees)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "৳ $processingFee BDT",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Payment gateway panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Digital Fee Mobile Payment Gate",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Please send ৳ $processingFee BDT to UDC Merchant dynamic number (01700-XXXXXX) via USSD or app, then input details:",
                    fontSize = 12.sp,
                )

                // Selector for Payment Method
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val methods = listOf("bKash", "Nagad", "Rocket")
                    methods.forEach { method ->
                        Button(
                            onClick = { payMethod = method },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (payMethod == method) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (payMethod == method) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(method, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = paymentPhone,
                        onValueChange = { paymentPhone = it },
                        label = { Text("Sender Mobile No") },
                        modifier = Modifier.weight(1.2f).testTag("payment_sender"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = txnId,
                        onValueChange = { txnId = it },
                        label = { Text("Transaction ID") },
                        placeholder = { Text("TRX832K9S") },
                        modifier = Modifier.weight(1f).testTag("payment_txnid"),
                        singleLine = true
                    )
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                // Validation inputs
                val isBill = selectedService == serviceTypes[4]
                val amountValid = if (isBill) billAmount.toDoubleOrNull() != null else true

                if (applicantName.isBlank() || txnId.isBlank() || paymentPhone.isBlank()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Please input applicant name, sender mobile, and transactional ID!")
                    }
                } else if (!amountValid) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Please enter a valid utility representation numerical amount!")
                    }
                } else {
                    // Build application details
                    val detailsSummary = when (selectedService) {
                        serviceTypes[0] -> "DOB: $birthDate | Father: $fatherName | Mother: $motherName"
                        serviceTypes[1] -> "Mouza: $MouzaName | Khatian: $khatianNo | Plot/Daag: $plotNo"
                        serviceTypes[2] -> "NID: $nidNo | Purpose: $certPurpose"
                        serviceTypes[3] -> "Slip No: $passportSlipNo | Notes: $extraDetails"
                        else -> "Provider: $billType | Account: $billAccountNo | Due: $billAmount BDT"
                    }

                    viewModel.submitServiceApplication(
                        serviceType = selectedService,
                        details = detailsSummary,
                        fee = processingFee,
                        payMethod = "$payMethod (from $paymentPhone)",
                        txnId = txnId
                    )

                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Your UDC Application has been successfully submitted! Tracking index is pending review.")
                    }

                    // Reset values
                    applicantName = ""
                    birthDate = ""
                    fatherName = ""
                    motherName = ""
                    MouzaName = ""
                    khatianNo = ""
                    plotNo = ""
                    nidNo = ""
                    certPurpose = ""
                    passportSlipNo = ""
                    billAccountNo = ""
                    billAmount = ""
                    extraDetails = ""
                    txnId = ""
                    paymentPhone = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("apply_service_submit_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Send, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Submit & Process Application", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ======================== TAB 1: TRACK APPLICATIONS ========================

@Composable
fun TrackApplicationsTab(viewModel: UDCViewModel) {
    val applications by viewModel.serviceApplications.collectAsState()

    if (applications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No Submissions Found",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Apply for a birth cert, land record, or certificate in apply tab to see statuses here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Track Your Applications",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(applications) { app ->
                ApplicationCard(app)
            }
        }
    }
}

@Composable
fun ApplicationCard(app: ServiceApplication) {
    var expanded by remember { mutableStateOf(false) }

    // Color indicators mapping
    val badgeColor = when (app.applicationStatus) {
        "Submitted" -> Color(0xFFFFB300) // Gold
        "Under Review" -> Color(0xFF1E88E5) // Blue
        "Processing" -> Color(0xFF8E24AA) // Purple
        "Approved", "Completed" -> Color(0xFF43A047) // Green
        else -> Color(0xFFE53935) // Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = app.serviceType,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Applicant: ${app.citizenName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Styled Status Badge
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = app.applicationStatus,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fee Paid: ৳ ${app.feeAmount}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Txn ID: ${app.transactionId}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Parameters Submitted:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = app.applicantDetails,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Payment Method: ${app.paymentMethod}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    if (app.remarks.isNotBlank()) {
                        Divider()
                        Text(
                            text = "Entrepreneur Remarks & Instructions:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = app.remarks,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ======================== TAB 2: NOTICES & TRAINING ========================

@Composable
fun NoticesTrainingTab(viewModel: UDCViewModel, snackbarHostState: SnackbarHostState) {
    var subTabState by remember { mutableStateOf(0) } // 0: Bulletin board, 1: Computer Courses

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = subTabState) {
            Tab(selected = subTabState == 0, onClick = { subTabState = 0 }) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Campaign, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Announcements Board", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Tab(selected = subTabState == 1, onClick = { subTabState = 1 }) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Computer, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Computer Training", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (subTabState == 0) {
                NoticesBulletinSubTab(viewModel)
            } else {
                ComputerTrainingSubTab(viewModel, snackbarHostState)
            }
        }
    }
}

@Composable
fun NoticesBulletinSubTab(viewModel: UDCViewModel) {
    val noticeBoard by viewModel.notices.collectAsState()

    if (noticeBoard.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active announcements currently posted.", style = MaterialTheme.typography.bodySmall)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(noticeBoard) { notice ->
                NoticeCard(notice)
            }
        }
    }
}

@Composable
fun NoticeCard(notice: Notice) {
    val categoryIcon = when (notice.category) {
        "Computer Course" -> Icons.Default.Computer
        "Govt Service" -> Icons.Default.AccountBalance
        "Agriculture" -> Icons.Default.Yard
        else -> Icons.Default.NotificationImportant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (notice.isImportant) 1.5.dp else 0.dp,
                color = if (notice.isImportant) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notice.isImportant) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(categoryIcon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(
                        text = notice.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (notice.isImportant) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            "IMPORTANT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = notice.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ComputerTrainingSubTab(viewModel: UDCViewModel, snackbarHostState: SnackbarHostState) {
    val courses = listOf(
        "Basic Office & Internet Applications (3 Months)",
        "Graphic Design & International Freelancing (4 Months)",
        "Web Development & Coding Basic (6 Months)"
    )

    var selectedCourse by remember { mutableStateOf(courses[0]) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var traineePhone by remember { mutableStateOf("") }

    val registrations by viewModel.trainingRegistrations.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Enrolment Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enrol in Computer Courses",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Gain high-income technical skills certified directly by Bangladesh Computer Council (BCC).",
                    style = MaterialTheme.typography.bodySmall
                )

                // Course choice dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCourse,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Technical Course") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDropdown = true }
                            .testTag("course_dropdown")
                    )
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course) },
                                onClick = {
                                    selectedCourse = course
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = traineePhone,
                    onValueChange = { traineePhone = it },
                    label = { Text("Contact Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (traineePhone.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please input a valid phone number to complete enrollment!")
                            }
                        } else {
                            viewModel.registerForTraining(selectedCourse, traineePhone)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Course registration submitted successfully! Awaiting batch assignment.")
                            }
                            traineePhone = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("enroll_course_submit_btn")
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Register & Submit Form")
                }
            }
        }

        Divider()

        // Student Registration Tickets list
        Text(
            text = "Your Course Enrollments",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        if (registrations.isEmpty()) {
            Text(
                "You haven't enroled in any technical training courses yet.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        } else {
            registrations.forEach { reg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(reg.courseName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Phone: ${reg.traineePhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                        Surface(
                            color = when (reg.registrationStatus) {
                                "Registered" -> Color(0xFFFFB300)
                                "Approved" -> Color(0xFF1E88E5)
                                "Ongoing" -> Color(0xFF8E24AA)
                                else -> Color(0xFF43A047)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                reg.registrationStatus,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======================== TAB 3: AI GROUNDED CONSULTANT ========================

@Composable
fun AIChatConsultantTab(viewModel: UDCViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Automatically scroll to the end on new message
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat History list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(chatHistory) { bubble ->
                ChatBubble(message = bubble)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Consulting Google Search Grounding...",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider()

        // Message Input Controller box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("ai_clear_chat")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Session",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask about Birth Certs, Land Records...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_input_text"),
                maxLines = 3,
                shape = RoundedCornerShape(24.dp)
            )

            FloatingActionButton(
                onClick = {
                    if (textInput.isNotBlank() && !isLoading) {
                        viewModel.sendConsultantMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier.testTag("ai_send_message_btn"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isCitizen = message.sender == "citizen"
    val bubbleColor = if (isCitizen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val fontColor = if (isCitizen) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCitizen) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 290.dp),
            horizontalAlignment = if (isCitizen) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = bubbleColor,
                contentColor = fontColor,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isCitizen) 16.dp else 0.dp,
                    bottomEnd = if (isCitizen) 0.dp else 16.dp
                ),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.message,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Render Search Sources details Grounding Referencing
            if (message.searchSources.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ref Grounding Citations:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    message.searchSources.take(3).forEach { (title, url) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore opening errors
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Link, "link", modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    text = title.take(20) + "...",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper block class for wrap layouts
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        var layoutHeight = 0
        var xPosition = 0
        var yPosition = 0
        var rowHeight = 0

        placeables.forEach { placeable ->
            if (xPosition + placeable.width > layoutWidth) {
                xPosition = 0
                yPosition += rowHeight
                rowHeight = 0
            }
            rowHeight = maxOf(rowHeight, placeable.height)
            xPosition += placeable.width
        }
        layoutHeight = yPosition + rowHeight

        layout(layoutWidth, layoutHeight) {
            xPosition = 0
            yPosition = 0
            rowHeight = 0
            placeables.forEach { placeable ->
                if (xPosition + placeable.width > layoutWidth) {
                    xPosition = 0
                    yPosition += rowHeight
                    rowHeight = 0
                }
                placeable.placeRelative(xPosition, yPosition)
                rowHeight = maxOf(rowHeight, placeable.height)
                xPosition += placeable.width
            }
        }
    }
}

// ======================== TAB 4: PORTAL SETTINGS & FIREBASE MANUAL SETUP ========================

@Composable
fun PortalSettingsTab(
    user: UserData,
    viewModel: UDCViewModel,
    onLogout: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var manualApiKey by remember { mutableStateOf("") }
    var manualAppId by remember { mutableStateOf("") }
    var manualProjectId by remember { mutableStateOf("") }

    val firebaseConfigured by viewModel.firebaseConfigured.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Portal Profile & Control Group",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        // General Information Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Citizen Information",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Name:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(user.displayName, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Email:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(user.email, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Phone:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(user.phone, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Local Division:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${user.unionName} UP, ${user.upazilaName}", fontSize = 13.sp)
                }
            }
        }

        // Dedicated Firebase Cloud Sync Portal configured box
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Firebase Cloud Service Connection",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Status Badge
                    Surface(
                        color = if (firebaseConfigured) Color(0xFF43A047) else Color(0xFFFFB300),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (firebaseConfigured) "CLOUD ACTIVE" else "LOCAL OFFLINE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Divider()

                if (firebaseConfigured) {
                    Text(
                        text = "Great! Your portal database syncs in real-time with Google Firestore & Google Identity. Applications and registrations will persist over the cloud.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = "Currently running in local-first secure offline mode. You can configure Firebase manually below to activate cloud syncing over your own Firebase database:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = manualApiKey,
                        onValueChange = { manualApiKey = it },
                        label = { Text("Firebase API Key", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = manualAppId,
                        onValueChange = { manualAppId = it },
                        label = { Text("Firebase Application ID", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = manualProjectId,
                        onValueChange = { manualProjectId = it },
                        label = { Text("Firebase Project ID", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (manualApiKey.isBlank() || manualAppId.isBlank() || manualProjectId.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please fill all Firebase Dynamic Credentials configuration inputs!")
                                }
                            } else {
                                viewModel.configureFirebaseDynamically(manualApiKey, manualAppId, manualProjectId)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Connecting to Firebase Cloud Instance...")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudSync, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Connect Firebase Instance")
                    }
                }
            }
        }

        VerticalSpacer(12)

        // Logout Panel
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("portal_logout_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout & Reset Session", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VerticalSpacer(dp: Int) {
    Spacer(modifier = Modifier.height(dp.dp))
}
