package com.salarywise.app.ui.screens.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.salarywise.app.data.local.entity.RecurringExpenseEntity
import com.salarywise.app.data.local.entity.UserEntity
import com.salarywise.app.data.repository.SalaryWiseRepository
import com.salarywise.app.domain.model.CurrencyFormatter
import com.salarywise.app.domain.model.DateUtils
import com.salarywise.app.domain.model.DefaultCategories
import com.salarywise.app.ui.components.ConfirmDeleteDialog
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class RecurringUiState(
    val user: UserEntity? = null,
    val list: List<RecurringExpenseEntity> = emptyList(),
    val totalMonthlyCommitment: Double = 0.0,
    val isLoading: Boolean = true
)

class RecurringViewModel(private val repository: SalaryWiseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getCurrentUserFlow().collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }
                repository.getAllRecurringExpensesFlow(user.id).collect { items ->
                    val monthlySum = items.filter { it.isActive }.sumOf {
                        when (it.frequency.uppercase()) {
                            "WEEKLY" -> it.amount * 4
                            "QUARTERLY" -> it.amount / 3
                            "YEARLY" -> it.amount / 12
                            else -> it.amount
                        }
                    }
                    _uiState.update {
                        it.copy(
                            user = user,
                            list = items,
                            totalMonthlyCommitment = monthlySum,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun addRecurring(
        name: String,
        amount: Double,
        category: String,
        frequency: String,
        dueDate: Long
    ) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            val item = RecurringExpenseEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                name = name,
                amount = amount,
                categoryName = category,
                frequency = frequency,
                nextDueDate = dueDate
            )
            repository.saveRecurringExpense(item)
        }
    }

    fun payBill(item: RecurringExpenseEntity) {
        viewModelScope.launch {
            repository.payRecurringBill(item)
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            repository.deleteRecurringExpense(id)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpenseScreen(
    viewModel: RecurringViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDeleteId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Expenses & Bills", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recurring Bill")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Total Monthly Obligations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatInr(state.totalMonthlyCommitment),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${state.list.count { it.isActive }} active subscriptions & recurring bills",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Scheduled Bills",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.list.isEmpty()) {
                item {
                    Text(
                        text = "No recurring bills set up. Add Rent, EMIs, Utilities, Subscriptions, etc.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(state.list, key = { it.id }) { bill ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bill.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = bill.categoryName,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = bill.frequency,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Next Due: ${DateUtils.formatDate(bill.nextDueDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyFormatter.formatInr(bill.amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row {
                                OutlinedButton(
                                    onClick = { viewModel.payBill(bill) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Pay Now", fontSize = 11.sp)
                                }
                                IconButton(onClick = { itemToDeleteId = bill.id }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showAddDialog) {
        AddRecurringDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, amount, cat, freq, due ->
                viewModel.addRecurring(name, amount, cat, freq, due)
                showAddDialog = false
            }
        )
    }

    if (itemToDeleteId != null) {
        ConfirmDeleteDialog(
            title = "Delete Recurring Bill",
            message = "Are you sure you want to delete this recurring bill schedule?",
            onConfirm = {
                itemToDeleteId?.let { viewModel.deleteItem(it) }
                itemToDeleteId = null
            },
            onDismiss = { itemToDeleteId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, amount: Double, category: String, frequency: String, dueDate: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Bills") }
    var frequency by remember { mutableStateOf("MONTHLY") }
    var dateText by remember { mutableStateOf(DateUtils.formatDate(System.currentTimeMillis() + 86400000L * 7)) }

    val presetNames = listOf("Rent", "EMI", "Netflix", "Internet", "Mobile Recharge", "Electricity", "Health Insurance")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recurring Bill", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bill / Expense Name") },
                    placeholder = { Text("e.g. Rent, Netflix") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick presets
                Text("Popular presets:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetNames.take(4).forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { name = p }
                        ) {
                            Text(p, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Next Due Date (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amt > 0) {
                        val due = DateUtils.parseDate(dateText) ?: System.currentTimeMillis()
                        onAdd(name.trim(), amt, selectedCategory, frequency, due)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
