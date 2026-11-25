package com.zaviyanllc.sproutplay.games.cup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.zaviyanllc.sproutplay.domain.model.BallColor
import com.zaviyanllc.sproutplay.domain.model.CupStyle
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * A glossy 3D cup drawn with Canvas, matching the @spotquest reference look:
 * gradient body, white top rim, bottom rim, soft shadow, optional zigzag
 * stripes or ridges. A glowing ball sits underneath on the ground layer and
 * is revealed when the cup lifts. Never draws any watermark.
 *
 * Layering: the ground layer (shadow + glow ring + ball) never moves; the
 * cup layer lifts up to reveal the ball, exactly like the real shell game.
 */
@Composable
fun CupView(
    style: CupStyle,
    base: Color,
    light: Color,
    dark: Color,
    stripeColor: Color?,
    ball: BallColor?,
    lifted: Boolean,
    glowColor: Color?,
    dimmed: Boolean,
    shakeKey: Int,
    modifier: Modifier = Modifier,
) {
    var cupHeightPx by remember { mutableFloatStateOf(0f) }
    val liftAnim = remember { Animatable(0f) }
    LaunchedEffect(lifted) {
        liftAnim.animateTo(if (lifted) 1f else 0f, animationSpec = tween(350))
    }
    val shakeX = remember { Animatable(0f) }
    LaunchedEffect(shakeKey) {
        if (shakeKey > 0) {
            for (dx in listOf(-14f, 12f, -9f, 7f, -4f, 0f)) {
                shakeX.animateTo(dx, animationSpec = tween(55))
                delay(10)
            }
        }
    }

    Box(
        modifier = modifier.onSizeChanged { cupHeightPx = it.height.toFloat() },
        contentAlignment = Alignment.Center,
    ) {
        // ---- Ground layer: shadow, glow ring, ball (never moves) ----
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val rimH = 0.17f * h

            drawOval(
                color = Color.Black.copy(alpha = 0.35f),
                topLeft = Offset(w * 0.06f, h - rimH * 0.55f),
                size = androidx.compose.ui.geometry.Size(w * 0.88f, rimH * 0.55f),
            )

            if (glowColor != null) {
                drawCircle(
                    color = glowColor.copy(alpha = 0.16f),
                    radius = w * 0.62f,
                    center = Offset(w / 2, h * 0.55f),
                )
                drawCircle(
                    color = glowColor.copy(alpha = 0.85f),
                    radius = w * 0.56f,
                    center = Offset(w / 2, h * 0.55f),
                    style = Stroke(width = 5.dp.toPx()),
                )
            }

            if (ball != null) {
                val ballR = w * 0.22f
                val ballC = Offset(w / 2, h * 0.82f)
                drawCircle(
                    color = ball.color.copy(alpha = 0.20f),
                    radius = ballR * 2.0f,
                    center = ballC,
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ball.light, ball.color, ball.dark),
                        center = ballC + Offset(-ballR * 0.35f, -ballR * 0.35f),
                        radius = ballR * 1.3f,
                    ),
                    radius = ballR,
                    center = ballC,
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = ballR * 0.18f,
                    center = ballC + Offset(-ballR * 0.35f, -ballR * 0.40f),
                )
            }
        }

        // ---- Cup layer: lifts and shakes ----
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = shakeX.value }
                .offset { IntOffset(0, (-liftAnim.value * cupHeightPx * 0.30f).roundToInt()) },
            contentAlignment = Alignment.Center,
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val topW = 0.60f * w
                    val botW = 0.94f * w
                    val rimH = 0.17f * h

                    // Cup body: trapezoid with a glossy left-to-right gradient
                    val body = Path().apply {
                        moveTo((w - topW) / 2, rimH * 0.55f)
                        lineTo((w + topW) / 2, rimH * 0.55f)
                        lineTo((w + botW) / 2, h - rimH * 0.45f)
                        quadraticBezierTo(w / 2, h - rimH * 0.05f, (w - botW) / 2, h - rimH * 0.45f)
                        close()
                    }
                    drawPath(
                        path = body,
                        brush = Brush.linearGradient(
                            colors = listOf(light, base, dark),
                            start = Offset(w * 0.05f, 0f),
                            end = Offset(w * 0.95f, h),
                        ),
                    )

                    // Vertical highlight stripe on the left
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.20f),
                        topLeft = Offset(w * 0.17f, rimH * 0.85f),
                        size = androidx.compose.ui.geometry.Size(w * 0.09f, h * 0.62f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.045f),
                    )

                    // Zigzag stripes (Brain Breaker / Split Brain style)
                    if (style == CupStyle.STRIPED && stripeColor != null) {
                        for (fy in listOf(0.44f, 0.66f)) {
                            val y = h * fy
                            val halfW = w * (0.30f + (fy - 0.17f) * 0.42f)
                            val zig = Path()
                            val steps = 6
                            for (s in 0..steps) {
                                val x = w / 2 - halfW + (2 * halfW) * s / steps
                                val yy = y + if (s % 2 == 0) -w * 0.035f else w * 0.035f
                                if (s == 0) zig.moveTo(x, yy) else zig.lineTo(x, yy)
                            }
                            drawPath(
                                path = zig,
                                color = stripeColor,
                                style = Stroke(width = 2.5.dp.toPx()),
                            )
                        }
                    }

                    // Horizontal ridges (Lights Out / Boss Level style)
                    if (style == CupStyle.RIDGED) {
                        for (fy in listOf(0.40f, 0.56f, 0.72f)) {
                            val y = h * fy
                            val halfW = w * (0.30f + (fy - 0.17f) * 0.42f)
                            val ridge = Path().apply {
                                moveTo(w / 2 - halfW, y)
                                quadraticBezierTo(w / 2, y + w * 0.03f, w / 2 + halfW, y)
                            }
                            drawPath(
                                path = ridge,
                                color = Color.Black.copy(alpha = 0.20f),
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    }

                    // Bottom rim arc
                    drawOval(
                        color = Color.White.copy(alpha = 0.45f),
                        topLeft = Offset((w - botW) / 2, h - rimH * 0.75f),
                        size = androidx.compose.ui.geometry.Size(botW, rimH * 0.6f),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )

                    // Top rim: white opening ellipse
                    drawOval(
                        color = Color(0xFFEAF6F6),
                        topLeft = Offset((w - topW) / 2, 0f),
                        size = androidx.compose.ui.geometry.Size(topW, rimH),
                    )
                    drawOval(
                        color = Color(0xFFC7DEDE),
                        topLeft = Offset((w - topW * 0.82f) / 2, rimH * 0.14f),
                        size = androidx.compose.ui.geometry.Size(topW * 0.82f, rimH * 0.72f),
                    )

                    // Dimmed shuffle: darken the whole cup
                    if (dimmed) {
                        drawRect(color = Color.Black.copy(alpha = 0.55f))
                    }
            }
        }
    }
}

/** A glowing ball used on the home screen and level cards. */
@Composable
fun GlowBall(
    ball: BallColor,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        val r = size.toPx() / 2
        val c = Offset(size.toPx() / 2, size.toPx() / 2)
        drawCircle(color = ball.color.copy(alpha = 0.20f), radius = r * 1.9f, center = c)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ball.light, ball.color, ball.dark),
                center = c + Offset(-r * 0.35f, -r * 0.35f),
                radius = r * 1.3f,
            ),
            radius = r,
            center = c,
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = r * 0.18f,
            center = c + Offset(-r * 0.35f, -r * 0.40f),
        )
    }
}
