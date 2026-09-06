package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entity.Vendor
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceElevated
import com.example.ui.theme.StatusClosed
import com.example.ui.theme.StatusOpen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeUtils
import java.io.File

@Composable
fun VendorCard(
    vendor: Vendor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
            .testTag("vendor_card_${vendor.id}"),
        colors = CardDefaults.cardColors(containerColor = CharcoalCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Photo Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(CharcoalSurfaceElevated)
            ) {
                VendorPhotoBanner(
                    photoUri = vendor.photoUri,
                    category = vendor.category,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay at bottom of photo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, CharcoalCard.copy(alpha = 0.9f)),
                                startY = 70f
                            )
                        )
                )

                // Top Left: Category Badge
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CharcoalBackground.copy(alpha = 0.85f))
                        .border(1.dp, AmberPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "${getCategoryEmoji(vendor.category)} ${vendor.category}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberSecondary
                    )
                }

                // Top Right: Open / Closed Badge with Community Status
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (vendor.isOpen) Color(0xFF143820).copy(alpha = 0.9f)
                            else Color(0xFF381414).copy(alpha = 0.9f)
                        )
                        .border(
                            1.dp,
                            if (vendor.isOpen) StatusOpen else StatusClosed,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (vendor.isOpen) StatusOpen else StatusClosed)
                        )
                        Text(
                            text = if (vendor.isOpen) "Open Now" else "Closed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (vendor.isOpen) StatusOpen else StatusClosed
                        )
                    }
                }
            }

            // Card Body Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Name and Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = vendor.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = vendor.priceRange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Landmark or street description
                if (vendor.addressOrLandmark.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = vendor.addressOrLandmark,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Bottom Footer: Rating and Community timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Star Rating Badge in #1A1614
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharcoalBackground)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "★",
                                fontSize = 11.sp,
                                color = AmberSecondary
                            )
                            Text(
                                text = String.format("%.1f", vendor.avgRating),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "(${vendor.ratingCount})",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // Community timestamp
                    Text(
                        text = "Updated ${TimeUtils.formatRelativeTime(vendor.lastStatusUpdateTimestamp)}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun VendorPhotoBanner(
    photoUri: String,
    category: String,
    modifier: Modifier = Modifier
) {
    when {
        photoUri == "res:chai_thela_stall" -> {
            Image(
                painter = painterResource(id = R.drawable.chai_thela_stall),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        photoUri == "res:street_food_cart" -> {
            Image(
                painter = painterResource(id = R.drawable.street_food_cart),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        photoUri.isNotBlank() && (photoUri.startsWith("/") || photoUri.startsWith("content:") || photoUri.startsWith("file:")) -> {
            AsyncImage(
                model = File(photoUri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        else -> {
            // Elegant category art placeholder
            Box(
                modifier = modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                CharcoalSurfaceElevated,
                                AmberContainer.copy(alpha = 0.6f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = getCategoryEmoji(category), fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unmapped Street Stall",
                        fontSize = 11.sp,
                        color = AmberSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category) {
        "Chai" -> "☕"
        "Fast Food" -> "🍔"
        "Snacks" -> "🥟"
        "Juice" -> "🥤"
        "Desi Food" -> "🥘"
        else -> "🍲"
    }
}
