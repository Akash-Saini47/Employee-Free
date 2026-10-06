/**
 * SalaryWise - Android Mobile App Logic & Simulator
 * Personal Salary & Financial Management Application for Employees
 */

// --- Default Data & Seeder ---
const DEFAULT_USER = {
    name: "Rahul Sharma",
    monthlySalary: 40000,
    salaryDate: 1,
    currentSavings: 60000,
    essentialExpenses: 20000,
    monthlySavingsTarget: 15500,
    emergencyFundTarget: 120000,
    pin: null,
    isDarkMode: false,
    notificationsEnabled: true,
    isOnboardingDone: true
};

const DEFAULT_SALARIES = [
    { id: "sal-1", monthYear: "2026-04", grossSalary: 38000, deductions: 3000, inHandSalary: 35000, paymentDate: "01/04/2026", notes: "Regular" },
    { id: "sal-2", monthYear: "2026-05", grossSalary: 38000, deductions: 3000, inHandSalary: 35000, paymentDate: "01/05/2026", notes: "Regular" },
    { id: "sal-3", monthYear: "2026-06", grossSalary: 42000, deductions: 4000, inHandSalary: 38000, paymentDate: "01/06/2026", notes: "Appraisal increment" },
    { id: "sal-4", monthYear: "2026-07", grossSalary: 45000, deductions: 5000, inHandSalary: 40000, paymentDate: "01/07/2026", notes: "Revised payroll" },
    { id: "sal-5", monthYear: "2026-10", grossSalary: 45000, deductions: 5000, inHandSalary: 40000, paymentDate: "01/10/2026", notes: "October salary" }
];

const DEFAULT_CATEGORIES = [
    { id: "cat-1", name: "Rent", allocated: 12000, isCustom: false },
    { id: "cat-2", name: "Food", allocated: 5000, isCustom: false },
    { id: "cat-3", name: "Groceries", allocated: 4000, isCustom: false },
    { id: "cat-4", name: "Transportation", allocated: 2000, isCustom: false },
    { id: "cat-5", name: "Utilities", allocated: 2000, isCustom: false },
    { id: "cat-6", name: "Bills", allocated: 1500, isCustom: false },
    { id: "cat-7", name: "Shopping", allocated: 3000, isCustom: false },
    { id: "cat-8", name: "Entertainment", allocated: 2000, isCustom: false },
    { id: "cat-9", name: "Healthcare", allocated: 1500, isCustom: false },
    { id: "cat-10", name: "Education", allocated: 1000, isCustom: false },
    { id: "cat-11", name: "EMI", allocated: 3000, isCustom: false },
    { id: "cat-12", name: "Investments", allocated: 4000, isCustom: false },
    { id: "cat-13", name: "Savings", allocated: 5000, isCustom: false },
    { id: "cat-14", name: "Other", allocated: 1000, isCustom: false }
];

const DEFAULT_EXPENSES = [
    { id: "exp-1", amount: 12000, category: "Rent", date: "01/10/2026", paymentMethod: "Bank Transfer", description: "Apartment Rent", isRecurring: true },
    { id: "exp-2", amount: 3800, category: "Food", date: "05/10/2026", paymentMethod: "UPI", description: "Dining & Swiggy", isRecurring: false },
    { id: "exp-3", amount: 3200, category: "Groceries", date: "03/10/2026", paymentMethod: "Debit Card", description: "Supermarket provisions", isRecurring: false },
    { id: "exp-4", amount: 2520, category: "Shopping", date: "04/10/2026", paymentMethod: "Credit Card", description: "Weekend apparel sale", isRecurring: false },
    { id: "exp-5", amount: 1500, category: "Transportation", date: "02/10/2026", paymentMethod: "UPI", description: "Metro card & Petrol", isRecurring: false },
    { id: "exp-6", amount: 1480, category: "Bills", date: "04/10/2026", paymentMethod: "UPI", description: "Electricity & Wifi", isRecurring: true }
];

const DEFAULT_RECURRING = [
    { id: "rec-1", name: "Apartment Rent", amount: 12000, category: "Rent", frequency: "Monthly", dueDate: "01/11/2026", isActive: true },
    { id: "rec-2", name: "Electricity Bill", amount: 1850, category: "Bills", frequency: "Monthly", dueDate: "08/10/2026", isActive: true },
    { id: "rec-3", name: "Netflix Subscription", amount: 649, category: "Entertainment", frequency: "Monthly", dueDate: "15/10/2026", isActive: true },
    { id: "rec-4", name: "Airtel Fiber Broadband", amount: 799, category: "Utilities", frequency: "Monthly", dueDate: "18/10/2026", isActive: true },
    { id: "rec-5", name: "Personal Loan EMI", amount: 3000, category: "EMI", frequency: "Monthly", dueDate: "10/10/2026", isActive: true }
];

const DEFAULT_GOALS = [
    { id: "goal-1", title: "Emergency Fund", targetAmount: 120000, currentAmount: 60000, monthlyContribution: 5000, isEmergencyFund: true },
    { id: "goal-2", title: "New Laptop", targetAmount: 80000, currentAmount: 35000, monthlyContribution: 4000, isEmergencyFund: false },
    { id: "goal-3", title: "Vacation Fund", targetAmount: 40000, currentAmount: 16000, monthlyContribution: 3000, isEmergencyFund: false }
];

const DEFAULT_NOTIFICATIONS = [
    { id: "n-1", title: "Salary Received! 🎉", message: "Monthly salary of ₹40,000 has been credited to your bank account.", time: "Today, 09:30 AM", type: "SALARY" },
    { id: "n-2", title: "Budget Warning: Shopping", message: "You have used 84% of your Shopping budget (₹2,520 of ₹3,000).", time: "Yesterday, 04:15 PM", type: "BUDGET_WARN" },
    { id: "n-3", title: "Upcoming Bill: Electricity", message: "Electricity bill of ₹1,850 is due on 08/10/2026.", time: "2 days ago", type: "BILL" }
];

// --- State Storage ---
class AppStore {
    constructor() {
        this.load();
    }

    load() {
        try {
            const rawUser = localStorage.getItem("salarywise_user");
            if (!rawUser) {
                this.resetToDefaults();
                return;
            }
            this.user = JSON.parse(rawUser);
            this.salaries = JSON.parse(localStorage.getItem("salarywise_salaries") || "[]");
            this.categories = JSON.parse(localStorage.getItem("salarywise_categories") || "[]");
            this.expenses = JSON.parse(localStorage.getItem("salarywise_expenses") || "[]");
            this.recurring = JSON.parse(localStorage.getItem("salarywise_recurring") || "[]");
            this.goals = JSON.parse(localStorage.getItem("salarywise_goals") || "[]");
            this.notifications = JSON.parse(localStorage.getItem("salarywise_notifications") || "[]");
        } catch (e) {
            console.error("Storage load failed, resetting:", e);
            this.resetToDefaults();
        }
    }

