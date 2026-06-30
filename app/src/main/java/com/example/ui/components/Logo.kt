package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
fun DownLandsLogo(modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary) {
    val logoVector = ImageVector.Builder(
        name = "DownLandsLogo",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(tint), fillAlpha = 0.3f) {
            moveTo(3f, 7f)
            curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
            lineToRelative(5f, 0f)
            lineToRelative(1.5f, 2f)
            lineToRelative(5.5f, 0f)
            curveToRelative(1.1f, 0f, 2f, 0.9f, 2f, 2f)
            lineToRelative(0f, 9f)
            curveToRelative(0f, 1.1f, -0.9f, 2f, -2f, 2f)
            lineToRelative(-14f, 0f)
            curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f)
            lineToRelative(0f, -11f)
            close()
        }
        path(fill = SolidColor(tint)) {
            moveTo(3f, 8.5f)
            curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
            lineToRelative(5f, 0f)
            lineToRelative(1.5f, 2f)
            lineToRelative(5.5f, 0f)
            curveToRelative(1.1f, 0f, 2f, 0.9f, 2f, 2f)
            lineToRelative(0f, 9f)
            curveToRelative(0f, 1.1f, -0.9f, 2f, -2f, 2f)
            lineToRelative(-14f, 0f)
            curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f)
            lineToRelative(0f, -11f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(11.5f, 15f)
            lineToRelative(-3f, -3f)
            lineToRelative(2f, 0f)
            lineToRelative(0f, -4.5f)
            lineToRelative(2f, 0f)
            lineToRelative(0f, 4.5f)
            lineToRelative(2f, 0f)
            close()
            moveTo(8.5f, 16f)
            lineToRelative(6f, 0f)
            lineToRelative(0f, 1f)
            lineToRelative(-6f, 0f)
            close()
        }
    }.build()

    Icon(
        imageVector = logoVector,
        contentDescription = "DownLands Logo",
        modifier = modifier.size(24.dp),
        tint = Color.Unspecified
    )
}
