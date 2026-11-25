/*
 * Astra Launcher
 * Copyright (C) 2026 Astra Launcher contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.random.Random

private data class AstraStar(
    val x: Float,
    val y: Float,
    val radiusDp: Float,
    val alpha: Float,
    val spike: Boolean,
)

private val AstraStarField = List(110) { index ->
    val random = Random(20261124 + index)
    AstraStar(
        x = random.nextFloat(),
        y = random.nextFloat(),
        radiusDp = random.nextFloat() * 1.15f + 0.35f,
        alpha = random.nextFloat() * 0.55f + 0.18f,
        spike = index % 17 == 0,
    )
}

/**
 * The default Astra canvas: a deep-space vertical gradient, three low-opacity
 * aurora blooms and a deterministic starfield. It is drawn only when the user
 * has not picked a custom launcher background, so wallpapers stay untouched.
 */
@Composable
fun AstraBackdrop(
    modifier: Modifier = Modifier,
    starCount: Int = AstraStarField.size,
) {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f

    val top = scheme.background
    val bottom = if (isDark) scheme.surfaceContainerLowest else scheme.surface
    val primary = scheme.primary
    val secondary = scheme.secondary
    val tertiary = scheme.tertiary

    val stars = remember(starCount) { AstraStarField.take(starCount) }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(top, bottom),
                startY = 0f,
                endY = max(size.height, 1f),
            )
        )

        val longest = max(size.width, size.height)
        val auroraAlpha = if (isDark) 1f else 0.45f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.20f * auroraAlpha), Color.Transparent),
                center = Offset(size.width * 0.16f, size.height * 0.12f),
                radius = longest * 0.72f,
            ),
            radius = longest * 0.72f,
            center = Offset(size.width * 0.16f, size.height * 0.12f),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondary.copy(alpha = 0.12f * auroraAlpha), Color.Transparent),
                center = Offset(size.width * 0.88f, size.height * 0.92f),
                radius = longest * 0.62f,
            ),
            radius = longest * 0.62f,
            center = Offset(size.width * 0.88f, size.height * 0.92f),
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tertiary.copy(alpha = 0.08f * auroraAlpha), Color.Transparent),
                center = Offset(size.width * 0.72f, size.height * 0.04f),
                radius = longest * 0.48f,
            ),
            radius = longest * 0.48f,
            center = Offset(size.width * 0.72f, size.height * 0.04f),
        )

        val starColor = if (isDark) Color.White else primary
        val starAlphaScale = if (isDark) 1f else 0.6f

        stars.forEach { star ->
            val center = Offset(star.x * size.width, star.y * size.height)
            val radius = star.radiusDp * density
            val alpha = (star.alpha * starAlphaScale).coerceIn(0f, 1f)

            drawCircle(
                color = starColor.copy(alpha = alpha),
                radius = radius,
                center = center,
            )

            if (star.spike) {
                val arm = radius * 3.2f
                drawLine(
                    color = starColor.copy(alpha = alpha * 0.55f),
                    start = Offset(center.x - arm, center.y),
                    end = Offset(center.x + arm, center.y),
                    strokeWidth = radius * 0.6f,
                )
                drawLine(
                    color = starColor.copy(alpha = alpha * 0.55f),
                    start = Offset(center.x, center.y - arm),
                    end = Offset(center.x, center.y + arm),
                    strokeWidth = radius * 0.6f,
                )
            }
        }
    }
}
