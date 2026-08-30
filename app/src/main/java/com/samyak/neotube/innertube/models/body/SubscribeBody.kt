package com.samyak.neotube.innertube.models.body

import com.samyak.neotube.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SubscribeBody(
    val channelIds: List<String>,
    val context: Context,
)
