package com.samyak.neotube.innertube.models.body

import com.samyak.neotube.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)
