package com.finley.android.qualitytime

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform