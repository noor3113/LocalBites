package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.TextMuted

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    starSize: Dp = 24.dp,
    maxStars: Int = 5
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..maxStars) {
            val isFilled = i <= rating
            val icon = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarOutline
            val tint = if (isFilled) AmberSecondary else TextMuted

            Icon(
                imageVector = icon,
                contentDescription = "$i stars",
                tint = tint,
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (onRatingChanged != null) {
                            Modifier.clickable { onRatingChanged(i) }
                        } else Modifier
                    )
            )
        }
    }
}
