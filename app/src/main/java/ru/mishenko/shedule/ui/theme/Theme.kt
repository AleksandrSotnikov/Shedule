package ru.mishenko.shedule.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

val colorMain: Color = Color(255,255,255)
val colorSelected = Color(255, 152, 59, 255)
val colorUnselected = Color(255,255,255)
val colorBottom = Color(47,50,93)
val colorTintBottom = Color(255,255,255)
val colorTextSelected = Color(255,255,255)
val colorTextUnSelected = Color(0,0,0)
val colorBottomSheet = Color(255, 255, 255, 255)
val colorBottomSheetText = Color(0,0,0)
val colorBorder = Color(255,255,255)
//val colorDop: Color = Color(31.99f,32.69f,35.32f,1f, colorSpace = ColorSpaces.AdobeRgb)

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200,
    background = colorMain,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,

)

@Composable
fun SheduleTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}