package com.example.nearhelp.ui.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.AiCyan
import com.example.nearhelp.theme.CardSurfaceVariant
import com.example.nearhelp.theme.SurfaceBorder
import com.example.nearhelp.theme.TextHighContrast
import com.example.nearhelp.theme.TextMuted

@Composable
fun OtpInputField(
  otpValue: String,
  onOtpChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  otpLength: Int = 6,
  isError: Boolean = false,
) {
  BasicTextField(
    value = otpValue,
    onValueChange = { newValue ->
      if (newValue.length <= otpLength && newValue.all { it.isDigit() }) {
        onOtpChange(newValue)
      }
    },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
    decorationBox = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        repeat(otpLength) { index ->
          val isFocused = index == otpValue.length
          val char = when {
            index < otpValue.length -> otpValue[index].toString()
            else -> ""
          }

          val borderColor = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> AiCyan
            char.isNotEmpty() -> Color.White.copy(alpha = 0.8f)
            else -> SurfaceBorder
          }

          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(CardSurfaceVariant)
              .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
              ),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = char,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
              ),
              color = if (char.isNotEmpty()) TextHighContrast else TextMuted,
            )
          }
        }
      }
    },
    modifier = modifier.fillMaxWidth(),
  )
}
