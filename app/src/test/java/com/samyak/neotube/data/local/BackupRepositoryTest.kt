package com.samyak.neotube.data.local

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.samyak.neotube.data.local.dao.PlaylistDao
import com.samyak.neotube.data.local.dao.VideoDao
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import androidx.room.withTransaction
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class BackupRepositoryTest {

    private val context: Context = mockk(relaxed = true)
    private val contentResolver: ContentResolver = mockk()
    private val viewHistory: ViewHistory = mockk()
    private val subscriptionRepo: SubscriptionRepository = mockk()
    private val likedVideosRepo: LikedVideosRepository = mockk()
    private val appDatabase: AppDatabase = mockk()
    private val playlistDao: PlaylistDao = mockk()
    private val videoDao: VideoDao = mockk()

    private lateinit var repository: BackupRepository

    @Before
    fun setup() {
        every { context.contentResolver } returns contentResolver
        
        mockkObject(ViewHistory)
        every { ViewHistory.getInstance(any()) } returns viewHistory
        
        mockkObject(SubscriptionRepository)
        every { SubscriptionRepository.getInstance(any()) } returns subscriptionRepo

        mockkObject(LikedVideosRepository)
        every { LikedVideosRepository.getInstance(any()) } returns likedVideosRepo
        every { likedVideosRepo.getAllLikedVideos() } returns flowOf(emptyList())
        
        mockkObject(AppDatabase)
        every { AppDatabase.getDatabase(any()) } returns appDatabase
        every { appDatabase.playlistDao() } returns playlistDao
        every { appDatabase.videoDao() } returns videoDao
        
        // Mock constructor-created repos
        mockkConstructor(SearchHistoryRepository::class)
        coEvery { anyConstructed<SearchHistoryRepository>().saveSearchQuery(any(), any()) } just Runs
        every { anyConstructed<SearchHistoryRepository>().getSearchHistoryFlow() } returns flowOf(emptyList())

        mockkConstructor(PlayerPreferences::class)
        coEvery { anyConstructed<PlayerPreferences>().getExportData() } returns SettingsBackup()

        mockkConstructor(LocalDataManager::class)
        coEvery { anyConstructed<LocalDataManager>().getExportData() } returns SettingsBackup()

        // Mock Room withTransaction
        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionSlot = slot<suspend () -> Any>()
        coEvery { appDatabase.withTransaction(capture(transactionSlot)) } coAnswers {
            transactionSlot.captured.invoke()
        }

        repository = BackupRepository(context)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `exportData writes JSON to stream`() = runTest {
        val uri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        
        every { contentResolver.openOutputStream(uri) } returns outputStream
        every { viewHistory.getAllHistory() } returns flowOf(emptyList())
        every { subscriptionRepo.getAllSubscriptions() } returns flowOf(emptyList())
        coEvery { playlistDao.getAllPlaylists() } returns flowOf(emptyList())
        coEvery { playlistDao.getAllPlaylistVideoCrossRefs() } returns emptyList()
        coEvery { videoDao.getAllVideos() } returns emptyList()

        val result = repository.exportData(uri)
        
        assertThat(result.isSuccess).isTrue()
        verify { contentResolver.openOutputStream(uri) }
    }

    @Test
    fun `importData parses JSON and calls save methods`() = runTest {
        val uri = mockk<Uri>()
        val json = """{
            "version":2,
            "viewHistory":[{
                "videoId":"v1",
                "title":"T1",
                "thumbnailUrl":"thumb",
                "position":100,
                "duration":200,
                "timestamp":12345,
                "channelName":"Channel",
                "channelId":"c1",
                "isMusic":false
            }]
        }"""
        val inputStream = ByteArrayInputStream(json.toByteArray())
        
        every { contentResolver.openInputStream(uri) } returns inputStream
        coEvery { viewHistory.bulkSaveHistoryEntries(any()) } just Runs
        
        val result = repository.importData(uri)
        
        assertThat(result.isSuccess).isTrue()
        coVerify { viewHistory.bulkSaveHistoryEntries(any()) }
    }
}
