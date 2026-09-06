package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LocalBitesTopBar(
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onOpenAiAssistant: () -> Unit = {},
    onOpenAbout: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = CharcoalBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Back button or Logo + Wordmark & Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (canNavigateBack) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CharcoalSurface)
                                .border(1.dp, CharcoalBorder, CircleShape)
                                .testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = AmberPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    // App Logo Badge - w-10 h-10 bg-[#E8770E] rounded-xl flex items-center justify-center shadow-lg
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmberPrimary)
                            .testTag("app_logo"),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_localbites_logo),
                            contentDescription = "Local Bites Logo",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Title and Subtitle: "HYPERLOCAL DISCOVERY"
                    Column {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        color = AmberPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        letterSpacing = (-0.5).sp
                                    )
                                ) {
                                    append("Local")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        letterSpacing = (-0.5).sp
                                    )
                                ) {
                                    append("Bites")
                                }
                            },
                            modifier = Modifier.testTag("wordmark_title")
                        )
                        Text(
                            text = "HYPERLOCAL DISCOVERY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = TextMuted
                        )
                    }
                }

                // Right: Action buttons styled as circular icons with border
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // AI Guide Assistant Button
                    IconButton(
                        onClick = onOpenAiAssistant,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CharcoalSurface)
                            .border(1.dp, CharcoalBorder, CircleShape)
                            .testTag("ai_assistant_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "Ask Local Bites Community AI Guide",
                            tint = AmberPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // About App Button
                    IconButton(
                        onClick = onOpenAbout,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CharcoalSurface)
                            .border(1.dp, CharcoalBorder, CircleShape)
                            .testTag("about_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "About Local Bites",
                            tint = TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}
