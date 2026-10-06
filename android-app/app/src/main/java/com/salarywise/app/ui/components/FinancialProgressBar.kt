package com.salarywise.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.salarywise.app.ui.theme.*

@Composable
fun FinancialProgressBar(
    percentage: Double,
    modifier: Modifier = Modifier,
    height: Int = 8
) {
    val progressFraction = (percentage / 100.0).coerceIn(0.0, 1.0).toFloat()
    val barColor = when {
        percentage > 100.0 -> AlertRose
        percentage >= 80.0 -> WarningAmber
        else -> SuccessGreen
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(height.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progressFraction)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height.dp))
                .background(barColor)
        )
    }
}

@Composable
fun WarningAlertBanner(
    message: String,
    isCritical: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isCritical) AlertContainer else WarningContainer
    val contentColor = if (isCritical) AlertRose else Color(0xFFB45309)
    val icon = if (isCritical) Icons.Filled.Error else Icons.Filled.Warning

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}
