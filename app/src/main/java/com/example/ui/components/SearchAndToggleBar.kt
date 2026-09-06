package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class DiscoveryViewMode {
    MAP, LIST
}

@Composable
fun SearchAndToggleBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    viewMode: DiscoveryViewMode,
    onViewModeChange: (DiscoveryViewMode) -> Unit,
    onLocateMe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Input Box
        Box(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalSurface)
                .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search nearby street stalls...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(AmberPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input")
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // View Mode Toggle (Map vs List)
        Box(
            modifier = Modifier
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CharcoalSurface)
                .border(1.dp, CharcoalBorder, RoundedCornerShape(14.dp))
                .padding(3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Map button
                val mapBg by animateColorAsState(
                    if (viewMode == DiscoveryViewMode.MAP) AmberPrimary else Color.Transparent,
                    label = "map_bg"
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(mapBg)
                        .clickable { onViewModeChange(DiscoveryViewMode.MAP) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("toggle_map_view"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Map view",
                        tint = if (viewMode == DiscoveryViewMode.MAP) com.example.ui.theme.OnAmberPrimary else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // List button
                val listBg by animateColorAsState(
                    if (viewMode == DiscoveryViewMode.LIST) AmberPrimary else Color.Transparent,
                    label = "list_bg"
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(listBg)
                        .clickable { onViewModeChange(DiscoveryViewMode.LIST) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("toggle_list_view"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "List view",
                        tint = if (viewMode == DiscoveryViewMode.LIST) com.example.ui.theme.OnAmberPrimary else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // GPS Locate Button
        IconButton(
            onClick = onLocateMe,
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CharcoalSurface)
                .border(1.dp, CharcoalBorder, RoundedCornerShape(14.dp))
                .testTag("locate_me_button")
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "My Location GPS",
                tint = AmberPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
