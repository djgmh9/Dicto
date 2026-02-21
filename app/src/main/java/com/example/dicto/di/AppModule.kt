package com.example.dicto.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.dicto.data.local.DefaultPreferencesManager
import com.example.dicto.data.local.DefaultWordStorage
import com.example.dicto.data.local.PreferencesManager
import com.example.dicto.data.local.WordStorage
import com.example.dicto.data.repository.ITranslationRepository
import com.example.dicto.data.repository.TranslationRepository
import com.example.dicto.domain.manager.ClipboardManager
import com.example.dicto.domain.manager.FloatingWindowManager
import com.example.dicto.domain.manager.IClipboardManager
import com.example.dicto.domain.manager.IFloatingWindowManager
import com.example.dicto.domain.manager.IPronunciationManager
import com.example.dicto.domain.manager.PronunciationManager
import com.example.dicto.domain.manager.TranslationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * AppModule - Hilt dependency provision
 *
 * Provides singleton instances of managers, repositories, and utilities
 * across the application
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ==================== COROUTINE SCOPE ====================

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    // ==================== DATA LAYER ====================

    @Provides
    @Singleton
    fun provideTranslationRepository(): ITranslationRepository {
        return TranslationRepository()
    }

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return DefaultPreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideWordStorage(
        @ApplicationContext context: Context
    ): WordStorage {
        return DefaultWordStorage(context)
    }

    // ==================== DOMAIN LAYER ====================

    @Provides
    @Singleton
    fun provideTranslationManager(
        repository: ITranslationRepository
    ): TranslationManager {
        return TranslationManager(repository)
    }

    @Provides
    @Singleton
    fun provideClipboardManager(
        preferencesManager: PreferencesManager,
        @ApplicationScope scope: CoroutineScope
    ): IClipboardManager {
        return ClipboardManager(preferencesManager, scope)
    }

    @Provides
    @Singleton
    fun provideFloatingWindowManager(
        @ApplicationContext context: Context
    ): IFloatingWindowManager {
        return FloatingWindowManager(context)
    }

    @Provides
    @Singleton
    fun providePronunciationManager(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope
    ): IPronunciationManager {
        return PronunciationManager(context as Application, scope)
    }
}

/**
 * Qualifier for application-level CoroutineScope
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

