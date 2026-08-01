package com.finley.android.qualitytime.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsService {
    fun getSpeechRate(): Flow<Float>
    suspend fun setSpeechRate(rate: Float)
    fun isAutoPlayEnabled(): Flow<Boolean>
    suspend fun setAutoPlayEnabled(enabled: Boolean)
    fun getSelectedVoiceId(): Flow<String?>
    suspend fun setSelectedVoiceId(id: String)
    fun isShowPinyinEnabled(): Flow<Boolean>
    suspend fun setShowPinyinEnabled(enabled: Boolean)
}

class DataStoreSettingsService(
    private val dataStore: DataStore<Preferences>
) : SettingsService {
    
    private val SPEECH_RATE = floatPreferencesKey("speech_rate")
    private val AUTO_PLAY = booleanPreferencesKey("auto_play")
    private val VOICE_ID = stringPreferencesKey("voice_id")
    private val SHOW_PINYIN = booleanPreferencesKey("show_pinyin")

    override fun getSpeechRate(): Flow<Float> = dataStore.data.map { it[SPEECH_RATE] ?: 1.0f }
    
    override suspend fun setSpeechRate(rate: Float) {
        dataStore.edit { it[SPEECH_RATE] = rate }
    }

    override fun isAutoPlayEnabled(): Flow<Boolean> = dataStore.data.map { it[AUTO_PLAY] ?: false }

    override suspend fun setAutoPlayEnabled(enabled: Boolean) {
        dataStore.edit { it[AUTO_PLAY] = enabled }
    }

    override fun getSelectedVoiceId(): Flow<String?> = dataStore.data.map { it[VOICE_ID] }

    override suspend fun setSelectedVoiceId(id: String) {
        dataStore.edit { it[VOICE_ID] = id }
    }

    override fun isShowPinyinEnabled(): Flow<Boolean> = dataStore.data.map { it[SHOW_PINYIN] ?: true }

    override suspend fun setShowPinyinEnabled(enabled: Boolean) {
        dataStore.edit { it[SHOW_PINYIN] = enabled }
    }
}

expect fun createDataStore(platformContext: Any? = null): DataStore<Preferences>
