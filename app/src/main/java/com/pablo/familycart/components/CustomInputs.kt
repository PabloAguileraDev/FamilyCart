package com.pablo.familycart.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.familycart.ui.theme.Negro
import com.pablo.familycart.ui.theme.Verde

@Composable
fun CustomTitulo(
    text: String,
    color: Color = Verde,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    fontSize: TextUnit = 40.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        style = style,
        color = color,
        modifier = modifier
    )
}

@Composable
fun CustomText(
    text: String,
    color: Color = Negro,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    fontSize: TextUnit = 23.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        style = style,
        color = color,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium) },
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        ),
        modifier = modifier.width(300.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        isError = isError,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Verde,
            unfocusedBorderColor = Verde,
            unfocusedLabelColor = Verde,
            focusedLabelColor = Verde,
        )
    )
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minWidth = 150.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(Verde)
    ) {
        Text(
            text = text,
            maxLines = 1,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
    }
}
