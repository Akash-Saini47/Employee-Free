package com.salarywise.app.domain.model

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Credit Card"),
    BANK_TRANSFER("Bank Transfer"),
    OTHER("Other")
}

data class DefaultCategoryItem(
    val name: String,
    val iconName: String,
    val isEssential: Boolean = false
)

object DefaultCategories {
    val list = listOf(
        DefaultCategoryItem("Rent", "home", isEssential = true),
        DefaultCategoryItem("Food", "restaurant", isEssential = true),
        DefaultCategoryItem("Groceries", "shopping_cart", isEssential = true),
        DefaultCategoryItem("Transportation", "commute", isEssential = true),
        DefaultCategoryItem("Utilities", "bolt", isEssential = true),
        DefaultCategoryItem("Bills", "receipt", isEssential = true),
        DefaultCategoryItem("Shopping", "shopping_bag", isEssential = false),
        DefaultCategoryItem("Entertainment", "movie", isEssential = false),
        DefaultCategoryItem("Healthcare", "medical_services", isEssential = true),
        DefaultCategoryItem("Education", "school", isEssential = true),
        DefaultCategoryItem("EMI", "credit_score", isEssential = true),
        DefaultCategoryItem("Investments", "trending_up", isEssential = false),
        DefaultCategoryItem("Savings", "savings", isEssential = false),
        DefaultCategoryItem("Other", "more_horiz", isEssential = false)
    )
}
