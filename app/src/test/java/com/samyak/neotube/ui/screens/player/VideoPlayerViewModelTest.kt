package com.samyak.neotube.ui.screens.player

import android.content.Context
import android.util.Log
import com.samyak.neotube.data.video.VideoDownloadManager
import com.samyak.neotube.data.video.DownloadedVideo
import com.samyak.neotube.data.local.*
import com.samyak.neotube.data.recommendation.InterestProfile
import com.samyak.neotube.data.recommendation.NeoTubeNeuroEngine
import com.samyak.neotube.data.repository.YouTubeRepository
import com.samyak.neotube.data.local.VideoQuality
import com.samyak.neotube.utils.PerformanceDispatcher
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.stream.StreamInfo

@OptIn(ExperimentalCoroutinesApi::class)
class VideoPlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val repository: YouTubeRepository = mockk(relaxed = true)
    private val viewHistory: ViewHistory = mockk(relaxed = true)
    private val subscriptionRepository: SubscriptionRepository = mockk(relaxed = true)
    private val likedVideosRepository: LikedVideosRepository = mockk(relaxed = true)
    private val playlistRepository: PlaylistRepository = mockk(relaxed = true)
    private val interestProfile: InterestProfile = mockk(relaxed = true)
    private val playerPreferences: PlayerPreferences = mockk(relaxed = true)
    private val videoDownloadManager: VideoDownloadManager = mockk(relaxed = true)

    private lateinit var viewModel: VideoPlayerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0

        mockkObject(NeoTubeNeuroEngine)
        mockkObject(PerformanceDispatcher)
        every { PerformanceDispatcher.networkIO } returns testDispatcher
        every { PerformanceDispatcher.diskIO } returns testDispatcher
        
        // Default mocks for preferences
        every { playerPreferences.defaultQualityWifi } returns flowOf(VideoQuality.Q_720p)
        every { playerPreferences.defaultQualityCellular } returns flowOf(VideoQuality.Q_360p)
        every { playerPreferences.autoplayEnabled } returns flowOf(true)
        every { playerPreferences.miniPlayerContinueWatchingEnabled } returns flowOf(false)
        every { playerPreferences.preferredAudioLanguage } returns flowOf("")
        every { playerPreferences.returnYouTubeDislikeEnabled } returns flowOf(false)
        every { videoDownloadManager.downloadedVideos } returns flowOf(emptyList<DownloadedVideo>())

        viewModel = VideoPlayerViewModel(
            context, repository, viewHistory, subscriptionRepository,
            likedVideosRepository, playlistRepository, interestProfile,
            playerPreferences, videoDownloadManager
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state is correct`() {
        val state = viewModel.uiState.value
        assertThat(state.streamInfo).isNull()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `loadVideoInfo updates state to loading`() = runTest {
        coEvery { repository.getVideoStreamInfo(any()) } coAnswers {
            delay(1000)
            mockk<StreamInfo>(relaxed = true)
        }
        
        viewModel.loadVideoInfo("test_vid")
        
        assertThat(viewModel.uiState.value.isLoading).isTrue()
        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `loadVideoInfo sets error when repository fails`() = runTest {
        coEvery { repository.getVideoStreamInfo(any()) } throws Exception("Network error")
        
        viewModel.loadVideoInfo("test_vid")
        advanceUntilIdle()
        
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.error).isNotNull()
    }

    @Test
    fun `toggleSubscription updates UI and repo`() = runTest {
        coEvery { subscriptionRepository.isSubscribed("chan1") } returns flowOf(false)
        coEvery { subscriptionRepository.subscribe(any()) } just Runs
        
        viewModel.toggleSubscription("chan1", "Channel 1", "avatar")
        advanceUntilIdle()
        
        coVerify { subscriptionRepository.subscribe(match { it.channelId == "chan1" }) }
        assertThat(viewModel.uiState.value.isSubscribed).isTrue()
    }

    @Test
    fun `switchQuality updates uiState`() = runTest {
        val streamInfo = mockk<StreamInfo>(relaxed = true)
        val videoStream = mockk<org.schabi.newpipe.extractor.stream.VideoStream>(relaxed = true)
        every { videoStream.height } returns 1080
        every { videoStream.format } returns MediaFormat.MPEG_4
        every { streamInfo.videoStreams } returns listOf(videoStream)
        every { streamInfo.videoOnlyStreams } returns emptyList()
        
        val field = VideoPlayerViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<VideoPlayerUiState>
        stateFlow.value = stateFlow.value.copy(streamInfo = streamInfo)
        
        viewModel.switchQuality(VideoQuality.Q_1080p)
        advanceUntilIdle()
        
        assertThat(viewModel.uiState.value.selectedQuality).isEqualTo(VideoQuality.Q_1080p)
        assertThat(viewModel.uiState.value.isAdaptiveMode).isFalse()
    }
}
