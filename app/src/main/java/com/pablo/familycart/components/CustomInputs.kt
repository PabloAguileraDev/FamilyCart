package com.pablo.familycart.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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

/**
 * Título personalizado con tamaño grande, color y estilo por defecto adaptado al tema de la app.
 */
@Composable
fun CustomTitulo(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Verde,
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

/**
 * Texto personalizado reutilizable.
 */
@Composable
fun CustomText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Negro,
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

/**
 * Campo de texto personalizado. Soporta texto normal o de contraseña.
 *
 * @param isPassword Si es true, oculta el texto con puntos.
 * @param isError Aplica color rojo si hay algún error en el input.
 */
@Composable
fun CustomTextField(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        },
        textStyle = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
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
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

/**
 * Botón reutilizable con esquinas redondeadas y color verde por defecto.
 */
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
