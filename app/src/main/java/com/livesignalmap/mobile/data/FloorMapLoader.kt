
package com.livesignalmap.mobile.data

import android.content.Context
import androidx.compose.ui.geometry.Offset
import com.livesignalmap.mobile.data.model.FloorRegion
import org.json.JSONObject

object FloorMapLoader {

    fun loadRegions(context: Context): List<FloorRegion> {

        // 1. Читаем JSON из assets
        val jsonText = context.assets
            .open("floor_1_android_ready.json")
            .bufferedReader()
            .use { it.readText() }

        // 2. Получаем массив помещений
        val root = JSONObject(jsonText)
        val rooms = root.getJSONArray("rooms")

        // 3. Преобразуем JSON в Kotlin-объекты
        return List(rooms.length()) { index ->

            val room = rooms.getJSONObject(index)
            val points = room.getJSONArray("polygon")
            val label = room.getJSONArray("labelPosition")

            // Координаты вершин полигона
            val polygon = List(points.length()) { i ->
                val point = points.getJSONArray(i)

                Offset(
                    x = point.getDouble(0).toFloat(),
                    y = point.getDouble(1).toFloat()
                )
            }

            FloorRegion(
                id = room.getString("id"),

                number = room.optString("number")
                    .takeIf {
                        it.isNotBlank() && it != "null"
                    },

                polygon = polygon,

                labelPosition = Offset(
                    x = label.getDouble(0).toFloat(),
                    y = label.getDouble(1).toFloat()
                ),

                type = room.optString("type", "unknown"),
                verified = room.optBoolean("verified", false)
            )
        }
    }
}
