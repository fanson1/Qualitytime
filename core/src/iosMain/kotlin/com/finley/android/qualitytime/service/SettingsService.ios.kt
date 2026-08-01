package com.finley.android.qualitytime.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.cinterop.ExperimentalEncodingApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import okio.Path.Companion.toPath

actual fun createDataStore(platformContext: Any?): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        producePath = {
            val documentDirectory: NSURL = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null
            )!!
            (requireNotNull(documentDirectory.path) + "/settings.preferences_pb").toPath()
        }
    )
}
