package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import androidx.compose.foundation.shape.CircleShape
import com.example.ui.theme.OnAmberPrimary

val FOOD_CATEGORIES = listOf(
    "All" to "✨",
    "Chai" to "☕",
    "Fast Food" to "🍔",
    "Snacks" to "🥟",
    "Juice" to "🥤",
    "Desi Food" to "🥘",
    "Other" to "🍲"
)

@Composable
fun CategoryFilterBar(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FOOD_CATEGORIES.forEach { (catName, emoji) ->
            val isSelected = selectedCategory == catName

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) AmberPrimary else CharcoalSurface
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) AmberPrimary else CharcoalBorder,
                        shape = CircleShape
                    )
                    .clickable { onCategorySelected(catName) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("filter_chip_$catName")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = emoji, fontSize = 13.sp)
                    Text(
                        text = catName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) OnAmberPrimary else TextSecondary
                    )
                }
            }
        }
    }
}
