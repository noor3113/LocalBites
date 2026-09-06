package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.Vendor
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OnAmberPrimary
import com.example.ui.theme.StatusClosed
import com.example.ui.theme.StatusOpen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeUtils

@Composable
fun MapVendorPreviewCard(
    vendor: Vendor,
    onViewDetails: (Vendor) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, CharcoalBorder, RoundedCornerShape(18.dp))
            .clickable { onViewDetails(vendor) }
            .testTag("map_vendor_preview_card"),
        color = CharcoalCard,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Stall photo thumbnail
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalSurface),
                    contentAlignment = Alignment.Center
                ) {
                    VendorPhotoBanner(
                        photoUri = vendor.photoUri,
                        category = vendor.category,
                        modifier = Modifier.size(68.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Info Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Row 1: Title and Star Rating badge + Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = vendor.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Star rating badge with #1A1614 background
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CharcoalBackground)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "★",
                                    fontSize = 10.sp,
                                    color = AmberSecondary
                                )
                                Text(
                                    text = String.format("%.1f", vendor.avgRating),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Row 2: Subtitle (category + price range)
                    Text(
                        text = "${getCategoryEmoji(vendor.category)} ${vendor.category} • ${vendor.priceRange}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Row 3: Open status + Updated time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (vendor.isOpen) StatusOpen else StatusClosed
                            )
                        }

                        Text(
                            text = "Updated ${TimeUtils.formatRelativeTime(vendor.lastStatusUpdateTimestamp)}",
                            fontSize = 9.sp,
                            color = TextMuted,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button
            Button(
                onClick = { onViewDetails(vendor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("preview_view_details_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "View Stall Details & Reviews",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
