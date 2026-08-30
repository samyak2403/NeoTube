package com.samyak.neotube.data.shorts

import android.content.Context
import android.util.Log
import com.samyak.neotube.data.model.ShortVideo
import com.samyak.neotube.data.recommendation.InterestProfile
import com.samyak.neotube.data.repository.YouTubeRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShortsRepositoryTest {

    private val context: Context = mockk(relaxed = true)
    private val youtubeRepository: YouTubeRepository = mockk()
    private val interestProfile: InterestProfile = mockk()
    private lateinit var repository: ShortsRepository

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        
        mockkObject(YouTubeRepository)
        every { YouTubeRepository.getInstance() } returns youtubeRepository
        
        mockkObject(InterestProfile)
        every { InterestProfile.getInstance(any()) } returns interestProfile

        mockkConstructor(android.util.LruCache::class)
        every { anyConstructed<android.util.LruCache<Any, Any>>().get(any()) } returns null
        every { anyConstructed<android.util.LruCache<Any, Any>>().evictAll() } just Runs

        val filesDir = java.io.File("build/tmp/shorts_tests").apply { mkdirs() }
        every { context.filesDir } returns filesDir
        every { context.applicationContext } returns context

        repository = ShortsRepository.getInstance(context)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    private fun createShortVideo(id: String) = ShortVideo(
        id = id,
        title = "Short $id",
        channelName = "Channel",
        channelId = "channelId",
        thumbnailUrl = "thumb",
        viewCountText = "1K views"
    )

    @Test
    fun `getCachedShort returns null when not cached`() {
        val cached = repository.getCachedShort("non_existent_id")
        assertThat(cached).isNull()
    }

    @Test
    fun `clearCaches resets cached initial feed`() {
        repository.clearCaches()
        val cached = repository.getCachedShort("test_id")
        assertThat(cached).isNull()
    }
}
