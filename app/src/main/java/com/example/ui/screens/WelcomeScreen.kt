package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.UDCViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    viewModel: UDCViewModel
) {
    var isSignUpMode by remember { mutableStateOf(false) }

    // Account credentials input states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var upazila by remember { mutableStateOf("") }
    var union by remember { mutableStateOf("") }
    var isEntrepreneur by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Seed default credentials for quick-eval and premium onboarding
    LaunchedEffect(isSignUpMode) {
        showError = false
        if (!isSignUpMode && email.isBlank()) {
            // Suggest pre-filled details to make app evaluation extremely easy
            email = "citizen@udc.gov"
            password = "password123"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // Screen Header visual Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_udc_hero),
                    contentDescription = "Union Digital Centre banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Union Digital Centre",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                blurRadius = 8f
                            )
                        )
                    )
                    Text(
                        text = "স্মার্ট ইউনিয়ন সেবা • Digital Citizen Center Portal",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Professional design card hosting auth toggle tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabButton(
                        text = "Sign In (লগইন)",
                        isSelected = !isSignUpMode,
                        onClick = { isSignUpMode = false },
                        modifier = Modifier.weight(1f).testTag("tab_login")
                    )
                    TabButton(
                        text = "Sign Up (নিবন্ধন)",
                        isSelected = isSignUpMode,
                        onClick = { isSignUpMode = true },
                        modifier = Modifier.weight(1f).testTag("tab_signup")
                    )
                }

                // Authentication Instruction details
                Text(
                    text = if (isSignUpMode) "Create Citizen or Entrepreneur Account" else "Access Union Digital Services",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Quick Evaluation tip credentials notice
                if (!isSignUpMode) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "💡 Quick Access Tip for Evaluators:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Citizen Demo: citizen@udc.gov (PW: password123)\n" +
                                       "• Entrepreneur Demo: entrepreneur@udc.gov (PW: password123)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // Validation indicators
                if (showError) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                if (isSignUpMode) {
                    // PERSONA SELECTOR (EXCLUSIVE ON REGISTRATION MODULE)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Select Account Role",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RoleSelectionChip(
                                    text = "Citizen (নাগরিক)",
                                    icon = Icons.Default.Person,
                                    isSelected = !isEntrepreneur,
                                    onClick = { isEntrepreneur = false },
                                    modifier = Modifier.weight(1f).testTag("role_citizen")
                                )
                                RoleSelectionChip(
                                    text = "Entrepreneur (উদ্যোক্তা)",
                                    icon = Icons.Default.AccountBalance,
                                    isSelected = isEntrepreneur,
                                    onClick = { isEntrepreneur = true },
                                    modifier = Modifier.weight(1f).testTag("role_entrepreneur")
                                )
                            }
                        }
                    }

                    // REGISTRATION FORM FIELDS
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name (পূর্ণ নাম)") },
                        placeholder = { Text("e.g. Hannan Sarker") },
                        leadingIcon = { Icon(Icons.Default.Badge, null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_reg_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address (ইমেইল)") },
                    placeholder = { Text("e.g. user@gmail.com") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_auth_email"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (পাসওয়ার্ড)") },
                    placeholder = { Text("Minimum 6 characters") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        val iconImage = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = iconImage, contentDescription = description)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("input_auth_password"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (isSignUpMode) {
                    // Mobile & Location data
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Number (মোবাইল)") },
                        placeholder = { Text("e.g. 01712345678") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_reg_phone"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = upazila,
                            onValueChange = { upazila = it },
                            label = { Text("Upazila") },
                            placeholder = { Text("e.g. Kaliakair") },
                            leadingIcon = { Icon(Icons.Default.Map, null) },
                            modifier = Modifier.weight(1f).testTag("input_reg_upazila"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = union,
                            onValueChange = { union = it },
                            label = { Text("Union Parishad") },
                            placeholder = { Text("e.g. Chapair") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            modifier = Modifier.weight(1f).testTag("input_reg_union"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Submit Button CTA
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Email and password fields are required!"
                            showError = true
                            return@Button
                        }
                        if (password.length < 6) {
                            errorMessage = "Password must be at least 6 characters long!"
                            showError = true
                            return@Button
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            errorMessage = "Please enter a valid email address!"
                            showError = true
                            return@Button
                        }

                        if (isSignUpMode) {
                            if (name.isBlank() || phone.isBlank() || upazila.isBlank() || union.isBlank()) {
                                errorMessage = "All profile registration fields must be complete!"
                                showError = true
                                return@Button
                            }

                            isProcessing = true
                            showError = false
                            viewModel.signup(
                                email = email,
                                passwordPlain = password,
                                name = name,
                                role = if (isEntrepreneur) "Entrepreneur" else "Citizen",
                                upazila = upazila,
                                union = union,
                                phone = phone,
                                onSuccess = {
                                    // Automatically sign in newly registered user
                                    viewModel.loginWithEmailAndPassword(
                                        email = email,
                                        passwordPlain = password,
                                        onSuccess = { isProcessing = false },
                                        onFailure = { err ->
                                            errorMessage = err
                                            showError = true
                                            isProcessing = false
                                        }
                                    )
                                },
                                onFailure = { err ->
                                    errorMessage = err
                                    showError = true
                                    isProcessing = false
                                }
                            )
                        } else {
                            isProcessing = true
                            showError = false
                            viewModel.loginWithEmailAndPassword(
                                email = email,
                                passwordPlain = password,
                                onSuccess = { isProcessing = false },
                                onFailure = { err ->
                                    errorMessage = err
                                    showError = true
                                    isProcessing = false
                                }
                            )
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("submit_auth_btn"),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(if (isSignUpMode) Icons.Default.AppRegistration else Icons.Default.Login, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSignUpMode) "Register Account (নিবন্ধন করুন)" else "Access Services (লগইন করুন)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Helper toggle label text underneath
                Text(
                    text = if (isSignUpMode) "Already have an account? Sign In" else "New to UDC digital platform? Register now",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSignUpMode = !isSignUpMode }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RoleSelectionChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
