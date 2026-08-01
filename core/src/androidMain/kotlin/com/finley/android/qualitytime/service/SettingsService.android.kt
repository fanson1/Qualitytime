package com.finley.android.qualitytime.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

actual fun createDataStore(platformContext: Any?): DataStore<Preferences> {
    val context = platformContext as? Context ?: throw IllegalArgumentException("Android context required")
    return androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("settings") }
    )
}
