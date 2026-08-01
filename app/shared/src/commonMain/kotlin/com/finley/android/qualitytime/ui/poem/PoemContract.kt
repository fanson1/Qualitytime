package com.finley.android.qualitytime.ui.poem

import com.finley.android.qualitytime.model.Poem
import com.finley.android.qualitytime.service.TtsVoice

data class CategoryOption(
    val name: String,
    val count: Int
)

data class SortOption(
    val key: String,
    val label: String
)

val sortOptions = listOf(
    SortOption("default", "默认"),
    SortOption("title", "标题"),
    SortOption("grade", "年级"),
    SortOption("dynasty", "朝代"),
    SortOption("author", "作者")
)

data class PoemState(
    val poems: List<Poem> = emptyList(),
    val filteredPoems: List<Poem> = emptyList(),
    val selectedPoem: Poem? = null,
    val isLoading: Boolean = false,
    val isSpeaking: Boolean = false,
    val highlightIndex: Int = -1,
    val speechRate: Float = 1.0f,
    val isAutoPlay: Boolean = false,
    val selectedGrade: String = "全部",
    val grades: List<CategoryOption> = emptyList(),
    val searchQuery: String = "",
    val showPinyin: Boolean = true,
    val selectedAuthor: String = "全部",
    val selectedDynasty: String = "全部",
    val authors: List<CategoryOption> = emptyList(),
    val dynasties: List<CategoryOption> = emptyList(),
    val activeFilterCount: Int = 0,
    val availableVoices: List<TtsVoice> = emptyList(),
    val selectedVoiceId: String? = null,
    val isShowingSettings: Boolean = false,
    val isSplashComplete: Boolean = false,
    val selectedSort: String = "default",
    val sortAscending: Boolean = true,
    val letterIndexMap: Map<String, Int> = emptyMap()
)

sealed interface PoemIntent {
    data object LoadPoems : PoemIntent
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
