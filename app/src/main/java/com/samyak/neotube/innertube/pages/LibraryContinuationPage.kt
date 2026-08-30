package com.samyak.neotube.innertube.pages

import com.samyak.neotube.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
