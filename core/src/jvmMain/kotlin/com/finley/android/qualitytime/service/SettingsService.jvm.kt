package com.finley.android.qualitytime.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File

actual fun createDataStore(platformContext: Any?): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        produceFile = { File("settings.preferences_pb") }
    )
}
