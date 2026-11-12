
package com.livesignalmap.mobile.data.model

import androidx.compose.ui.geometry.Offset

data class FloorRegion(
    val id: String,
    val number: String?,
    val polygon: List<Offset>,
    val labelPosition: Offset,
    val type: String,
    val verified: Boolean
)
