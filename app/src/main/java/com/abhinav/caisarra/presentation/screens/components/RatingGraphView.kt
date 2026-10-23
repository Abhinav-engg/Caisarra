package com.abhinav.caisarra.presentation.screens.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.dto.RatingHistoryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val CyanPrimary = Color(0xFF22D3EE)
private val GridLineColor = Color(0xFF1B2838)
private val AxisTextColor = Color(0xFF82909E)
private val TooltipBgColor = Color(0xFF0F1722)
private val TooltipBorderColor = Color(0xFF22D3EE)

private val DisplayDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

private data class ParsedPoint(
    val dateStr: String,
    val localDate: LocalDate?,
    val epochDay: Long,
    val rating: Int
)

@Composable
fun RatingGraphView(
    ratingHistory: List<RatingHistoryPoint>,
    startRating: Int,
    currentRating: Int,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    // Parse dates safely
    val parsedPoints = remember(ratingHistory, startRating) {
        ratingHistory.mapIndexed { index, point ->
            val parsedDate = try {
                LocalDate.parse(point.date.trim())
            } catch (_: Exception) {
                null
            }
            val epoch = parsedDate?.toEpochDay() ?: index.toLong()
            ParsedPoint(
                dateStr = point.date,
                localDate = parsedDate,
                epochDay = epoch,
                rating = point.rating
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(parsedPoints) {
                if (parsedPoints.isEmpty()) return@pointerInput

                detectTapGestures(
                    onPress = { offset ->
                        selectedPointIndex = findClosestPointIndex(offset.x, size.width.toFloat(), parsedPoints)
                    },
                    onTap = {
                        // Keep or dismiss on tap
                    }
                )
            }
            .pointerInput(parsedPoints) {
                if (parsedPoints.isEmpty()) return@pointerInput

                detectDragGestures(
                    onDragStart = { offset ->
                        selectedPointIndex = findClosestPointIndex(offset.x, size.width.toFloat(), parsedPoints)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        selectedPointIndex = findClosestPointIndex(change.position.x, size.width.toFloat(), parsedPoints)
                    },
                    onDragEnd = {
                        // keep selection or clear after short delay
                    },
                    onDragCancel = {
                        selectedPointIndex = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val leftAxisPadding = 48.dp.toPx()
            val rightPadding = 12.dp.toPx()
            val topPadding = 16.dp.toPx()
            val bottomPadding = 32.dp.toPx()

            val chartWidth = (width - leftAxisPadding - rightPadding).coerceAtLeast(10f)
            val chartHeight = (height - topPadding - bottomPadding).coerceAtLeast(10f)

            // Rating Bounds
            val ratings = if (parsedPoints.isNotEmpty()) {
                parsedPoints.map { it.rating } + listOf(startRating, currentRating)
            } else {
                listOf(startRating, currentRating)
            }

            val minR = ratings.minOrNull() ?: 1200
            val maxR = ratings.maxOrNull() ?: 1200
            val diff = (maxR - minR).coerceAtLeast(1)

            val ratingPadding = if (diff < 30) 25 else (diff * 0.15f).roundToInt().coerceAtLeast(15)
            val rawMin = ((minR - ratingPadding) / 10) * 10
            val rawMax = (((maxR + ratingPadding) + 9) / 10) * 10
            val rawMid = (rawMin + rawMax) / 2

            val rangeSpan = (rawMax - rawMin).coerceAtLeast(1)

            fun getY(ratingVal: Int): Float {
                val fraction = (ratingVal - rawMin).toFloat() / rangeSpan.toFloat()
                return topPadding + (1f - fraction.coerceIn(0f, 1f)) * chartHeight
            }

            // Draw horizontal gridlines & Y labels
            val gridYPositions = listOf(
                rawMax to topPadding,
                rawMid to (topPadding + chartHeight / 2f),
                rawMin to (topPadding + chartHeight)
            )

            val textStyle = TextStyle(
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = AxisTextColor
            )

            for ((ratingVal, yPos) in gridYPositions) {
                // Grid line
                drawLine(
                    color = GridLineColor,
                    start = Offset(leftAxisPadding, yPos),
                    end = Offset(width - rightPadding, yPos),
                    strokeWidth = 1.dp.toPx()
                )

                // Y Axis text
                val textLayout = textMeasurer.measure(
                    text = ratingVal.toString(),
                    style = textStyle
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        x = 4.dp.toPx(),
                        y = yPos - (textLayout.size.height / 2f)
                    )
                )
            }

            if (parsedPoints.isEmpty()) {
                // Empty state: flat line at startRating
                val baselineY = getY(startRating)
                drawLine(
                    color = CyanPrimary.copy(alpha = 0.4f),
                    start = Offset(leftAxisPadding, baselineY),
                    end = Offset(width - rightPadding, baselineY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                val emptyTextLayout = textMeasurer.measure(
                    text = "No rated games in this period",
                    style = textStyle.copy(color = AxisTextColor.copy(alpha = 0.7f))
                )
                drawText(
                    textLayoutResult = emptyTextLayout,
                    topLeft = Offset(
                        x = leftAxisPadding + (chartWidth - emptyTextLayout.size.width) / 2f,
                        y = topPadding + (chartHeight - emptyTextLayout.size.height) / 2f
                    )
                )
                return@Canvas
            }

            // Calculate X positions for parsedPoints
            val minEpoch = parsedPoints.first().epochDay
            val maxEpoch = parsedPoints.last().epochDay
            val epochSpan = (maxEpoch - minEpoch).coerceAtLeast(1L)

            fun getX(epochDay: Long, index: Int): Float {
                return if (parsedPoints.size == 1 || maxEpoch == minEpoch) {
                    leftAxisPadding + chartWidth / 2f
                } else {
                    val frac = (epochDay - minEpoch).toFloat() / epochSpan.toFloat()
                    leftAxisPadding + frac.coerceIn(0f, 1f) * chartWidth
                }
            }

            val pointOffsets = parsedPoints.mapIndexed { index, p ->
                Offset(
                    x = getX(p.epochDay, index),
                    y = getY(p.rating)
                )
            }

            // Draw line and gradient
            if (pointOffsets.size == 1) {
                val single = pointOffsets.first()
                drawLine(
                    color = CyanPrimary.copy(alpha = 0.5f),
                    start = Offset(leftAxisPadding, single.y),
                    end = Offset(width - rightPadding, single.y),
                    strokeWidth = 2.dp.toPx()
                )
                drawCircle(
                    color = TooltipBgColor,
                    radius = 6.dp.toPx(),
                    center = single
                )
                drawCircle(
                    color = CyanPrimary,
                    radius = 4.dp.toPx(),
                    center = single
                )
            } else {
                val linePath = Path()
                val fillPath = Path()

                linePath.moveTo(pointOffsets.first().x, pointOffsets.first().y)
                fillPath.moveTo(pointOffsets.first().x, topPadding + chartHeight)
                fillPath.lineTo(pointOffsets.first().x, pointOffsets.first().y)

                for (i in 1 until pointOffsets.size) {
                    val p = pointOffsets[i]
                    linePath.lineTo(p.x, p.y)
                    fillPath.lineTo(p.x, p.y)
                }

                fillPath.lineTo(pointOffsets.last().x, topPadding + chartHeight)
                fillPath.close()

                // Gradient fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CyanPrimary.copy(alpha = 0.25f),
                            CyanPrimary.copy(alpha = 0.02f)
                        ),
                        startY = topPadding,
                        endY = topPadding + chartHeight
                    )
                )

                // Neon stroke line
                drawPath(
                    path = linePath,
                    color = CyanPrimary,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw dots at each point
                for (pt in pointOffsets) {
                    drawCircle(
                        color = TooltipBgColor,
                        radius = 4.5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = CyanPrimary,
                        radius = 2.5.dp.toPx(),
                        center = pt
                    )
                }
            }

            // Draw X-axis date labels
            val dateLabels = mutableListOf<Pair<String, Float>>()
            if (parsedPoints.isNotEmpty()) {
                val firstPt = parsedPoints.first()
                val firstDateStr = firstPt.localDate?.format(DisplayDateFormatter) ?: firstPt.dateStr
                dateLabels.add(firstDateStr to pointOffsets.first().x)

                if (parsedPoints.size >= 3) {
                    val midIndex = parsedPoints.size / 2
                    val midPt = parsedPoints[midIndex]
                    val midDateStr = midPt.localDate?.format(DisplayDateFormatter) ?: midPt.dateStr
                    dateLabels.add(midDateStr to pointOffsets[midIndex].x)
                }

                if (parsedPoints.size >= 2) {
                    val lastPt = parsedPoints.last()
                    val lastDateStr = lastPt.localDate?.format(DisplayDateFormatter) ?: lastPt.dateStr
                    dateLabels.add(lastDateStr to pointOffsets.last().x)
                }
            }

            for ((label, xPos) in dateLabels) {
                val dateLayout = textMeasurer.measure(
                    text = label,
                    style = textStyle.copy(fontSize = 10.sp)
                )
                val clampedX = (xPos - dateLayout.size.width / 2f).coerceIn(
                    leftAxisPadding,
                    width - rightPadding - dateLayout.size.width
                )
                drawText(
                    textLayoutResult = dateLayout,
                    topLeft = Offset(
                        x = clampedX,
                        y = topPadding + chartHeight + 8.dp.toPx()
                    )
                )
            }

            // Draw interactive inspection tooltip if point selected
            selectedPointIndex?.let { selIdx ->
                if (selIdx in pointOffsets.indices) {
                    val targetOffset = pointOffsets[selIdx]
                    val selPoint = parsedPoints[selIdx]

                    // Vertical guide line
                    drawLine(
                        color = CyanPrimary.copy(alpha = 0.5f),
                        start = Offset(targetOffset.x, topPadding),
                        end = Offset(targetOffset.x, topPadding + chartHeight),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // Big glowing center circle
                    drawCircle(
                        color = CyanPrimary,
                        radius = 6.dp.toPx(),
                        center = targetOffset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = targetOffset
                    )

                    // Tooltip text
                    val formattedDate = selPoint.localDate?.format(DisplayDateFormatter) ?: selPoint.dateStr
                    val tooltipText = "${selPoint.rating} ELO • $formattedDate"
                    val tooltipLayout = textMeasurer.measure(
                        text = tooltipText,
                        style = TextStyle(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    )

                    val tooltipPadding = 6.dp.toPx()
                    val tooltipW = tooltipLayout.size.width + tooltipPadding * 2
                    val tooltipH = tooltipLayout.size.height + tooltipPadding * 2

                    val tooltipX = (targetOffset.x - tooltipW / 2f).coerceIn(
                        leftAxisPadding,
                        width - rightPadding - tooltipW
                    )
                    val tooltipY = (targetOffset.y - tooltipH - 10.dp.toPx()).coerceAtLeast(0f)

                    drawRoundRect(
                        color = TooltipBgColor,
                        topLeft = Offset(tooltipX, tooltipY),
                        size = androidx.compose.ui.geometry.Size(tooltipW, tooltipH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                    )
                    drawRoundRect(
                        color = TooltipBorderColor,
                        topLeft = Offset(tooltipX, tooltipY),
                        size = androidx.compose.ui.geometry.Size(tooltipW, tooltipH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawText(
                        textLayoutResult = tooltipLayout,
                        topLeft = Offset(tooltipX + tooltipPadding, tooltipY + tooltipPadding)
                    )
                }
            }
        }
    }
}

private fun findClosestPointIndex(
    touchX: Float,
    totalWidth: Float,
    points: List<ParsedPoint>
): Int? {
    if (points.isEmpty()) return null
    if (points.size == 1) return 0

    val leftPadding = 48f
    val rightPadding = 12f
    val chartWidth = (totalWidth - leftPadding - rightPadding).coerceAtLeast(1f)

    val minEpoch = points.first().epochDay
    val maxEpoch = points.last().epochDay
    val epochSpan = (maxEpoch - minEpoch).coerceAtLeast(1L)

    var closestIndex = 0
    var minDistance = Float.MAX_VALUE

    for (i in points.indices) {
        val frac = (points[i].epochDay - minEpoch).toFloat() / epochSpan.toFloat()
        val ptX = leftPadding + frac * chartWidth
        val dist = abs(ptX - touchX)
        if (dist < minDistance) {
            minDistance = dist
            closestIndex = i
        }
    }

    return closestIndex
}
