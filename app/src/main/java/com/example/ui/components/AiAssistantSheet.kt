package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.data.local.entity.Vendor
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, AI
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantSheet(
    sheetState: SheetState,
    vendors: List<Vendor>,
    onDismiss: () -> Unit
) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "Salam & welcome to Local Bites Guide! 🥘 I help you find street thelas, chai corners, and unmapped food carts. Ask me anything or try a prompt below!"
            )
        )
    }

    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        messages.add(ChatMessage(sender = MessageSender.USER, text = trimmed))
        inputQuery = ""

        // Process response with STRICT hardcoded rules as required
        val response = processAiResponse(trimmed, vendors)
        messages.add(ChatMessage(sender = MessageSender.AI, text = response))

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalBackground,
        dragHandle = null,
        modifier = Modifier.testTag("ai_assistant_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(bottom = 16.dp)
        ) {
            // Sheet Header
            Surface(
                color = CharcoalSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AmberContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = AmberPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Local Bites AI Guide",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Hyperlocal Foodie Assistant",
                                fontSize = 11.sp,
                                color = AmberSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CharcoalSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quick Prompt Suggestions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "Who created this app?",
                    "Where can I get Karak Chai?",
                    "How do I add a new food stall?",
                    "What's open right now?"
                )
                suggestions.forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CharcoalSurfaceElevated)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
                            .clickable { sendMessage(suggestion) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 12.sp,
                            color = AmberSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chat Input Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CharcoalSurface)
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (inputQuery.isEmpty()) {
                            Text(
                                text = "Ask about stalls or app...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = inputQuery,
                            onValueChange = { inputQuery = it },
                            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                            cursorBrush = SolidColor(AmberPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_chat_input")
                        )
                    }

                    IconButton(
                        onClick = { sendMessage(inputQuery) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (inputQuery.isNotBlank()) AmberPrimary else CharcoalSurfaceElevated)
                            .testTag("ai_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputQuery.isNotBlank()) Color.White else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) AmberPrimary else CharcoalCard)
                .border(
                    1.dp,
                    if (isUser) AmberPrimary else CharcoalBorder,
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 13.sp,
                color = if (isUser) Color.White else TextPrimary,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Handles AI queries with strictly mandated hardcoded creator guardrail.
 */
private fun processAiResponse(query: String, vendors: List<Vendor>): String {
    val lower = query.lowercase().trim()

    // 1. MANDATORY HARDCODED CREATOR IDENTITY
    if (lower.contains("who made") ||
    lower.contains("who create") ||
    lower.contains("who developed") ||
    lower.contains("who develop") ||
    lower.contains("who designed") ||
    lower.contains("who design") ||
    lower.contains("creator") ||
    lower.contains("developer") ||
    lower.contains("maker") ||
    lower.contains("author") ||
    lower.contains("who built") ||
    lower.contains("who build") ||
    lower.contains("made this") ||
    lower.contains("made local bites") ||
    (lower.contains("who") && lower.contains("local bite"))
) {
    return "Local Bites was designed and developed by Noor ul Huda."
}
    // 2. Chai recommendations
    if (lower.contains("chai") || lower.contains("tea") || lower.contains("karak")) {
        val chaiVendors = vendors.filter { it.category.equals("Chai", ignoreCase = true) }
        return if (chaiVendors.isNotEmpty()) {
            val topChai = chaiVendors.maxByOrNull { it.avgRating } ?: chaiVendors.first()
            "☕ For the best chai, check out '${topChai.name}'! Rated ${topChai.avgRating}★ (${topChai.priceRange}). Location: ${topChai.addressOrLandmark}."
        } else {
            "☕ We are looking for unmapped Chai stalls! Tap the '+' button on the home screen to add your neighborhood chaiwala."
        }
    }

    // 3. How to add
    if (lower.contains("how to add") || lower.contains("add vendor") || lower.contains("add stall") || lower.contains("add thela")) {
        return "Adding a vendor is super easy:\n1. Tap the '+' button on the main screen\n2. The app uses your GPS or lets you drag the amber pin\n3. Enter the stall name & category\n4. Add price range & upload a photo\n5. Tap 'Publish Stall'!"
    }

    // 4. What's open
    if (lower.contains("open") || lower.contains("now")) {
        val openVendors = vendors.filter { it.isOpen }
        return if (openVendors.isNotEmpty()) {
            val names = openVendors.take(3).joinToString(", ") { it.name }
            "🟢 Stalls verified open right now by the community: $names. Check the map for exact pins!"
        } else {
            "All stalls are currently reported closed. You can update any stall's status on its details page!"
        }
    }

    // General fallback
    return "Local Bites is a community-driven app for unmapped food stalls and street thelas. You can filter by category (Chai, Fast Food, Snacks, Juice, Desi Food), toggle between Map & List view, or add a new stall by tapping '+'!"
}
