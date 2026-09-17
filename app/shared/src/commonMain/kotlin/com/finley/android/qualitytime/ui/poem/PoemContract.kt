package com.finley.android.qualitytime.ui.poem

import com.finley.android.qualitytime.model.Poem
import com.finley.android.qualitytime.service.TtsVoice

/** The "no filter selected" sentinel shared across grade / dynasty / author filters. */
const val FILTER_ALL = "全部"

data class CategoryOption(
    val name: String,
    val count: Int
)

data class SortOption(
    val key: String,
    val label: String
)

val sortOptions = listOf(
    SortOption(SortKey.DEFAULT, "默认"),
    SortOption(SortKey.TITLE, "标题"),
    SortOption(SortKey.GRADE, "年级"),
    SortOption(SortKey.DYNASTY, "朝代"),
    SortOption(SortKey.AUTHOR, "作者")
)

/** Well-known sort keys used by [PoemFilterEngine] and the sort UI. */
object SortKey {
    const val DEFAULT = "default"
    const val TITLE = "title"
    const val GRADE = "grade"
    const val DYNASTY = "dynasty"
    const val AUTHOR = "author"
}

data class PoemState(
    /** Raw, unfiltered poem library. */
    val poems: List<Poem> = emptyList(),
    /** Result of the current filter / sort / search pipeline. */
    val filteredPoems: List<Poem> = emptyList(),
    val selectedPoem: Poem? = null,
    val isLoading: Boolean = false,
    /** Non-null when loading the poem library failed; shows a retry screen. */
    val loadError: String? = null,
    val isSpeaking: Boolean = false,
    val highlightIndex: Int = -1,
    val speechRate: Float = 1.0f,
    val isAutoPlay: Boolean = false,
    val selectedGrade: String = FILTER_ALL,
    val grades: List<CategoryOption> = emptyList(),
    val searchQuery: String = "",
    val showPinyin: Boolean = true,
    val selectedAuthor: String = FILTER_ALL,
    val selectedDynasty: String = FILTER_ALL,
    val authors: List<CategoryOption> = emptyList(),
    val dynasties: List<CategoryOption> = emptyList(),
    val activeFilterCount: Int = 0,
    val availableVoices: List<TtsVoice> = emptyList(),
    val selectedVoiceId: String? = null,
    val isShowingSettings: Boolean = false,
    val isSplashComplete: Boolean = false,
    val selectedSort: String = SortKey.DEFAULT,
    val sortAscending: Boolean = true,
    val letterIndexMap: Map<String, Int> = emptyMap()
)

sealed interface PoemIntent {
    data object LoadPoems : PoemIntent
    data object RetryLoad : PoemIntent
    data class SelectPoem(val poem: Poem?) : PoemIntent
    data class Speak(val poem: Poem) : PoemIntent
    data object StopSpeaking : PoemIntent
    data class UpdateHighlight(val index: Int) : PoemIntent
    data class ChangeSpeechRate(val rate: Float) : PoemIntent
    data class ToggleAutoPlay(val enabled: Boolean) : PoemIntent
    data class OnPlaybackDone(val utteranceId: String) : PoemIntent
    data class FilterByGrade(val grade: String) : PoemIntent
    data class SearchPoems(val query: String) : PoemIntent
    data object TogglePinyin : PoemIntent
    data class FilterByAuthor(val author: String) : PoemIntent
    data class FilterByDynasty(val dynasty: String) : PoemIntent
    data object ResetFilters : PoemIntent
    data class ChangeVoice(val id: String) : PoemIntent
    data class NavigateToSettings(val show: Boolean) : PoemIntent
    data object SplashComplete : PoemIntent
    data object RefreshVoices : PoemIntent
    data class SortPoems(val sortKey: String) : PoemIntent
    data object ToggleSortOrder : PoemIntent
}

sealed interface PoemEffect {
    data class ShowError(val message: String) : PoemEffect
}