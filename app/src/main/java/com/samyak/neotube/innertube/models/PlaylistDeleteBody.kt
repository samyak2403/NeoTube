package com.samyak.neotube.innertube.models.body

import com.samyak.neotube.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDeleteBody(
    val context: Context,
    val playlistId: String
)
