package com.livesignalmap.mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.livesignalmap.mobile.data.FloorMapLoader
import kotlin.math.min
import com.livesignalmap.mobile.data.model.FloorRegion
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import android.graphics.Paint

private val OffsetSaver = listSaver<Offset, Float>(
    save = { offset ->
        listOf(offset.x, offset.y)
    },
    restore = { values ->
        Offset(
            x = values[0],
            y = values[1]
        )
    }
)
@Composable
fun InteractiveFloorPlan(
    regions: List<FloorRegion>,
    modifier: Modifier = Modifier,
    selectedRegionId: String? = null,
    regionColors: Map<String, Color> = emptyMap(),
    onRegionSelected: (FloorRegion?) -> Unit = {}
) {
    val points = remember(regions) {
        regions.flatMap { it.polygon }
    }

    val bounds = remember(points) {
        if (points.isEmpty()) {
            null
        } else {
            floatArrayOf(
                points.minOf { it.x },
                points.minOf { it.y },
                points.maxOf { it.x },
                points.maxOf { it.y }
            )
        }
    }


    var zoom by rememberSaveable {
        mutableFloatStateOf(1f)
    }

    var pan by rememberSaveable(
        stateSaver = OffsetSaver
    ) {
        mutableStateOf(Offset.Zero)
    }
    val density = LocalDensity.current
    val paddingPx = with(density) { 24.dp.toPx() }


    fun constrainPan(
        proposedPan: Offset,
        currentZoom: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        baseMapWidth: Float,
        baseMapHeight: Float
    ): Offset {

        fun constrainAxis(
            proposed: Float,
            canvasSize: Float,
            mapSize: Float
        ): Float {
            val scaledMapSize = mapSize * currentZoom

            if (scaledMapSize <= canvasSize) {
                return 0f
            }

            val maxPan = (scaledMapSize - canvasSize) / 2f

            return proposed.coerceIn(-maxPan, maxPan)
        }

        return Offset(
            x = constrainAxis(
                proposedPan.x,
                canvasWidth,
                baseMapWidth
            ),
            y = constrainAxis(
                proposedPan.y,
                canvasHeight,
                baseMapHeight
            )
        )
    }
    val labelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(bounds) {
                detectTransformGestures { centroid, panChange, zoomChange, _ ->

                    val oldZoom = zoom
                    val newZoom = (oldZoom * zoomChange)
                        .coerceIn(1f, 8f)

                    val zoomRatio = newZoom / oldZoom

                    val canvasCenter = Offset(
                        x = size.width / 2f,
                        y = size.height / 2f
                    )

                    val newPan = Offset(
                        x = centroid.x - canvasCenter.x -
                                (centroid.x - canvasCenter.x - pan.x) * zoomRatio +
                                panChange.x,

                        y = centroid.y - canvasCenter.y -
                                (centroid.y - canvasCenter.y - pan.y) * zoomRatio +
                                panChange.y
                    )


                    val mapBounds = bounds

                    if (mapBounds != null) {
                        val mapWidth = mapBounds[2] - mapBounds[0]
                        val mapHeight = mapBounds[3] - mapBounds[1]

                        val availableWidth = size.width - paddingPx * 2f
                        val availableHeight = size.height - paddingPx * 2f

                        if (mapWidth > 0f &&
                            mapHeight > 0f &&
                            availableWidth > 0f &&
                            availableHeight > 0f
                        ) {
                            val baseScale = min(
                                availableWidth / mapWidth,
                                availableHeight / mapHeight
                            )

                            pan = constrainPan(
                                proposedPan = newPan,
                                currentZoom = newZoom,
                                canvasWidth = size.width.toFloat(),
                                canvasHeight = size.height.toFloat(),
                                baseMapWidth = mapWidth * baseScale,
                                baseMapHeight = mapHeight * baseScale
                            )
                        }
                    }

                    zoom = newZoom

                }
            }

            .pointerInput(regions, bounds, zoom, pan) {
                detectTapGestures { tapPosition ->
                    val mapBounds = bounds ?: return@detectTapGestures

                    val minX = mapBounds[0]
                    val minY = mapBounds[1]
                    val mapWidth = mapBounds[2] - minX
                    val mapHeight = mapBounds[3] - minY

                    val padding = 24.dp.toPx()
                    val availableWidth = size.width - padding * 2f
                    val availableHeight = size.height - padding * 2f

                    if (mapWidth <= 0f || mapHeight <= 0f ||
                        availableWidth <= 0f || availableHeight <= 0f
                    ) {
                        return@detectTapGestures
                    }

                    val baseScale = min(
                        availableWidth / mapWidth,
                        availableHeight / mapHeight
                    )

                    val centerX = (size.width - mapWidth * baseScale) / 2f
                    val centerY = (size.height - mapHeight * baseScale) / 2f
                    val mapPoint = Offset(
                        x = (
                                (tapPosition.x - size.width / 2f - pan.x) / zoom +
                                        size.width / 2f - centerX
                                ) / baseScale + minX,

                        y = (
                                (tapPosition.y - size.height / 2f - pan.y) / zoom +
                                        size.height / 2f - centerY
                                ) / baseScale + minY
                    )

                    val selectedRegion = regions.lastOrNull { region ->
                        isPointInsidePolygon(mapPoint, region.polygon)
                    }

                    onRegionSelected(selectedRegion)
                }
            }

    ) {
        val mapBounds = bounds ?: return@Canvas

        val minX = mapBounds[0]
        val minY = mapBounds[1]
        val maxX = mapBounds[2]
        val maxY = mapBounds[3]

        val mapWidth = maxX - minX
        val mapHeight = maxY - minY

        if (mapWidth <= 0f || mapHeight <= 0f) return@Canvas

        val padding = 24.dp.toPx()

        val availableWidth = size.width - padding * 2
        val availableHeight = size.height - padding * 2

        if (availableWidth <= 0f || availableHeight <= 0f) return@Canvas

        val baseScale = min(
            availableWidth / mapWidth,
            availableHeight / mapHeight
        )

        val centerX = (size.width - mapWidth * baseScale) / 2f
        val centerY = (size.height - mapHeight * baseScale) / 2f

        fun transform(point: Offset): Offset {
            val initialX = (point.x - minX) * baseScale + centerX
            val initialY = (point.y - minY) * baseScale + centerY

            return Offset(
                x = (initialX - size.width / 2f) * zoom +
                        size.width / 2f + pan.x,

                y = (initialY - size.height / 2f) * zoom +
                        size.height / 2f + pan.y
            )
        }

        regions.forEach { region ->
            if (region.polygon.size < 3) return@forEach

            val path = Path().apply {
                val first = transform(region.polygon.first())
                moveTo(first.x, first.y)

                region.polygon.drop(1).forEach { point ->
                    val transformed = transform(point)
                    lineTo(transformed.x, transformed.y)
                }

                close()
            }

            val isSelected = region.id == selectedRegionId


            val heatmapColor = regionColors[region.id]

            val fillColor = when {
                isSelected -> Color(0xFF2196F3).copy(alpha = 0.45f)
                heatmapColor != null -> heatmapColor.copy(alpha = 0.55f)
                else -> Color(0xFF4A90E2).copy(alpha = 0.15f)
            }

            drawPath(
                path = path,
                color = fillColor
            )


            drawPath(
                path = path,
                color = if (isSelected) {
                    Color(0xFF1565C0)
                } else {
                    Color(0xFF263238)
                },
                style = Stroke(
                    width = if (isSelected) 3.dp.toPx() else 1.5.dp.toPx()
                )
            )

        }
        if (zoom >= 1.5f) {
            drawContext.canvas.nativeCanvas.apply {
                regions.forEach { region ->
                    val number = region.number ?: return@forEach

                    val screenPosition = transform(region.labelPosition)

                    val minX = region.polygon.minOf { it.x }
                    val maxX = region.polygon.maxOf { it.x }


                    val roomWidth = (maxX - minX) * baseScale * zoom


                    val availableWidth = roomWidth * 0.8f


                    val preferredFontSize = (13f * zoom).coerceIn(13f, 36f)

                    labelPaint.textSize = preferredFontSize


                    val textWidth = labelPaint.measureText(number)


                    val fontSize = if (textWidth > availableWidth && textWidth > 0f) {
                        preferredFontSize * (availableWidth / textWidth)
                    } else {
                        preferredFontSize
                    }

                    labelPaint.textSize = fontSize

                    drawText(
                        number,
                        screenPosition.x,
                        screenPosition.y -
                                (labelPaint.ascent() + labelPaint.descent()) / 2f,
                        labelPaint
                    )
                }
            }
        }
    }
}

private fun isPointInsidePolygon(
    point: Offset,
    polygon: List<Offset>
): Boolean {
    if (polygon.size < 3) return false

    var inside = false
    var previousIndex = polygon.lastIndex

    for (index in polygon.indices) {
        val current = polygon[index]
        val previous = polygon[previousIndex]

        val intersects =
            (current.y > point.y) != (previous.y > point.y) &&
                    point.x < (previous.x - current.x) *
                    (point.y - current.y) /
                    (previous.y - current.y) + current.x

        if (intersects) {
            inside = !inside
        }

        previousIndex = index
    }

    return inside
}