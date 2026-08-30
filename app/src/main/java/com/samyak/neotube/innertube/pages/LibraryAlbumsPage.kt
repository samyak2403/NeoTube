package com.samyak.neotube.innertube.pages

import com.samyak.neotube.innertube.models.Album
import com.samyak.neotube.innertube.models.AlbumItem
import com.samyak.neotube.innertube.models.Artist
import com.samyak.neotube.innertube.models.ArtistItem
import com.samyak.neotube.innertube.models.MusicResponsiveListItemRenderer
import com.samyak.neotube.innertube.models.MusicTwoRowItemRenderer
import com.samyak.neotube.innertube.models.PlaylistItem
import com.samyak.neotube.innertube.models.SongItem
import com.samyak.neotube.innertube.models.YTItem
import com.samyak.neotube.innertube.models.oddElements
import com.samyak.neotube.innertube.utils.parseTime

data class LibraryAlbumsPage(
    val albums: List<AlbumItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
            return AlbumItem(
                        browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
                        playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                            ?.musicPlayButtonRenderer?.playNavigationEndpoint
                            ?.watchPlaylistEndpoint?.playlistId ?: return null,
                        title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                        artists = null,
                        year = renderer.subtitle?.runs?.lastOrNull()?.text?.toIntOrNull(),
                        thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                        explicit = renderer.subtitleBadges?.find {
                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                        } != null
                    )
        }
    }
}
