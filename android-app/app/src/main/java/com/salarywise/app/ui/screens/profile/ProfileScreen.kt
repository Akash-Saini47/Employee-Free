package com.salarywise.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salarywise.app.data.local.entity.UserEntity
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.domain.model.CurrencyFormatter
import com.salarywise.app.domain.model.PinSecurity
import com.salarywise.app.ui.components.ConfirmDeleteDialog
import com.salarywise.app.ui.theme.AlertRose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: SalaryWiseRepository) : ViewModel() {
    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    private val _exportedJson = MutableStateFlow<String?>(null)
    val exportedJson: StateFlow<String?> = _exportedJson.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collect {
                _user.value = it
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val u = _user.value ?: return@launch
            repository.updateDarkMode(u.id, enabled)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val u = _user.value ?: return@launch
            repository.updateNotifications(u.id, enabled)
        }
    }

    fun updatePin(pin: String?) {
        viewModelScope.launch {
            val u = _user.value ?: return@launch
            val stored = pin?.let { PinSecurity.hashPin(it) }
            repository.updatePin(u.id, stored)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            val u = _user.value ?: return@launch
            val json = repository.exportAllDataJson(u.id)
            _exportedJson.value = json
        }
    }

    fun clearExported() {
        _exportedJson.value = null
    }

    fun deleteAllData() {
        viewModelScope.launch {
            val u = _user.value ?: return@launch
            repository.deleteAllUserData(u.id)
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAccount()
            onComplete()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPinLock: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val userState by viewModel.user.collectAsState()
    val exportedJsonState by viewModel.exportedJson.collectAsState()

    var showDeleteDataDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val user = userState
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = user?.name ?: "Employee",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Monthly Salary: ${CurrencyFormatter.formatInr(user?.monthlyInHandSalary ?: 0.0)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Salary Date: ${user?.salaryDate ?: 1}st of month",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Preferences Section
            item {
                Text(
                    text = "App Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingToggleItem(
                            icon = Icons.Default.DarkMode,
                            title = "Dark Mode",
                            subtitle = "Switch between light and dark theme",
                            checked = user?.isDarkMode ?: false,
                            onCheckedChange = { viewModel.toggleDarkMode(it) }
                        )
                        Divider()
                        SettingToggleItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "Alerts for bills, budgets, and salary",
                            checked = user?.isNotificationsEnabled ?: true,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )
                        Divider()
                        SettingActionItem(
                            icon = Icons.Default.CurrencyRupee,
                            title = "Default Currency",
                            value = "INR (₹)"
                        )
                        Divider()
                        SettingActionItem(
                            icon = Icons.Default.CalendarToday,
                            title = "Date Format",
                            value = "DD/MM/YYYY"
                        )
                    }
                }
            }

            // Security & Privacy Section
            item {
                Text(
                    text = "Security & Privacy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingClickableItem(
                            icon = Icons.Default.Lock,
                            title = "App Lock PIN",
                            subtitle = if (user?.pinHash != null) "PIN Protection is Active" else "Not configured",
                            onClick = { showPinDialog = true }
                        )
                        Divider()
                        SettingClickableItem(
                            icon = Icons.Default.FileDownload,
                            title = "Export Financial Data",
                            subtitle = "Download all transactions and budgets as JSON",
                            onClick = { viewModel.exportData() }
                        )
                    }
                }
            }

            // Danger Zone
            item {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingClickableItem(
                            icon = Icons.Default.DeleteSweep,
                            title = "Delete All Financial Data",
                            subtitle = "Clear expenses, budgets, salaries & goals",
                            titleColor = AlertRose,
                            onClick = { showDeleteDataDialog = true }
                        )
                        Divider()
                        SettingClickableItem(
                            icon = Icons.Default.PersonRemove,
                            title = "Delete Account & Reset",
                            subtitle = "Wipe all local app data and start fresh",
                            titleColor = AlertRose,
                            onClick = { showDeleteAccountDialog = true }
                        )
                        Divider()
                        SettingClickableItem(
                            icon = Icons.Default.Info,
                            title = "About SalaryWise",
                            subtitle = "Version 1.1.0 • Financial Management for Employees",
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showDeleteDataDialog) {
        ConfirmDeleteDialog(
            title = "Delete All Financial Data?",
            message = "This will permanently remove all your recorded salaries, expenses, budgets, recurring bills, and savings contributions. This action cannot be undone.",
            onConfirm = {
                viewModel.deleteAllData()
                showDeleteDataDialog = false
            },
            onDismiss = { showDeleteDataDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        ConfirmDeleteDialog(
            title = "Delete Account & Reset?",
            message = "This will completely erase your profile and all financial information from this device.",
            onConfirm = {
                showDeleteAccountDialog = false
                viewModel.deleteAccount(onAccountDeleted)
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    if (showPinDialog) {
        SetPinDialog(
            onDismiss = { showPinDialog = false },
            onSavePin = { pin ->
                viewModel.updatePin(pin)
                showPinDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AboutSalaryWiseDialog(onDismiss = { showAboutDialog = false })
    }

    exportedJsonState?.let { json ->
        AlertDialog(
            onDismissRequest = { viewModel.clearExported() },
            title = { Text("Exported Data Preview", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Your financial data has been serialized into JSON format:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    ) {
                        Text(
                            text = json.take(500) + if (json.length > 500) "\n..." else "",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.clearExported() }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun SettingClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = titleColor)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = titleColor)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SetPinDialog(
    onDismiss: () -> Unit,
    onSavePin: (String?) -> Unit
) {
    var pinText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Security PIN", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Enter a 4-digit numeric PIN to protect your salary data, or leave blank to disable lock.")
                OutlinedTextField(
                    value = pinText,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinText = it },
                    label = { Text("4-digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSavePin(if (pinText.length == 4) pinText else null) }) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AboutSalaryWiseDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SalaryWise v1.1.0", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "SalaryWise is an Android financial management application designed specifically for employees.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Core Loop:\nTRACK → ANALYZE → PLAN → CONTROL → SAVE → ACHIEVE FINANCIAL GOALS",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "All financial data is stored securely on your device. Currency: INR (₹). Date format: DD/MM/YYYY.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}
