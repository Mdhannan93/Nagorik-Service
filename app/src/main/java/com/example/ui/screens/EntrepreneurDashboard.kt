package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ComputerTraining
import com.example.data.model.Notice
import com.example.data.model.ServiceApplication
import com.example.data.model.UserData
import com.example.ui.viewmodel.UDCViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrepreneurDashboard(
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
                            text = "UDC Entrepreneur Console",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${user.unionName} UP • Operator: ${user.displayName}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin icon",
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
                    icon = { Icon(Icons.Default.Dns, "Ledger") },
                    label = { Text("Ledger", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_nav_ledger")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.AddHomeWork, "Notice") },
                    label = { Text("Notice", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_nav_notice")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Class, "Courses") },
                    label = { Text("Courses", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_nav_courses")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Forum, "AI Policy") },
                    label = { Text("AI Policy", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_nav_ai")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Insights, "Revenue") },
                    label = { Text("Revenue", fontSize = 11.sp) },
                    modifier = Modifier.testTag("admin_nav_revenue")
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
                0 -> ManageSubmissionsTab(viewModel, snackbarHostState)
                1 -> BroadcastNoticeTab(viewModel, snackbarHostState)
                2 -> CourseTraineesTab(viewModel, snackbarHostState)
                3 -> AIChatConsultantTab(viewModel) // Reuse chat component
                4 -> RevenueStatsTab(user, viewModel, onLogout, snackbarHostState)
            }
        }
    }
}

// ======================== TAB 0: SUBMISSIONS REVIEW LEDGER ========================

@Composable
fun ManageSubmissionsTab(viewModel: UDCViewModel, snackbarHostState: SnackbarHostState) {
    val applications by viewModel.serviceApplications.collectAsState()
    var filterStatus by remember { mutableStateOf("All") }

    val filteredList = when (filterStatus) {
        "Pending" -> applications.filter { it.applicationStatus == "Submitted" || it.applicationStatus == "Under Review" }
        "Completed" -> applications.filter { it.applicationStatus == "Completed" || it.applicationStatus == "Approved" }
        else -> applications
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Selection segments
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("All", "Pending", "Completed").forEach { opt ->
                Button(
                    onClick = { filterStatus = opt },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filterStatus == opt) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (filterStatus == opt) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(opt, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No applications matches the selected criteria.", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { app ->
                    ManageApplicationItem(app, viewModel, snackbarHostState)
                }
            }
        }
    }
}

@Composable
fun ManageApplicationItem(app: ServiceApplication, viewModel: UDCViewModel, snackbarHostState: SnackbarHostState) {
    var expanded by remember { mutableStateOf(false) }
    var remarksInput by remember { mutableStateOf(app.remarks) }
    val coroutineScope = rememberCoroutineScope()

    val badgeColor = when (app.applicationStatus) {
        "Submitted" -> Color(0xFFFFB300)
        "Under Review" -> Color(0xFF1E88E5)
        "Processing" -> Color(0xFF8E24AA)
        "Approved", "Completed" -> Color(0xFF43A047)
        else -> Color(0xFFE53935)
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
                        app.serviceType,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Citizen: ${app.citizenName} (${app.citizenEmail})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        app.applicationStatus,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Fee collected: ৳ ${app.feeAmount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Txn: ${app.transactionId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Service Form Entries:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    Text(app.applicantDetails, fontSize = 12.sp)
                    Text("Payment Method: ${app.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

                    Divider()

                    // Quick Actions to change Status
                    Text("Review Decision:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateApplicationStatus(app.id, "Under Review", remarksInput)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Status updated to Under Review") }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Review", fontSize = 10.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.updateApplicationStatus(app.id, "Approved", remarksInput)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Service application Approved") }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Text("Approve", fontSize = 10.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.updateApplicationStatus(app.id, "Rejected", remarksInput)
                                coroutineScope.launch { snackbarHostState.showSnackbar("Service Rejected") }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject", fontSize = 10.sp, color = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = remarksInput,
                        onValueChange = { remarksInput = it },
                        label = { Text("Entrepreneur Instruction Message") },
                        placeholder = { Text("e.g. Your verification cert is ready. Collect at room 3.") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Button(
                        onClick = {
                            viewModel.updateApplicationStatus(app.id, app.applicationStatus, remarksInput)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Instructions saved and synced.") }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Message Instructions", fontSize = 12.sp)
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

// ======================== TAB 1: BROADCAST NOTICES ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastNoticeTab(viewModel: UDCViewModel, snackbarHostState: SnackbarHostState) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Govt Service") }
    var isImportant by remember { mutableStateOf(false) }

    var expandedDrop by remember { mutableStateOf(false) }
    val categories = listOf("Govt Service", "Health Campaign", "Computer Course", "Agriculture")

    val noticeList by viewModel.notices.collectAsState()
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
            text = "Broadcast Union Notice",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Notice Category") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDrop = true }
                    )
                    DropdownMenu(expanded = expandedDrop, onDismissRequest = { expandedDrop = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = {
                                category = cat
                                expandedDrop = false
                            })
                        }
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Notice Details / Announcement Information") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mark as Critical Important Alert?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(checked = isImportant, onCheckedChange = { isImportant = it })
                }

                Button(
                    onClick = {
                        if (title.isBlank() || content.isBlank()) {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Please fill notice title and body information!") }
                        } else {
                            viewModel.publishNotice(title, content, category, isImportant)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Notice published and broadcasted successfully!") }
                            title = ""
                            content = ""
                            isImportant = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.VolumeUp, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Broadcast Announcement")
                }
            }
        }

        Divider()

        // List active notices and support local deletions
        Text("Active System Announcements", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

        if (noticeList.isEmpty()) {
            Text("No announcements broadcasted actively.", fontSize = 12.sp)
        } else {
            noticeList.forEach { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(notice.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.deleteNotice(notice.id) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ======================== TAB 2: COURSE TRAINEES ADMIN ========================

@Composable
fun CourseTraineesTab(viewModel: UDCViewModel, snackbarHostState: SnackbarHostState) {
    val registrations by viewModel.trainingRegistrations.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    if (registrations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No student registrees registered for technical courses currently.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Manage Enrolled Trainees",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(registrations) { student ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(student.courseName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Student: ${student.traineeName}", fontSize = 13.sp)
                                Text("Email: ${student.traineeEmail} | Phone: ${student.traineePhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            Surface(
                                color = when (student.registrationStatus) {
                                    "Registered" -> Color(0xFFFFB300)
                                    "Approved" -> Color(0xFF1E88E5)
                                    "Ongoing" -> Color(0xFF8E24AA)
                                    else -> Color(0xFF43A047)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    student.registrationStatus,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateTrainingStatus(student.id, "Approved")
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Student course Registration Approved") }
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Approve", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.updateTrainingStatus(student.id, "Ongoing")
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Student status moved to class Ongoing") }
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Ongoing", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.updateTrainingStatus(student.id, "Completed")
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Class status marked Completed") }
                                },
                                modifier = Modifier.weight(1.1f),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Completed", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================== TAB 4: REVENUE STATS & SETTINGS ========================

@Composable
fun RevenueStatsTab(
    user: UserData,
    viewModel: UDCViewModel,
    onLogout: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val applications by viewModel.serviceApplications.collectAsState()
    val trainees by viewModel.trainingRegistrations.collectAsState()
    val firebaseConfigured by viewModel.firebaseConfigured.collectAsState()

    val totalRevenue = applications.sumOf { it.feeAmount }
    val completedFees = applications.filter { it.applicationStatus == "Completed" || it.applicationStatus == "Approved" }.sumOf { it.feeAmount }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Center Revenue & Stats",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        // General Stats Indicators Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Total Collected", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("৳ $totalRevenue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Card(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Service Delivered", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("৳ $completedFees", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Digital Submissions", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${applications.size} files", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Trainees Enrolled", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${trainees.size} students", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Custom M3 Dynamic Ledger Revenue Grid-Chart Representation
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Service Category Volume Chart",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider()

                val serviceCounts = applications.groupBy { it.serviceType }.map { (type, list) ->
                    val total = list.sumOf { it.feeAmount }
                    type.take(20) + "..." to total
                }

                if (serviceCounts.isEmpty()) {
                    Text("No visual revenue metric streams yet.", fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                } else {
                    serviceCounts.forEach { (cat, bdt) ->
                        val pct = if (totalRevenue > 0) (bdt / totalRevenue).toFloat() else 0f
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("৳ $bdt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            // Styled bar representation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(pct)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Configuration status details card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Entrepreneur Details",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider()
                Text("Operator: ${user.displayName}", fontSize = 13.sp)
                Text("UP Code: BR-UDC-${user.unionName.take(3).uppercase()}", fontSize = 13.sp)
                Text("Firebase Storage: " + if (firebaseConfigured) "Google Cloud Synced Active" else "Offline Local Cache Pool", fontSize = 13.sp)
            }
        }

        // Logout
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("admin_logout_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.ExitToApp, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout & Lock Terminal", fontWeight = FontWeight.Bold)
        }
    }
}
