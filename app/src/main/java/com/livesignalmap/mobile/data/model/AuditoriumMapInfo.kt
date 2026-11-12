package com.livesignalmap.mobile.data.model

data class AuditoriumMapInfo(
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)
val auditoriumsMapData = listOf(
    // Координаты взяты из твоего SVG
    AuditoriumMapInfo("131", 122f, 43f, 43f, 32f),
    AuditoriumMapInfo("133", 168f, 44f, 19f, 33f),
    AuditoriumMapInfo("135", 186f, 45f, 13f, 30f),
    AuditoriumMapInfo("139", 211f, 45f, 18f, 32f),
    AuditoriumMapInfo("141", 228f, 44f, 16f, 32f),
    AuditoriumMapInfo("143", 246f, 45f, 16f, 34f),
    AuditoriumMapInfo("145а", 264f, 45f, 14f, 29f),
    AuditoriumMapInfo("145б", 277f, 44f, 17f, 30f),
    AuditoriumMapInfo("147", 296f, 45f, 21f, 28f),
    AuditoriumMapInfo("149", 318f, 46f, 16f, 28f),
    AuditoriumMapInfo("119", 34f, 113f, 28f, 11f),
    AuditoriumMapInfo("121", 36f, 90f, 26f, 23f),
    AuditoriumMapInfo("123", 33f, 66f, 30f, 24f),
    AuditoriumMapInfo("125", 35f, 44f, 26f, 19f),
    AuditoriumMapInfo("127", 33f, 22f, 68f, 21f)
)