package com.samyak.neotube.innertube.pages

import com.samyak.neotube.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
