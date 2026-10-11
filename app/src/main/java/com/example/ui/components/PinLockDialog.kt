package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppStrings
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed

@Composable
fun PinLockScreen(
    strings: AppStrings,
    isSettingPin: Boolean = false,
    onPinSuccess: (String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("pin_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isSettingPin) strings.setPin else strings.enterPin,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.pinProtected,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Dots (4 dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMsg!!,
                    color = ExpenseRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Numeric Keypad
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                keypad.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        row.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(modifier = Modifier.size(68.dp))
                            } else if (key == "DEL") {
                                Surface(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                                errorMsg = null
                                            }
                                        },
                                    shape = CircleShape,
                                    color = Color.Transparent
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            if (enteredPin.length < 4) {
                                                val newPin = enteredPin + key
                                                enteredPin = newPin
                                                errorMsg = null
                                                if (newPin.length == 4) {
                                                    onPinSuccess(newPin)
                                                }
                                            }
                                        },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (onCancel != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = strings.cancel,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onCancel() }
                        .padding(8.dp)
                )
            }
        }
    }
}
