package com.samyak.neotube.ui.screens.home

import com.samyak.neotube.data.local.SubscriptionRepository
import com.samyak.neotube.data.model.Video
import com.samyak.neotube.data.recommendation.NeoTubeNeuroEngine
import com.samyak.neotube.data.repository.YouTubeRepository
import com.samyak.neotube.data.shorts.ShortsRepository
import com.samyak.neotube.utils.PerformanceDispatcher
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private val repository: YouTubeRepository = mockk(relaxed = true)
    private val subscriptionRepository: SubscriptionRepository = mockk(relaxed = true)
    private val shortsRepository: ShortsRepository = mockk(relaxed = true)
    private val playerPreferences: com.samyak.neotube.data.local.PlayerPreferences = mockk(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        HomeFeedCache.clear()
        
        mockkObject(NeoTubeNeuroEngine)
        mockkObject(PerformanceDispatcher)
        every { PerformanceDispatcher.networkIO } returns testDispatcher
        
        // Mock default behaviors
        coEvery { NeoTubeNeuroEngine.generateDiscoveryQueries() } returns listOf("test")
        coEvery { NeoTubeNeuroEngine.rank(any(), any()) } answers { it.invocation.args[0] as List<Video> }
        
        coEvery { shortsRepository.getHomeFeedShorts() } returns emptyList()
        coEvery { subscriptionRepository.getAllSubscriptionIds() } returns emptySet()
        every { playerPreferences.trendingRegion } returns flowOf("US")
        every { playerPreferences.homeShortsShelfEnabled } returns flowOf(true)
        every { playerPreferences.continueWatchingEnabled } returns flowOf(false)
        
        // Mock repository trending call to return empty by default
        coEvery { repository.getTrendingVideos(any(), any()) } returns Pair(emptyList(), null)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
        HomeFeedCache.clear()
    }

    private fun createVideo(
        id: String,
        title: String = "Title $id",
        duration: Int = 300,
        isShort: Boolean = false,
        isLive: Boolean = false
    ) = Video(
        id = id,
        title = title,
        channelName = "Channel $id",
        channelId = "channelId_$id",
        thumbnailUrl = "thumbnail",
        duration = duration,
        viewCount = 1000L,
        likeCount = 0L,
        uploadDate = "1 day ago",
        isShort = isShort,
        isLive = isLive
    )

    @Test
    fun `initial state is correct`() {
        viewModel = HomeViewModel(repository, subscriptionRepository, shortsRepository, playerPreferences)
        val state = viewModel.uiState.value
        assertThat(state.error).isNull()
        assertThat(state.isRefreshing).isFalse()
    }

    @Test
    fun `loadNeoTubeFeed updates uiState with videos`() = runTest {
        val mockVideos = listOf(
            createVideo(id = "1", title = "Test 1", duration = 300),
            createVideo(id = "2", title = "Test 2", duration = 500)
        )
        
        coEvery { repository.getTrendingVideos(any(), any()) } returns Pair(mockVideos, null)
        
        viewModel = HomeViewModel(repository, subscriptionRepository, shortsRepository, playerPreferences)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.videos).containsExactlyElementsIn(mockVideos)
        assertThat(state.error).isNull()
    }

    @Test
    fun `updateVideosAndShorts filters out short videos correctly`() = runTest {
        viewModel = HomeViewModel(repository, subscriptionRepository, shortsRepository, playerPreferences)
        
        val mixedVideos = listOf(
            createVideo(id = "reg", title = "Regular", duration = 300),
            createVideo(id = "short1", title = "Short 1", duration = 30),
            createVideo(id = "short2", title = "Short 2", duration = 60, isShort = true),
            createVideo(id = "live", title = "Live", duration = 0, isLive = true)
        )
        
        val method = HomeViewModel::class.java.getDeclaredMethod("updateVideosAndShorts", List::class.java, Boolean::class.java)
        method.isAccessible = true
        method.invoke(viewModel, mixedVideos, false)
        
        val state = viewModel.uiState.value
        assertThat(state.videos.map { it.id }).containsExactly("reg", "live")
        assertThat(state.shorts.map { it.id }).containsExactly("short1", "short2")
    }
}
