package com.abhinav.caisarra.presentation.screens.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.dto.RatingHistoryPoint
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.TextSizeTiny
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val DisplayDateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)

private data class ParsedPoint(
    val dateStr: String,
    val localDate: LocalDate?,
    val timestampMs: Long,
    val rating: Int
)

@Composable
fun RatingGraphView(
    ratingHistory: List<RatingHistoryPoint>,
    startRating: Int,
    currentRating: Int,
    modifier: Modifier = Modifier,
    bestRating: Int? = null,
    bestRatingAt: String? = null,
    lowestRating: Int? = null,
    lowestRatingAt: String? = null
) {
    val textMeasurer = rememberTextMeasurer()
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val density = LocalDensity.current

    val parsedPoints = remember(
        ratingHistory,
        startRating,
        currentRating,
        bestRating,
        bestRatingAt,
        lowestRating,
        lowestRatingAt
    ) {
        fun parseDateAndTimestamp(dateStr: String, fallbackIndex: Int): Pair<LocalDate?, Long> {
            val clean = dateStr.trim()
            val parsedInstant = try {
                Instant.parse(clean)
            } catch (_: Exception) {
                try {
                    val ld = LocalDate.parse(clean.substringBefore('T').substringBefore(' '))
                    ld.atStartOfDay(ZoneId.systemDefault()).toInstant()
                } catch (_: Exception) {
                    null
                }
            }
            val parsedDate = try {
                LocalDate.parse(clean.substringBefore('T').substringBefore(' '))
            } catch (_: Exception) {
                parsedInstant?.atZone(ZoneId.systemDefault())?.toLocalDate()
            }
            val timestamp = parsedInstant?.toEpochMilli() ?: (fallbackIndex * 86_400_000L)
            return parsedDate to timestamp
        }

        val rawPoints = mutableListOf<ParsedPoint>()
        for (i in ratingHistory.indices) {
            val pt = ratingHistory[i]
            val (ld, ts) = parseDateAndTimestamp(pt.date, i + 1)
            rawPoints.add(ParsedPoint(pt.date, ld, ts, pt.rating))
        }

        if (rawPoints.size <= 1) {
            val hasRatingChange = startRating != currentRating ||
                (bestRating != null && bestRating != currentRating) ||
                (lowestRating != null && lowestRating != currentRating)

            if (hasRatingChange) {
                val synthesized = mutableListOf<ParsedPoint>()
                val latestTimestamp = rawPoints.firstOrNull()?.timestampMs ?: System.currentTimeMillis()
                val earliestTimestamp = latestTimestamp - 86_400_000L

                val startDate = Instant.ofEpochMilli(earliestTimestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                synthesized.add(
                    ParsedPoint(
                        dateStr = startDate.toString(),
                        localDate = startDate,
                        timestampMs = earliestTimestamp,
                        rating = startRating
                    )
                )

                if (lowestRating != null && lowestRating != startRating && lowestRating != currentRating) {
                    val (ld, ts) = if (!lowestRatingAt.isNullOrBlank()) {
                        parseDateAndTimestamp(lowestRatingAt, 1)
                    } else {
                        null to (earliestTimestamp + (latestTimestamp - earliestTimestamp) / 3)
                    }
                    synthesized.add(
                        ParsedPoint(
                            dateStr = lowestRatingAt ?: ld?.toString() ?: "",
                            localDate = ld,
                            timestampMs = ts,
                            rating = lowestRating
                        )
                    )
                }

                if (bestRating != null && bestRating != startRating && bestRating != lowestRating && bestRating != currentRating) {
                    val (ld, ts) = if (!bestRatingAt.isNullOrBlank()) {
                        parseDateAndTimestamp(bestRatingAt, 2)
                    } else {
                        null to (earliestTimestamp + (latestTimestamp - earliestTimestamp) * 2 / 3)
                    }
                    synthesized.add(
                        ParsedPoint(
                            dateStr = bestRatingAt ?: ld?.toString() ?: "",
                            localDate = ld,
                            timestampMs = ts,
                            rating = bestRating
                        )
                    )
                }

                val currentLd = rawPoints.firstOrNull()?.localDate ?: LocalDate.now()
                synthesized.add(
                    ParsedPoint(
                        dateStr = rawPoints.firstOrNull()?.dateStr ?: currentLd.toString(),
                        localDate = currentLd,
                        timestampMs = latestTimestamp,
                        rating = currentRating
                    )
                )

                synthesized.sortBy { it.timestampMs }
                rawPoints.clear()
                rawPoints.addAll(synthesized)
            }
        } else {
            if (rawPoints.first().rating != startRating) {
                val firstTs = rawPoints.first().timestampMs
                val startTs = firstTs - 86_400_000L
                val startLd = Instant.ofEpochMilli(startTs).atZone(ZoneId.systemDefault()).toLocalDate()
                rawPoints.add(
                    0,
                    ParsedPoint(
                        dateStr = startLd.toString(),
                        localDate = startLd,
                        timestampMs = startTs,
                        rating = startRating
                    )
                )
            }
        }

        rawPoints
    }

    val ratings = remember(parsedPoints, startRating, currentRating) {
        if (parsedPoints.isNotEmpty()) {
            parsedPoints.map { it.rating } + listOf(startRating, currentRating)
        } else {
            listOf(startRating, currentRating)
        }
    }

    val minR = ratings.minOrNull() ?: 1200
    val maxR = ratings.maxOrNull() ?: 1200
    val diff = (maxR - minR).coerceAtLeast(1)

    val ratingPadding = if (diff < 30) 25 else (diff * 0.15f).roundToInt().coerceAtLeast(15)
    val rawMin = ((minR - ratingPadding) / 10) * 10
    val rawMax = (((maxR + ratingPadding) + 9) / 10) * 10
    val rawMid = (rawMin + rawMax) / 2
    val rangeSpan = (rawMax - rawMin).coerceAtLeast(1)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val leftAxisPadding = with(density) { 48.dp.toPx() }
        val rightPadding = with(density) { 12.dp.toPx() }
        val topPadding = with(density) { 16.dp.toPx() }
        val bottomPadding = with(density) { 32.dp.toPx() }

        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = with(density) { 180.dp.toPx() }

        val chartWidth = (widthPx - leftAxisPadding - rightPadding).coerceAtLeast(10f)
        val chartHeight = (heightPx - topPadding - bottomPadding).coerceAtLeast(10f)

        fun getY(ratingVal: Int): Float {
            val fraction = (ratingVal - rawMin).toFloat() / rangeSpan.toFloat()
            return topPadding + (1f - fraction.coerceIn(0f, 1f)) * chartHeight
        }

        val pointOffsets = remember(parsedPoints, chartWidth, chartHeight, rawMin, rangeSpan, widthPx) {
            if (parsedPoints.isEmpty()) {
                emptyList()
            } else if (parsedPoints.size == 1) {
                listOf(Offset(leftAxisPadding + chartWidth / 2f, getY(parsedPoints.first().rating)))
            } else {
                val minTime = parsedPoints.first().timestampMs
                val maxTime = parsedPoints.last().timestampMs
                val timeSpan = (maxTime - minTime).coerceAtLeast(1L)
                val minSpacing = with(density) { 4.dp.toPx() }

                val offsets = mutableListOf<Offset>()
                for (i in parsedPoints.indices) {
                    val pt = parsedPoints[i]
                    val rawX = if (maxTime == minTime) {
                        leftAxisPadding + (i.toFloat() / (parsedPoints.size - 1)) * chartWidth
                    } else {
                        val frac = (pt.timestampMs - minTime).toFloat() / timeSpan.toFloat()
                        leftAxisPadding + frac.coerceIn(0f, 1f) * chartWidth
                    }
                    val x = if (i > 0) {
                        maxOf(rawX, offsets[i - 1].x + minSpacing).coerceAtMost(widthPx - rightPadding)
                    } else {
                        rawX
                    }
                    offsets.add(Offset(x, getY(pt.rating)))
                }
                offsets
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(pointOffsets) {
                    if (pointOffsets.isEmpty()) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        selectedPointIndex = findClosestPointIndex(down.position.x, pointOffsets)

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) {
                                break
                            }
                            selectedPointIndex = findClosestPointIndex(change.position.x, pointOffsets)
                            change.consume()
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width

                val gridYPositions = listOf(
                    rawMax to topPadding,
                    rawMid to (topPadding + chartHeight / 2f),
                    rawMin to (topPadding + chartHeight)
                )

                val textStyle = TextStyle(
                    fontFamily = JetBrainsMono,
                    fontSize = TextSizeTiny,
                    fontWeight = FontWeight.Medium,
                    color = StatsTextMuted
                )

                for ((ratingVal, yPos) in gridYPositions) {
                    drawLine(
                        color = StatsCardBorder,
                        start = Offset(leftAxisPadding, yPos),
                        end = Offset(width - rightPadding, yPos),
                        strokeWidth = 1.dp.toPx()
                    )

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

                if (parsedPoints.isEmpty() || pointOffsets.isEmpty()) {
                    val baselineY = getY(startRating)
                    drawLine(
                        color = StatsCyan.copy(alpha = 0.4f),
                        start = Offset(leftAxisPadding, baselineY),
                        end = Offset(width - rightPadding, baselineY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    val emptyTextLayout = textMeasurer.measure(
                        text = "No rated games in this period",
                        style = textStyle.copy(color = StatsTextMuted.copy(alpha = 0.7f))
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

                if (pointOffsets.size == 1) {
                    val single = pointOffsets.first()
                    drawLine(
                        color = StatsCyan.copy(alpha = 0.5f),
                        start = Offset(leftAxisPadding, single.y),
                        end = Offset(width - rightPadding, single.y),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = StatsCardBg,
                        radius = 6.dp.toPx(),
                        center = single
                    )
                    drawCircle(
                        color = StatsCyan,
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

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                StatsCyan.copy(alpha = 0.25f),
                                StatsCyan.copy(alpha = 0.02f)
                            ),
                            startY = topPadding,
                            endY = topPadding + chartHeight
                        )
                    )

                    drawPath(
                        path = linePath,
                        color = StatsCyan,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    for (pt in pointOffsets) {
                        drawCircle(
                            color = StatsCardBg,
                            radius = 4.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = StatsCyan,
                            radius = 2.5.dp.toPx(),
                            center = pt
                        )
                    }
                }

                val dateLabels = mutableListOf<Pair<String, Float>>()
                if (parsedPoints.isNotEmpty() && pointOffsets.isNotEmpty()) {
                    val firstPt = parsedPoints.first()
                    val firstDateStr = firstPt.localDate?.format(DisplayDateFormatter) ?: firstPt.dateStr.take(10)
                    val lastPt = parsedPoints.last()
                    val lastDateStr = lastPt.localDate?.format(DisplayDateFormatter) ?: lastPt.dateStr.take(10)

                    if (parsedPoints.size == 1 || firstDateStr == lastDateStr) {
                        val midX = pointOffsets[pointOffsets.size / 2].x
                        dateLabels.add(firstDateStr to midX)
                    } else {
                        dateLabels.add(firstDateStr to pointOffsets.first().x)

                        if (parsedPoints.size >= 3) {
                            val midIndex = parsedPoints.size / 2
                            val midPt = parsedPoints[midIndex]
                            val midDateStr = midPt.localDate?.format(DisplayDateFormatter) ?: midPt.dateStr.take(10)
                            val midX = pointOffsets[midIndex].x
                            val startX = pointOffsets.first().x
                            val endX = pointOffsets.last().x

                            val minSpacingPx = 60.dp.toPx()
                            if (midDateStr != firstDateStr && midDateStr != lastDateStr &&
                                (midX - startX) >= minSpacingPx && (endX - midX) >= minSpacingPx
                            ) {
                                dateLabels.add(midDateStr to midX)
                            }
                        }

                        dateLabels.add(lastDateStr to pointOffsets.last().x)
                    }
                }

                for ((label, xPos) in dateLabels) {
                    val dateLayout = textMeasurer.measure(
                        text = label,
                        style = textStyle.copy(fontSize = 10.sp)
                    )
                    val maxLabelX = (width - rightPadding - dateLayout.size.width).coerceAtLeast(leftAxisPadding)
                    val clampedX = (xPos - dateLayout.size.width / 2f).coerceIn(
                        leftAxisPadding,
                        maxLabelX
                    )
                    drawText(
                        textLayoutResult = dateLayout,
                        topLeft = Offset(
                            x = clampedX,
                            y = topPadding + chartHeight + 8.dp.toPx()
                        )
                    )
                }

                selectedPointIndex?.let { selIdx ->
                    if (selIdx in pointOffsets.indices && selIdx in parsedPoints.indices) {
                        val targetOffset = pointOffsets[selIdx]
                        val selPoint = parsedPoints[selIdx]

                        drawLine(
                            color = StatsCyan.copy(alpha = 0.5f),
                            start = Offset(targetOffset.x, topPadding),
                            end = Offset(targetOffset.x, topPadding + chartHeight),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        drawCircle(
                            color = StatsCyan,
                            radius = 6.dp.toPx(),
                            center = targetOffset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = targetOffset
                        )

                        val formattedDate = selPoint.localDate?.format(DisplayDateFormatter) ?: selPoint.dateStr.take(10)
                        val tooltipText = "${selPoint.rating} ELO • $formattedDate"
                        val tooltipLayout = textMeasurer.measure(
                            text = tooltipText,
                            style = TextStyle(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = TextSizeTiny,
                                color = Color.White
                            )
                        )

                        val tooltipPadding = 6.dp.toPx()
                        val tooltipW = tooltipLayout.size.width + tooltipPadding * 2
                        val tooltipH = tooltipLayout.size.height + tooltipPadding * 2

                        val maxTooltipX = (width - rightPadding - tooltipW).coerceAtLeast(leftAxisPadding)
                        val tooltipX = (targetOffset.x - tooltipW / 2f).coerceIn(
                            leftAxisPadding,
                            maxTooltipX
                        )
                        val tooltipY = (targetOffset.y - tooltipH - 10.dp.toPx()).coerceAtLeast(0f)

                        drawRoundRect(
                            color = StatsCardBg,
                            topLeft = Offset(tooltipX, tooltipY),
                            size = Size(tooltipW, tooltipH),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                        drawRoundRect(
                            color = StatsCyan,
                            topLeft = Offset(tooltipX, tooltipY),
                            size = Size(tooltipW, tooltipH),
                            cornerRadius = CornerRadius(6.dp.toPx()),
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
}

private fun findClosestPointIndex(
    touchX: Float,
    pointOffsets: List<Offset>
): Int? {
    if (pointOffsets.isEmpty()) return null
    if (pointOffsets.size == 1) return 0

    var closestIndex = 0
    var minDistance = Float.MAX_VALUE

    for (i in pointOffsets.indices) {
        val dist = abs(pointOffsets[i].x - touchX)
        if (dist < minDistance) {
            minDistance = dist
            closestIndex = i
        }
    }

    return closestIndex
}
