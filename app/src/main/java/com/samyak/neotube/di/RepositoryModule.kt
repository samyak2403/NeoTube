package com.samyak.neotube.di

import android.content.Context
import com.samyak.neotube.data.local.PlayerPreferences
import com.samyak.neotube.data.repository.YouTubeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideYouTubeRepository(playerPreferences: PlayerPreferences): YouTubeRepository {
        return YouTubeRepository.getInstance(playerPreferences)
    }

    @Provides
    @Singleton
    fun provideSubscriptionRepository(@ApplicationContext context: Context): com.samyak.neotube.data.local.SubscriptionRepository {
        return com.samyak.neotube.data.local.SubscriptionRepository.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideLikedVideosRepository(@ApplicationContext context: Context): com.samyak.neotube.data.local.LikedVideosRepository {
        return com.samyak.neotube.data.local.LikedVideosRepository.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideViewHistory(@ApplicationContext context: Context): com.samyak.neotube.data.local.ViewHistory {
        return com.samyak.neotube.data.local.ViewHistory.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideInterestProfile(@ApplicationContext context: Context): com.samyak.neotube.data.recommendation.InterestProfile {
        return com.samyak.neotube.data.recommendation.InterestProfile.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideMusicPlaylistRepository(@ApplicationContext context: Context): com.samyak.neotube.data.music.PlaylistRepository {
        return com.samyak.neotube.data.music.PlaylistRepository(context)
    }


    // VideoDownloadManager is now @Singleton @Inject — Hilt provides it automatically
    @Provides
    @Singleton
    fun providePlayerPreferences(@ApplicationContext context: Context): com.samyak.neotube.data.local.PlayerPreferences {
        return com.samyak.neotube.data.local.PlayerPreferences(context)
    }

    @Provides
    @Singleton
    fun provideShortsRepository(@ApplicationContext context: Context): com.samyak.neotube.data.shorts.ShortsRepository {
        return com.samyak.neotube.data.shorts.ShortsRepository.getInstance(context)
    }
}
