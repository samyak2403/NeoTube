package com.samyak.neotube.innertube.models.body

import com.samyak.neotube.innertube.models.Context
import com.samyak.neotube.innertube.models.Continuation
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String?,
    val params: String?,
    val continuation: String?
)
