package com.finley.android.qualitytime.model

import kotlinx.serialization.Serializable

@Serializable
data class Poem(
    val id: String,
    val title: String,
    val author: String,
    val dynasty: String,
    val content: String,
    val translation: String = "",
    val explanation: String = "",
    val grade: String = "", // e.g. "幼儿园", "一年级"
    val pinyin: String = "",
    val titlePinyin: String = "",
    val authorPinyin: String = "",
    val dynastyPinyin: String = "",
    val appreciation: String = ""
)