    save() {
        localStorage.setItem("salarywise_user", JSON.stringify(this.user));
        localStorage.setItem("salarywise_salaries", JSON.stringify(this.salaries));
        localStorage.setItem("salarywise_categories", JSON.stringify(this.categories));
        localStorage.setItem("salarywise_expenses", JSON.stringify(this.expenses));
        localStorage.setItem("salarywise_recurring", JSON.stringify(this.recurring));
        localStorage.setItem("salarywise_goals", JSON.stringify(this.goals));
        localStorage.setItem("salarywise_notifications", JSON.stringify(this.notifications));
    }

    resetToDefaults() {
        this.user = JSON.parse(JSON.stringify(DEFAULT_USER));
        this.salaries = JSON.parse(JSON.stringify(DEFAULT_SALARIES));
        this.categories = JSON.parse(JSON.stringify(DEFAULT_CATEGORIES));
        this.expenses = JSON.parse(JSON.stringify(DEFAULT_EXPENSES));
        this.recurring = JSON.parse(JSON.stringify(DEFAULT_RECURRING));
        this.goals = JSON.parse(JSON.stringify(DEFAULT_GOALS));
        this.notifications = JSON.parse(JSON.stringify(DEFAULT_NOTIFICATIONS));
        this.save();
    }
}

const store = new AppStore();

// --- Formatting Helpers ---
function formatINR(val) {
    if (val === undefined || val === null || isNaN(val)) val = 0;
    const isNeg = val < 0;
    const absVal = Math.round(Math.abs(val));
    // Indian currency comma separation
    const str = absVal.toString();
    let result = '';
    if (str.length > 3) {
        const lastThree = str.substring(str.length - 3);
        const otherNumbers = str.substring(0, str.length - 3);
        result = otherNumbers.replace(/\B(?=(\d{2})+(?!\d))/g, ",") + "," + lastThree;
    } else {
        result = str;
    }
    return (isNeg ? "-" : "") + "₹" + result;
}

function getTodayDDMMYYYY() {
    const d = new Date();
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}/${month}/${year}`;
}

// --- Calculations (Section 17) ---
function computeMetrics() {
    const salary = store.user.monthlySalary || 40000;
    const totalExpenses = store.expenses.reduce((sum, exp) => sum + Number(exp.amount), 0);
    const totalSaved = Math.max(0, salary - totalExpenses);
    const remainingMoney = salary - totalExpenses;
    const savingsRate = salary > 0 ? (totalSaved / salary) * 100 : 0;

    const totalAllocatedBudget = store.categories.reduce((sum, cat) => sum + Number(cat.allocated), 0);
    const budgetUsed = totalAllocatedBudget > 0 ? (totalExpenses / totalAllocatedBudget) * 100 : 0;

    // Category usage
    const catUsage = {};
    store.categories.forEach(cat => {
        catUsage[cat.name] = { allocated: Number(cat.allocated), spent: 0 };
    });
    store.expenses.forEach(exp => {
        if (!catUsage[exp.category]) {
            catUsage[exp.category] = { allocated: 1000, spent: 0 };
        }
        catUsage[exp.category].spent += Number(exp.amount);
    });

    const categoryStatuses = Object.keys(catUsage).map(name => {
        const item = catUsage[name];
        const remaining = item.allocated - item.spent;
        const pct = item.allocated > 0 ? (item.spent / item.allocated) * 100 : 0;
        return {
            name,
            allocated: item.allocated,
            spent: item.spent,
            remaining,
            pct,
            isWarning: pct >= 80 && pct <= 100,
            isExceeded: pct > 100
        };
    });

    // Emergency Fund Coverage
    const efGoal = store.goals.find(g => g.isEmergencyFund) || store.goals[0];
    const efCurrent = efGoal ? efGoal.currentAmount : store.user.currentSavings;
    const essentials = store.user.essentialExpenses > 0 ? store.user.essentialExpenses : (salary * 0.5);
    const efMonths = essentials > 0 ? (efCurrent / essentials) : 0;

    return {
        salary,
        totalExpenses,
        totalSaved,
        remainingMoney,
        savingsRate,
        totalAllocatedBudget,
        budgetUsed,
        categoryStatuses,
        efGoal,
        efCurrent,
        essentials,
        efMonths
    };
}

// --- Navigation & Routing ---
let currentScreen = "home";

function navigateTo(screenId) {
    document.querySelectorAll(".screen").forEach(s => s.classList.remove("active"));
    const target = document.getElementById("screen" + capitalize(screenId));
    if (target) {
        target.classList.add("active");
        currentScreen = screenId;
        window.scrollTo(0, 0);
        document.getElementById("appViewport").scrollTop = 0;
    }

    // Update bottom nav
    document.querySelectorAll(".nav-item").forEach(item => {
        if (item.dataset.nav === screenId) {
            item.classList.add("active");
        } else {
            item.classList.remove("active");
        }
    });

    // Render screen contents
    renderActiveScreen(screenId);
}

function capitalize(s) {
    if (!s) return "";
    return s.charAt(0).toUpperCase() + s.slice(1);
}

function renderActiveScreen(screenId) {
    switch (screenId) {
        case "home":
            renderHomeScreen();
            break;
        case "expenses":
            renderExpensesScreen();
            break;
        case "budget":
            renderBudgetScreen();
            break;
        case "analytics":
            renderAnalyticsScreen();
            break;
        case "goals":
            renderGoalsScreen();
            break;
        case "salary":
            renderSalaryScreen();
            break;
        case "recurring":
            renderRecurringScreen();
            break;
        case "stability":
            renderStabilityScreen();
            break;
        case "report":
            renderReportScreen();
            break;
        case "health":
            renderHealthScoreScreen();
            break;
        case "notifications":
            renderNotificationsScreen();
            break;
        case "profile":
            renderProfileScreen();
            break;
    }
    lucide.createIcons();
}

// --- SCREEN RENDERERS ---

// 1. HOME SCREEN
function renderHomeScreen() {
    const m = computeMetrics();

    document.getElementById("topUserGreeting").textContent = `Hello, ${store.user.name || "Employee"}`;
    document.getElementById("homeSalary").textContent = formatINR(m.salary);
    document.getElementById("homeExpenses").textContent = formatINR(m.totalExpenses);
    document.getElementById("homeSaved").textContent = formatINR(m.totalSaved);
    document.getElementById("homeRemaining").textContent = formatINR(m.remainingMoney);
    document.getElementById("homeSavingsRate").textContent = m.savingsRate.toFixed(1) + "%";
    document.getElementById("homeBudgetUsedText").textContent = Math.round(m.budgetUsed) + "%";
    document.getElementById("homeBudgetBar").style.width = Math.min(100, m.budgetUsed) + "%";

    // Budget Warnings (Section 18)
    const warnContainer = document.getElementById("homeWarningContainer");
    warnContainer.innerHTML = "";

    if (m.totalExpenses > m.salary && m.salary > 0) {
        warnContainer.innerHTML += `
            <div class="alert-banner critical">
                <i data-lucide="alert-triangle"></i>
                <span>Your expenses are higher than your income this month!</span>
            </div>
        `;
    }

    const warnings = m.categoryStatuses.filter(c => c.isWarning || c.isExceeded);
    warnings.forEach(w => {
        if (w.isExceeded) {
            warnContainer.innerHTML += `
                <div class="alert-banner critical">
                    <i data-lucide="alert-circle"></i>
                    <span>You have exceeded your ${w.name} budget (${formatINR(w.spent)} / ${formatINR(w.allocated)})</span>
                </div>
            `;
        } else if (w.isWarning) {
            warnContainer.innerHTML += `
                <div class="alert-banner warning">
                    <i data-lucide="alert-triangle"></i>
                    <span>Warning: ${w.name} has reached ${Math.round(w.pct)}% of budget</span>
                </div>
            `;
        }
    });

    // Smart Insight
    const insightText = document.getElementById("homeInsightText");
    if (warnings.find(w => w.name === "Shopping" && w.pct >= 80)) {
        insightText.textContent = `You have used ${Math.round(warnings.find(w => w.name === "Shopping").pct)}% of your shopping budget.`;
    } else if (m.savingsRate >= 35) {
        insightText.textContent = `Your savings rate increased to ${m.savingsRate.toFixed(1)}%. Excellent financial momentum!`;
    } else if (m.totalExpenses > m.salary) {
        insightText.textContent = "Your expenses are higher than your income this month. Prioritize cutting non-essentials.";
    } else {
        insightText.textContent = "Your emergency fund covers approximately " + m.efMonths.toFixed(1) + " months of essential expenses.";
    }

    // Emergency Fund Card
    if (m.efGoal) {
        document.getElementById("homeEFSubtitle").textContent = `${formatINR(m.efGoal.currentAmount)} / ${formatINR(m.efGoal.targetAmount)}`;
        const efPct = Math.round((m.efGoal.currentAmount / m.efGoal.targetAmount) * 100);
        document.getElementById("homeEFPct").textContent = efPct + "%";
        document.getElementById("homeEFBar").style.width = Math.min(100, efPct) + "%";
        document.getElementById("homeEFCoverageTip").innerHTML = `<i data-lucide="shield-check"></i> <span>Covers approximately ${m.efMonths.toFixed(1)} months of essential living expenses</span>`;
    }

    // Upcoming Recurring Bills (Upcoming within 14 days)
    const billsList = document.getElementById("homeUpcomingBillsList");
    billsList.innerHTML = "";
    const activeBills = store.recurring.filter(b => b.isActive).slice(0, 3);
    if (activeBills.length === 0) {
        billsList.innerHTML = `<span style="font-size:12px;color:var(--text-secondary);">No upcoming recurring bills.</span>`;
    } else {
        activeBills.forEach(bill => {
            billsList.innerHTML += `
                <div class="upcoming-bill-row">
                    <div class="bill-name-due">
                        <strong>${bill.name}</strong>
                        <span>Due: ${bill.dueDate} • ${bill.category}</span>
                    </div>
                    <div class="bill-amount-action">
                        <strong>${formatINR(bill.amount)}</strong>
                        <button class="pay-btn" onclick="payBillDirectly('${bill.id}')">Pay</button>
                    </div>
                </div>
            `;
        });
    }

    // Health Score
    document.getElementById("homeHealthScoreNum").textContent = "78";
}

// 2. SALARY SCREEN
function renderSalaryScreen() {
    const listContainer = document.getElementById("salaryRecordsList");
    listContainer.innerHTML = "";

    const sortedSalaries = [...store.salaries].sort((a, b) => b.monthYear.localeCompare(a.monthYear));
    const latest = sortedSalaries[0] || { inHandSalary: store.user.monthlySalary };

    document.getElementById("salaryCurrentInHand").textContent = formatINR(latest.inHandSalary);

    // Calculate growth over time
    const oldest = sortedSalaries[sortedSalaries.length - 1];
    if (oldest && oldest.inHandSalary > 0 && latest.inHandSalary > oldest.inHandSalary) {
        const growth = ((latest.inHandSalary - oldest.inHandSalary) / oldest.inHandSalary) * 100;
        document.getElementById("salaryGrowthTag").textContent = `+${growth.toFixed(1)}% Growth over recorded history`;
    }

    sortedSalaries.forEach(sal => {
        listContainer.innerHTML += `
            <div class="upcoming-bill-row" style="margin-bottom:8px;padding:12px;">
                <div class="bill-name-due">
                    <strong>${formatMonthYearReadable(sal.monthYear)}</strong>
                    <span>Paid on ${sal.paymentDate} ${sal.notes ? "• " + sal.notes : ""}</span>
                    ${sal.deductions > 0 ? `<div style="font-size:11px;color:var(--rose)">Gross: ${formatINR(sal.grossSalary)} | Deductions: -${formatINR(sal.deductions)}</div>` : ''}
                </div>
                <div class="bill-amount-action">
                    <strong style="color:var(--primary);">${formatINR(sal.inHandSalary)}</strong>
                    <button class="delete-action-btn" onclick="deleteSalaryRecord('${sal.id}')">
                        <i data-lucide="trash-2"></i>
                    </button>
                </div>
            </div>
        `;
    });

    renderSalaryGrowthChart();
}

function formatMonthYearReadable(my) {
    if (!my) return "";
    const parts = my.split("-");
    if (parts.length < 2) return my;
    const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    const mIdx = parseInt(parts[1], 10) - 1;
    return `${months[mIdx] || parts[1]} ${parts[0]}`;
}

let salaryChartInstance = null;
function renderSalaryGrowthChart() {
    const ctx = document.getElementById("salaryGrowthChart");
    if (!ctx) return;
    if (salaryChartInstance) salaryChartInstance.destroy();

    const chronological = [...store.salaries].sort((a, b) => a.monthYear.localeCompare(b.monthYear));
    const labels = chronological.map(s => formatMonthYearReadable(s.monthYear));
    const data = chronological.map(s => s.inHandSalary);

    salaryChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'In-Hand Salary (₹)',
                data: data,
                backgroundColor: '#0d9488',
                borderRadius: 8
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                y: {
                    beginAtZero: false,
                    ticks: { callback: v => '₹' + (v / 1000) + 'k' }
                }
            }
        }
    });
}

// 3. BUDGET SCREEN
function renderBudgetScreen() {
    const m = computeMetrics();

    document.getElementById("budgetTotalAllocated").textContent = formatINR(m.totalAllocatedBudget);
    document.getElementById("budgetTotalSpent").textContent = formatINR(m.totalExpenses);
    document.getElementById("budgetTotalRemaining").textContent = formatINR(m.totalAllocatedBudget - m.totalExpenses);

    const alertsContainer = document.getElementById("budgetAlertsContainer");
    alertsContainer.innerHTML = "";

    const exceeded = m.categoryStatuses.filter(c => c.isExceeded);
    const warnings = m.categoryStatuses.filter(c => c.isWarning);

    if (exceeded.length > 0) {
        alertsContainer.innerHTML += `
            <div class="alert-banner critical" style="margin-bottom:12px;">
                <i data-lucide="alert-circle"></i>
                <span>You have exceeded your budget for ${exceeded.map(e => e.name).join(", ")}!</span>
            </div>
        `;
    }
    if (warnings.length > 0) {
        alertsContainer.innerHTML += `
            <div class="alert-banner warning" style="margin-bottom:12px;">
                <i data-lucide="alert-triangle"></i>
                <span>Warning: ${warnings.map(w => w.name).join(", ")} has reached >= 80% of budget limit.</span>
            </div>
        `;
    }

    const catList = document.getElementById("budgetCategoriesList");
    catList.innerHTML = "";

    m.categoryStatuses.forEach(cat => {
        let badgeHtml = '';
        let borderClass = '';
        if (cat.isExceeded) {
            badgeHtml = `<span class="cat-badge danger">Exceeded</span>`;
            borderClass = 'alert-border';
        } else if (cat.isWarning) {
            badgeHtml = `<span class="cat-badge warn">80% Limit</span>`;
            borderClass = 'warning-border';
        }

        const barColorClass = cat.isExceeded ? 'rose' : (cat.isWarning ? 'amber' : 'emerald');

        catList.innerHTML += `
            <div class="budget-cat-card ${borderClass}">
                <div class="cat-top-row">
                    <div class="cat-title-badge">
                        <strong>${cat.name}</strong>
                        ${badgeHtml}
                    </div>
                    <span style="font-size:12px;font-weight:700;">Used: ${Math.round(cat.pct)}%</span>
                </div>
                <div class="cat-numbers">
                    <span>Budget: ${formatINR(cat.allocated)}</span>
                    <span>Spent: ${formatINR(cat.spent)}</span>
                    <span style="color:${cat.remaining < 0 ? 'var(--rose)' : 'var(--emerald)'}">Rem: ${formatINR(cat.remaining)}</span>
                </div>
                <div class="progress-bar-container">
                    <div class="progress-bar-fill ${barColorClass}" style="width: ${Math.min(100, cat.pct)}%;"></div>
                </div>
            </div>
        `;
    });
}

// 4. EXPENSES SCREEN
let currentExpenseFilter = "all";
let currentSortDesc = true;

function renderExpensesScreen() {
    // Populate category dropdown filter if empty
    const catSelect = document.getElementById("expenseCategoryFilter");
    catSelect.innerHTML = `<option value="">All Categories</option>`;
    store.categories.forEach(c => {
        catSelect.innerHTML += `<option value="${c.name}">${c.name}</option>`;
    });

    filterAndRenderExpenses();
}

function filterAndRenderExpenses() {
    const searchVal = document.getElementById("expenseSearchInput").value.toLowerCase();
    const catVal = document.getElementById("expenseCategoryFilter").value;
    const methodVal = document.getElementById("expenseMethodFilter").value;

    let filtered = [...store.expenses];

    // Filter by text
    if (searchVal) {
        filtered = filtered.filter(e => 
            (e.description && e.description.toLowerCase().includes(searchVal)) ||
            (e.category && e.category.toLowerCase().includes(searchVal))
        );
    }

    // Filter by Category
    if (catVal) {
        filtered = filtered.filter(e => e.category === catVal);
    }

    // Filter by Payment Method
    if (methodVal) {
        filtered = filtered.filter(e => e.paymentMethod === methodVal);
    }

    // Sorting
    filtered.sort((a, b) => {
        return currentSortDesc ? (b.amount - a.amount) : (a.amount - b.amount);
    });

    const sum = filtered.reduce((s, e) => s + Number(e.amount), 0);
    document.getElementById("expenseCountText").textContent = `${filtered.length} expenses found`;
    document.getElementById("expenseTotalFiltered").textContent = formatINR(sum);

    const container = document.getElementById("expenseCardsList");
    container.innerHTML = "";

    if (filtered.length === 0) {
        container.innerHTML = `
            <div style="text-align:center;padding:30px;color:var(--text-secondary);">
                <i data-lucide="receipt-text" style="width:40px;height:40px;margin-bottom:8px;"></i>
                <p>No matching expenses found.</p>
            </div>
        `;
        lucide.createIcons();
        return;
    }

    filtered.forEach(exp => {
        container.innerHTML += `
            <div class="expense-item">
                <div class="desc">
                    <strong>${exp.description || exp.category}</strong>
                    <div class="expense-meta-tags">
                        <span class="mini-tag">${exp.category}</span>
                        <span class="mini-tag">${exp.paymentMethod}</span>
                        <span class="mini-tag">${exp.date}</span>
                        ${exp.isRecurring ? '<span class="mini-tag" style="color:var(--primary)">Recurring</span>' : ''}
                    </div>
                </div>
                <div class="cost">
                    <strong>-${formatINR(exp.amount)}</strong>
                    <button class="delete-action-btn" onclick="deleteExpense('${exp.id}')">
                        <i data-lucide="trash-2"></i>
                    </button>
                </div>
            </div>
        `;
    });
    lucide.createIcons();
}

// 5. RECURRING EXPENSES SCREEN
function renderRecurringScreen() {
    const list = document.getElementById("recurringItemsList");
    list.innerHTML = "";

    let totalMonthly = 0;
    store.recurring.forEach(item => {
        if (item.isActive) {
            let mult = 1;
            if (item.frequency === "Weekly") mult = 4;
            if (item.frequency === "Quarterly") mult = 1 / 3;
            if (item.frequency === "Yearly") mult = 1 / 12;
            totalMonthly += item.amount * mult;
        }

        list.innerHTML += `
            <div class="upcoming-bill-row" style="margin-bottom:10px;padding:12px;">
                <div class="bill-name-due">
                    <strong>${item.name}</strong>
                    <span>Due: ${item.dueDate} • ${item.category} • ${item.frequency}</span>
                </div>
                <div class="bill-amount-action">
                    <strong>${formatINR(item.amount)}</strong>
                    <button class="pay-btn" onclick="payBillDirectly('${item.id}')">Pay Now</button>
                    <button class="delete-action-btn" onclick="deleteRecurringItem('${item.id}')">
                        <i data-lucide="trash-2"></i>
                    </button>
                </div>
            </div>
        `;
    });

    document.getElementById("recurringTotalSum").textContent = formatINR(totalMonthly);
    lucide.createIcons();
}

// 6. SAVINGS GOALS SCREEN
function renderGoalsScreen() {
    const m = computeMetrics();
    const totalGoalsSaved = store.goals.reduce((s, g) => s + Number(g.currentAmount), 0);
    document.getElementById("savingsTotalHero").textContent = formatINR(totalGoalsSaved);
    document.getElementById("savingsTargetHero").textContent = formatINR(store.user.monthlySavingsTarget);

    const list = document.getElementById("savingsGoalsList");
    list.innerHTML = "";

    store.goals.forEach(goal => {
        const pct = Math.round((goal.currentAmount / goal.targetAmount) * 100);
        const remaining = Math.max(0, goal.targetAmount - goal.currentAmount);
        const monthsLeft = goal.monthlyContribution > 0 ? Math.ceil(remaining / goal.monthlyContribution) : 0;

        list.innerHTML += `
            <div class="section-card">
                <div class="section-card-header">
                    <div>
                        <h3>${goal.title} ${goal.isEmergencyFund ? '<span class="percent-chip" style="font-size:10px;">Primary Shield</span>' : ''}</h3>
                        <span class="sub-text">${formatINR(goal.currentAmount)} / ${formatINR(goal.targetAmount)}</span>
                    </div>
                    <span class="percent-chip">${pct}%</span>
                </div>
                <div class="progress-bar-container" style="margin: 8px 0 10px;">
                    <div class="progress-bar-fill emerald" style="width: ${Math.min(100, pct)}%;"></div>
                </div>
                <div style="display:flex;justify-content:space-between;align-items:center;font-size:12px;">
                    <span style="color:var(--text-secondary)">Remaining: ${formatINR(remaining)} (${monthsLeft > 0 ? `~${monthsLeft} mo` : 'Done!'})</span>
                    <div>
                        <button class="pill-btn primary" onclick="openDepositModal('${goal.id}', '${goal.title}')">+ Add Money</button>
                        ${!goal.isEmergencyFund ? `<button class="delete-action-btn" onclick="deleteGoal('${goal.id}')"><i data-lucide="trash-2"></i></button>` : ''}
                    </div>
                </div>
            </div>
        `;
    });
    lucide.createIcons();
}

// 7. FINANCIAL FREEDOM / STABILITY SCREEN
function renderStabilityScreen() {
    const m = computeMetrics();

    document.getElementById("stabilityMonthsCount").textContent = `${m.efMonths.toFixed(1)} Months`;
    document.getElementById("stabilityExplanation").textContent = `"Your emergency fund currently covers approximately ${m.efMonths.toFixed(1)} months of essential expenses."`;

    const coveragePct = Math.min(100, (m.efMonths / 6) * 100);
    document.getElementById("stabilityCoverageBar").style.width = coveragePct + "%";

    let badgeText = "Starter Buffer";
    if (m.efMonths >= 12) badgeText = "High Resilience";
    else if (m.efMonths >= 6) badgeText = "Financial Stability";
    else if (m.efMonths >= 3) badgeText = "Essential Security";
    document.getElementById("stabilityStageBadge").textContent = badgeText;

    document.getElementById("indEmergencyFund").textContent = formatINR(m.efCurrent);
    document.getElementById("indEssentialExpenses").textContent = formatINR(m.essentials);
    document.getElementById("indSavingsRate").textContent = m.savingsRate.toFixed(1) + "%";

    // Debt
    const totalDebt = store.recurring.filter(r => r.category === "EMI" || r.name.toLowerCase().includes("loan")).reduce((s, r) => s + r.amount, 0);
    const dti = m.salary > 0 ? (totalDebt / m.salary) * 100 : 0;
    document.getElementById("indDebtToIncome").textContent = `${dti.toFixed(0)}% (${formatINR(totalDebt)})`;

    let advice = "";
    if (m.efMonths < 3) {
        advice = `Focus all discretionary surplus into your emergency buffer until you reach at least 3 months (${formatINR(m.essentials * 3)}). This safeguards against sudden job changes or medical surprises.`;
    } else if (m.efMonths < 6) {
        advice = `Good progress! Continue steadily building towards the 6-month benchmark (${formatINR(m.essentials * 6)}). Avoid taking new loans or unnecessary EMIs.`;
    } else {
        advice = `Your safety cushion is well established at 6+ months. You are in a strong position to systematically fund long-term wealth assets and specific personal goals.`;
    }
    document.getElementById("stabilityActionPlan").textContent = advice;
}

// 8. ANALYTICS SCREEN & CHARTS
let incomeVsExpChartInstance = null;
let categoryDonutChartInstance = null;
let budgetVsActualChartInstance = null;
let spendingTrendChartInstance = null;

function renderAnalyticsScreen() {
    const m = computeMetrics();

    document.getElementById("anAvgExpense").textContent = formatINR(m.totalExpenses);
    document.getElementById("anAvgSavings").textContent = formatINR(m.totalSaved);

    const highestExpense = store.expenses.reduce((max, e) => e.amount > (max ? max.amount : 0) ? e : max, null);
    document.getElementById("anHighestExpense").textContent = highestExpense ? formatINR(highestExpense.amount) : "₹0";

    const topCat = [...m.categoryStatuses].sort((a, b) => b.spent - a.spent)[0];
    document.getElementById("anTopCategory").textContent = topCat ? `${topCat.name} (${formatINR(topCat.spent)})` : "None";

    renderAnalyticsCharts(m);
}

function renderAnalyticsCharts(m) {
    // 1. Income vs Expenses Bar Chart
    const ctx1 = document.getElementById("incomeVsExpensesChart");
    if (ctx1) {
        if (incomeVsExpChartInstance) incomeVsExpChartInstance.destroy();
        incomeVsExpChartInstance = new Chart(ctx1, {
            type: 'bar',
            data: {
                labels: ['Income', 'Expenses', 'Saved'],
                datasets: [{
                    data: [m.salary, m.totalExpenses, m.totalSaved],
                    backgroundColor: ['#10b981', '#f43f5e', '#0d9488'],
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: false } },
                scales: { y: { ticks: { callback: v => '₹' + (v / 1000) + 'k' } } }
            }
        });
    }

    // 2. Category Donut Chart
    const ctx2 = document.getElementById("categoryDonutChart");
    if (ctx2) {
        if (categoryDonutChartInstance) categoryDonutChartInstance.destroy();
        const activeCats = m.categoryStatuses.filter(c => c.spent > 0);
        const labels = activeCats.map(c => c.name);
        const data = activeCats.map(c => c.spent);
        const palette = ['#0d9488', '#0284c7', '#f59e0b', '#f43f5e', '#8b5cf6', '#10b981', '#ec4899', '#6366f1'];

        categoryDonutChartInstance = new Chart(ctx2, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: palette.slice(0, labels.length)
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }

    // 3. Budget vs Actual Grouped Bar Chart
    const ctx3 = document.getElementById("budgetVsActualChart");
    if (ctx3) {
        if (budgetVsActualChartInstance) budgetVsActualChartInstance.destroy();
        const cats = m.categoryStatuses.slice(0, 6);
        budgetVsActualChartInstance = new Chart(ctx3, {
            type: 'bar',
            data: {
                labels: cats.map(c => c.name),
                datasets: [
                    { label: 'Budget', data: cats.map(c => c.allocated), backgroundColor: '#94a3b8', borderRadius: 4 },
                    { label: 'Spent', data: cats.map(c => c.spent), backgroundColor: '#0d9488', borderRadius: 4 }
                ]
            },
            options: {
                responsive: true,
                scales: { y: { ticks: { callback: v => '₹' + (v / 1000) + 'k' } } }
            }
        });
    }

    // 4. Spending & Savings Trend
    const ctx4 = document.getElementById("spendingTrendChart");
    if (ctx4) {
        if (spendingTrendChartInstance) spendingTrendChartInstance.destroy();
        spendingTrendChartInstance = new Chart(ctx4, {
            type: 'line',
            data: {
                labels: ['May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct'],
                datasets: [
                    { label: 'Expenses', data: [22000, 23500, 24000, 23000, 25500, m.totalExpenses], borderColor: '#f43f5e', tension: 0.3 },
                    { label: 'Savings', data: [13000, 14500, 16000, 17000, 14500, m.totalSaved], borderColor: '#10b981', tension: 0.3 }
                ]
            },
            options: {
                responsive: true,
                scales: { y: { ticks: { callback: v => '₹' + (v / 1000) + 'k' } } }
            }
        });
    }
}

// 9. MONTHLY REPORT SCREEN
function renderReportScreen() {
    const m = computeMetrics();

    document.getElementById("repIncome").textContent = formatINR(m.salary);
    document.getElementById("repExpenses").textContent = formatINR(m.totalExpenses);
    document.getElementById("repSavings").textContent = formatINR(m.totalSaved);
    document.getElementById("repSavingsRate").textContent = m.savingsRate.toFixed(1) + "%";
    document.getElementById("repBudgetUsed").textContent = m.budgetUsed.toFixed(1) + "%";

    const topCat = [...m.categoryStatuses].sort((a, b) => b.spent - a.spent)[0];
    document.getElementById("repTopCategory").textContent = topCat ? `${topCat.name} (${formatINR(topCat.spent)})` : "None";

    const highestExpense = store.expenses.reduce((max, e) => e.amount > (max ? max.amount : 0) ? e : max, null);
    document.getElementById("repLargestExpense").textContent = highestExpense ? `${formatINR(highestExpense.amount)} (${highestExpense.description || highestExpense.category})` : "₹0";

    document.getElementById("repGoalProgressSummary").textContent = `Emergency fund covers approximately ${m.efMonths.toFixed(1)} months of essential living expenses.`;
}

// 10. FINANCIAL HEALTH SCORE SCREEN
function renderHealthScoreScreen() {
    const m = computeMetrics();
    const factorsList = document.getElementById("healthFactorsList");
    factorsList.innerHTML = "";

    const factors = [
        { isPos: m.savingsRate >= 30, title: "High Savings Rate", text: `Saving ${m.savingsRate.toFixed(1)}% of in-hand salary (target >= 30%).` },
        { isPos: m.budgetUsed <= 90, title: "Budget Adherence", text: `Used ${Math.round(m.budgetUsed)}% of total monthly budget limits.` },
        { isPos: m.efMonths >= 3, title: "Emergency Cushion", text: `Liquid emergency buffer covers ${m.efMonths.toFixed(1)} months of essentials.` },
        { isPos: m.totalExpenses <= m.salary, title: "Living Within Means", text: "Total spending strictly remains within monthly income." },
        { isPos: false, title: "Shopping Expenditure Increased", text: "Shopping category reached 84% of allocated limit." }
    ];

    factors.forEach(f => {
        factorsList.innerHTML += `
            <div class="factor-item ${f.isPos ? 'positive' : 'negative'}">
                <i data-lucide="${f.isPos ? 'check-circle' : 'alert-triangle'}"></i>
                <div>
                    <strong>${f.isPos ? '+ ' : '- '}${f.title}</strong>
                    <p style="color:var(--text-secondary);margin-top:2px;">${f.text}</p>
                </div>
            </div>
        `;
    });
    lucide.createIcons();
}

// 11. NOTIFICATIONS SCREEN
function renderNotificationsScreen() {
    const list = document.getElementById("notificationsList");
    list.innerHTML = "";

    if (store.notifications.length === 0) {
        list.innerHTML = `<p style="text-align:center;padding:20px;color:var(--text-secondary);">No new notifications.</p>`;
        return;
    }

    store.notifications.forEach(n => {
        list.innerHTML += `
            <div class="section-card" style="margin-bottom:8px;padding:12px;">
                <div style="display:flex;justify-content:space-between;margin-bottom:4px;">
                    <strong style="font-size:14px;">${n.title}</strong>
                    <span style="font-size:11px;color:var(--text-secondary);">${n.time}</span>
                </div>
                <p style="font-size:13px;color:var(--text-secondary);">${n.message}</p>
            </div>
        `;
    });
}

// 12. PROFILE & SETTINGS SCREEN
function renderProfileScreen() {
    document.getElementById("profileName").textContent = store.user.name || "Employee";
    document.getElementById("profileSalaryLabel").textContent = `Monthly Salary: ${formatINR(store.user.monthlySalary)}`;
    document.getElementById("profileSalaryDate").textContent = `Salary Day: ${store.user.salaryDate || 1}st of month`;
    document.getElementById("profileDarkToggle").checked = store.user.isDarkMode;
    document.getElementById("profileNotifToggle").checked = store.user.notificationsEnabled;
    document.getElementById("pinStatusText").textContent = store.user.pin ? "PIN Protection Active (••••)" : "Not Set (Tap to configure)";
}

// --- ACTIONS & MODAL HANDLERS ---

function closeModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.remove("active");
}

function openModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.add("active");
}

// Add Expense
document.getElementById("fabAddExpense").addEventListener("click", () => {
    populateCategorySelect("expCategory");
    document.getElementById("expDate").value = getTodayDDMMYYYY();
    openModal("addExpenseModal");
});

document.getElementById("headerAddExpenseBtn")?.addEventListener("click", () => {
    populateCategorySelect("expCategory");
    document.getElementById("expDate").value = getTodayDDMMYYYY();
    openModal("addExpenseModal");
});

function populateCategorySelect(selectId) {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    sel.innerHTML = "";
    store.categories.forEach(c => {
        sel.innerHTML += `<option value="${c.name}">${c.name}</option>`;
    });
}

document.getElementById("addExpenseForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const amount = Number(document.getElementById("expAmount").value);
    const category = document.getElementById("expCategory").value;
    const date = document.getElementById("expDate").value;
    const paymentMethod = document.getElementById("expPaymentMethod").value;
    const desc = document.getElementById("expDesc").value || category;
    const isRecurring = document.getElementById("expIsRecurring").checked;

    if (!amount || amount <= 0) return;

    const newExp = {
        id: "exp-" + Date.now(),
        amount,
        category,
        date,
        paymentMethod,
        description: desc,
        isRecurring
    };

    store.expenses.unshift(newExp);

    if (isRecurring) {
        store.recurring.push({
            id: "rec-" + Date.now(),
            name: desc,
            amount,
            category,
            frequency: "Monthly",
            dueDate: date,
            isActive: true
        });
    }

    store.save();
    closeModal("addExpenseModal");
    document.getElementById("addExpenseForm").reset();

    renderActiveScreen(currentScreen);
});

// Delete Expense
let itemToDeleteCallback = null;
function deleteExpense(id) {
    openConfirmDialog("Delete Expense", "Are you sure you want to delete this expense record?", () => {
        store.expenses = store.expenses.filter(e => e.id !== id);
        store.save();
        renderActiveScreen(currentScreen);
    });
}

// Pay Bill Directly
function payBillDirectly(id) {
    const bill = store.recurring.find(b => b.id === id);
    if (!bill) return;

    store.expenses.unshift({
        id: "exp-" + Date.now(),
        amount: bill.amount,
        category: bill.category,
        date: getTodayDDMMYYYY(),
        paymentMethod: "UPI",
        description: `Bill Paid: ${bill.name}`,
        isRecurring: true
    });

    store.save();
    alert(`Successfully recorded payment of ${formatINR(bill.amount)} for ${bill.name}!`);
    renderActiveScreen(currentScreen);
}

function deleteRecurringItem(id) {
    openConfirmDialog("Delete Recurring Bill", "Are you sure you want to delete this bill schedule?", () => {
        store.recurring = store.recurring.filter(r => r.id !== id);
        store.save();
        renderActiveScreen(currentScreen);
    });
}

// Add Salary Modal
document.getElementById("openAddSalaryModalBtn").addEventListener("click", () => {
    document.getElementById("salDate").value = getTodayDDMMYYYY();
    openModal("addSalaryModal");
});

document.getElementById("salGross").addEventListener("input", updateSalPreview);
document.getElementById("salDeductions").addEventListener("input", updateSalPreview);

function updateSalPreview() {
    const gross = Number(document.getElementById("salGross").value) || 0;
    const ded = Number(document.getElementById("salDeductions").value) || 0;
    document.getElementById("salCalculatedInHand").textContent = formatINR(Math.max(0, gross - ded));
}

document.getElementById("addSalaryForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const month = document.getElementById("salMonth").value;
    const gross = Number(document.getElementById("salGross").value);
    const ded = Number(document.getElementById("salDeductions").value) || 0;
    const inHand = Math.max(0, gross - ded);
    const date = document.getElementById("salDate").value || getTodayDDMMYYYY();
    const notes = document.getElementById("salNotes").value;

    store.salaries.push({
        id: "sal-" + Date.now(),
        monthYear: month,
        grossSalary: gross,
        deductions: ded,
        inHandSalary: inHand,
        paymentDate: date,
        notes: notes
    });

    store.user.monthlySalary = inHand;
    store.save();
    closeModal("addSalaryModal");
    renderActiveScreen(currentScreen);
});

function deleteSalaryRecord(id) {
    openConfirmDialog("Delete Salary Record", "Are you sure you want to remove this salary entry?", () => {
        store.salaries = store.salaries.filter(s => s.id !== id);
        store.save();
        renderActiveScreen(currentScreen);
    });
}

// Custom Category Modal
document.getElementById("openAddCategoryModalBtn").addEventListener("click", () => {
    openModal("addCategoryModal");
});

document.getElementById("addCategoryForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const name = document.getElementById("customCatName").value.trim();
    const budget = Number(document.getElementById("customCatBudget").value);

    if (name && budget > 0) {
        store.categories.push({
            id: "cat-" + Date.now(),
            name: name,
            allocated: budget,
            isCustom: true
        });
        store.save();
        closeModal("addCategoryModal");
        renderActiveScreen(currentScreen);
    }
});

// Goals
document.getElementById("openAddGoalBtn").addEventListener("click", () => {
    openModal("addGoalModal");
});

document.getElementById("addGoalForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const title = document.getElementById("goalTitle").value.trim();
    const target = Number(document.getElementById("goalTarget").value);
    const current = Number(document.getElementById("goalCurrent").value) || 0;
    const monthly = Number(document.getElementById("goalMonthly").value) || 2000;

    if (title && target > 0) {
        store.goals.push({
            id: "goal-" + Date.now(),
            title: title,
            targetAmount: target,
            currentAmount: current,
            monthlyContribution: monthly,
            isEmergencyFund: false
        });
        store.save();
        closeModal("addGoalModal");
        renderActiveScreen(currentScreen);
    }
});

function openDepositModal(goalId, title) {
    document.getElementById("depositGoalId").value = goalId;
    document.getElementById("depositModalTitle").textContent = `Deposit to ${title}`;
    openModal("depositGoalModal");
}

document.getElementById("depositGoalForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const goalId = document.getElementById("depositGoalId").value;
    const amount = Number(document.getElementById("depositAmount").value);
    const goal = store.goals.find(g => g.id === goalId);
    if (goal && amount > 0) {
        goal.currentAmount += amount;
        store.save();
        closeModal("depositGoalModal");
        renderActiveScreen(currentScreen);
    }
});

function deleteGoal(id) {
    openConfirmDialog("Delete Goal", "Are you sure you want to delete this savings goal?", () => {
        store.goals = store.goals.filter(g => g.id !== id);
        store.save();
        renderActiveScreen(currentScreen);
    });
}

// Add Recurring Bill Modal
document.getElementById("openAddRecurringBtn").addEventListener("click", () => {
    populateCategorySelect("recCategory");
    document.getElementById("recDueDate").value = getTodayDDMMYYYY();
    openModal("addRecurringModal");
});

document.getElementById("addRecurringForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const name = document.getElementById("recName").value.trim();
    const amount = Number(document.getElementById("recAmount").value);
    const category = document.getElementById("recCategory").value;
    const frequency = document.getElementById("recFrequency").value;
    const dueDate = document.getElementById("recDueDate").value;

    if (name && amount > 0) {
        store.recurring.push({
            id: "rec-" + Date.now(),
            name,
            amount,
            category,
            frequency,
            dueDate,
            isActive: true
        });
        store.save();
        closeModal("addRecurringModal");
        renderActiveScreen(currentScreen);
    }
});

// Confirmation Dialog helper
function openConfirmDialog(title, msg, onConfirm) {
    document.getElementById("confirmDeleteTitle").textContent = title;
    document.getElementById("confirmDeleteMessage").textContent = msg;
    itemToDeleteCallback = onConfirm;
    openModal("confirmDeleteModal");
}

document.getElementById("confirmDeleteActionBtn").addEventListener("click", () => {
    if (itemToDeleteCallback) {
        itemToDeleteCallback();
        itemToDeleteCallback = null;
    }
    closeModal("confirmDeleteModal");
});

// PIN Configuration
document.getElementById("setPinItem").addEventListener("click", () => {
    openModal("pinModal");
});

document.getElementById("pinForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const pin = document.getElementById("inputPinVal").value.trim();
    store.user.pin = pin.length === 4 ? pin : null;
    store.save();
    closeModal("pinModal");
    renderProfileScreen();
});

// PIN Keypad for Lock Screen
let enteredDigits = "";
function pressKey(k) {
    if (k === "DEL") {
        enteredDigits = enteredDigits.slice(0, -1);
    } else if (enteredDigits.length < 4) {
        enteredDigits += k;
    }

    // Update dots
    for (let i = 0; i < 4; i++) {
        const dot = document.getElementById("dot" + i);
        if (dot) {
            if (i < enteredDigits.length) dot.classList.add("filled");
            else dot.classList.remove("filled");
        }
    }

    if (enteredDigits.length === 4) {
        if (enteredDigits === store.user.pin) {
            document.getElementById("pinLockOverlay").classList.remove("active");
            enteredDigits = "";
        } else {
            document.getElementById("pinError").textContent = "Incorrect PIN. Try again.";
            setTimeout(() => {
                enteredDigits = "";
                for (let i = 0; i < 4; i++) {
                    const dot = document.getElementById("dot" + i);
                    if (dot) dot.classList.remove("filled");
                }
                document.getElementById("pinError").textContent = "";
            }, 500);
        }
    }
}

// Data Export & Reset
document.getElementById("exportDataBtn").addEventListener("click", () => {
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(store, null, 2));
    const dlAnchor = document.createElement('a');
    dlAnchor.setAttribute("href", dataStr);
    dlAnchor.setAttribute("download", `salarywise_backup_${Date.now()}.json`);
    dlAnchor.click();
});

document.getElementById("deleteAllDataBtn").addEventListener("click", () => {
    openConfirmDialog("Delete All Financial Data?", "This will permanently clear all your salaries, expenses, budgets, and savings goals.", () => {
        store.salaries = [];
        store.expenses = [];
        store.goals = [];
        store.recurring = [];
        store.save();
        alert("All financial data deleted successfully.");
        renderActiveScreen(currentScreen);
    });
});

document.getElementById("resetAccountBtn").addEventListener("click", () => {
    openConfirmDialog("Reset Account?", "This will wipe all local data and return to onboarding.", () => {
        localStorage.clear();
        store.resetToDefaults();
        store.user.isOnboardingDone = false;
        store.save();
        navigateTo("onboarding");
    });
});

// Top bar buttons
document.getElementById("notifBtn").addEventListener("click", () => navigateTo("notifications"));
document.getElementById("profileBtn").addEventListener("click", () => navigateTo("profile"));
document.getElementById("clearNotifsBtn")?.addEventListener("click", () => {
    store.notifications = [];
    store.save();
    renderNotificationsScreen();
});

// Expense Filter Handlers
document.querySelectorAll(".filter-chip").forEach(chip => {
    chip.addEventListener("click", () => {
        document.querySelectorAll(".filter-chip").forEach(c => c.classList.remove("active"));
        chip.classList.add("active");
        currentExpenseFilter = chip.dataset.filter;
        filterAndRenderExpenses();
    });
});

document.getElementById("expenseSearchInput").addEventListener("input", filterAndRenderExpenses);
document.getElementById("expenseCategoryFilter").addEventListener("change", filterAndRenderExpenses);
document.getElementById("expenseMethodFilter").addEventListener("change", filterAndRenderExpenses);
document.getElementById("sortExpenseBtn").addEventListener("click", () => {
    currentSortDesc = !currentSortDesc;
    filterAndRenderExpenses();
});

// Bottom Navigation Listeners
document.querySelectorAll(".nav-item").forEach(item => {
    item.addEventListener("click", () => {
        const route = item.dataset.nav;
        navigateTo(route);
    });
});

// Theme Toggle
function applyTheme(isDark) {
    document.body.classList.toggle("theme-dark", isDark);
    document.body.classList.toggle("theme-light", !isDark);
    document.getElementById("themeBtnText").textContent = isDark ? "Light Mode" : "Dark Mode";
    document.getElementById("profileDarkToggle").checked = isDark;
}

document.getElementById("toggleThemeBtn").addEventListener("click", () => {
    store.user.isDarkMode = !store.user.isDarkMode;
    store.save();
    applyTheme(store.user.isDarkMode);
    if (currentScreen === "analytics") renderAnalyticsScreen();
});

document.getElementById("profileDarkToggle").addEventListener("change", (e) => {
    store.user.isDarkMode = e.target.checked;
    store.save();
    applyTheme(store.user.isDarkMode);
});

// Reset Sample Data Button
document.getElementById("seedDataBtn").addEventListener("click", () => {
    store.resetToDefaults();
    alert("Sample data loaded according to SalaryWise specification (₹40,000 salary, ₹24,500 expenses, ₹15,500 saved)!");
    renderActiveScreen(currentScreen);
});

// Phone Frame Toggle
document.getElementById("toggleFrameBtn").addEventListener("click", () => {
    const wrap = document.getElementById("deviceWrapper");
    wrap.classList.toggle("framed");
    const isFramed = wrap.classList.contains("framed");
    document.getElementById("frameBtnText").textContent = isFramed ? "Frame: ON" : "Frame: OFF";
});

// Status Bar Clock
function updateClock() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const mins = String(now.getMinutes()).padStart(2, '0');
    const clock = document.getElementById("statusClock");
    if (clock) clock.textContent = `${hours}:${mins}`;
}
setInterval(updateClock, 1000);
updateClock();

// Onboarding Form
document.getElementById("onboardingForm").addEventListener("submit", (e) => {
    e.preventDefault();
    const name = document.getElementById("obName").value.trim() || "Employee";
    const salary = Number(document.getElementById("obSalary").value) || 40000;
    const salaryDate = Number(document.getElementById("obSalaryDate").value) || 1;
    const savings = Number(document.getElementById("obSavings").value) || 15000;
    const essentials = Number(document.getElementById("obEssentials").value) || (salary * 0.5);
    const savingsTarget = Number(document.getElementById("obSavingsTarget").value) || (salary * 0.25);
    const efTarget = Number(document.getElementById("obEFTarget").value) || (essentials * 6);

    store.user.name = name;
    store.user.monthlySalary = salary;
    store.user.salaryDate = salaryDate;
    store.user.currentSavings = savings;
    store.user.essentialExpenses = essentials;
    store.user.monthlySavingsTarget = savingsTarget;
    store.user.emergencyFundTarget = efTarget;
    store.user.isOnboardingDone = true;

    // Automatically create first monthly financial plan (Section 1)
    const currentMonth = "2026-10";
    store.salaries = [
        { id: "sal-init", monthYear: currentMonth, grossSalary: salary + 5000, deductions: 5000, inHandSalary: salary, paymentDate: getTodayDDMMYYYY(), notes: "Initial plan salary" }
    ];

    store.goals = [
        { id: "goal-ef", title: "Emergency Fund", targetAmount: efTarget, currentAmount: savings, monthlyContribution: savingsTarget * 0.6, isEmergencyFund: true }
    ];

    store.save();
    navigateTo("home");
});

// App Initialization
window.addEventListener("DOMContentLoaded", () => {
    applyTheme(store.user.isDarkMode);

    if (store.user.pin) {
        document.getElementById("pinLockOverlay").classList.add("active");
    }

    if (!store.user.isOnboardingDone) {
        navigateTo("onboarding");
    } else {
        navigateTo("home");
    }
});
