package com.finley.android.qualitytime.ui.poem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finley.android.qualitytime.model.Poem
import com.finley.android.qualitytime.service.SettingsService
import com.finley.android.qualitytime.service.TextToSpeechService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import qualitytime.app.shared.generated.resources.Res

class PoemViewModel(
    private val ttsService: TextToSpeechService? = null,
    private val settingsService: SettingsService? = null
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    private val _viewState = MutableStateFlow(PoemState())
    val viewState: StateFlow<PoemState> = _viewState.asStateFlow()

    private val _effect = MutableSharedFlow<PoemEffect>()
    val effect: SharedFlow<PoemEffect> = _effect.asSharedFlow()

    private var lastUtteranceId: String = ""

    init {
        ttsService?.setProgressListener(
            onStart = { utteranceId ->
                val index = utteranceId.toIntOrNull() ?: -1
                handleIntent(PoemIntent.UpdateHighlight(index))
            },
            onDone = { utteranceId ->
                handleIntent(PoemIntent.OnPlaybackDone(utteranceId))
            }
        )
        loadSettings()
        handleIntent(PoemIntent.LoadPoems)
        startSplashTimer()
    }

    private fun startSplashTimer() {
        viewModelScope.launch {
            delay(2000)
            handleIntent(PoemIntent.SplashComplete)
        }
    }

    fun handleIntent(intent: PoemIntent) {
        println("PoemViewModel handleIntent: $intent")
        when (intent) {
            is PoemIntent.LoadPoems -> loadPoems()
            is PoemIntent.SelectPoem -> selectPoem(intent.poem)
            is PoemIntent.Speak -> speak(intent.poem)
            is PoemIntent.StopSpeaking -> stopSpeaking()
            is PoemIntent.UpdateHighlight -> updateHighlight(intent.index)
            is PoemIntent.ChangeSpeechRate -> changeSpeechRate(intent.rate)
            is PoemIntent.ToggleAutoPlay -> toggleAutoPlay(intent.enabled)
            is PoemIntent.OnPlaybackDone -> checkAutoPlayNext(intent.utteranceId)
            is PoemIntent.FilterByGrade -> filterByGrade(intent.grade)
            is PoemIntent.SearchPoems -> searchPoems(intent.query)
            is PoemIntent.TogglePinyin -> togglePinyin()
            is PoemIntent.FilterByAuthor -> filterByAuthor(intent.author)
            is PoemIntent.FilterByDynasty -> filterByDynasty(intent.dynasty)
            is PoemIntent.ResetFilters -> resetFilters()
            is PoemIntent.ChangeVoice -> changeVoice(intent.id)
            is PoemIntent.NavigateToSettings -> navigateToSettings(intent.show)
            is PoemIntent.RefreshVoices -> refreshVoices()
            is PoemIntent.SplashComplete -> {
                refreshVoices()
                _viewState.update { it.copy(isSplashComplete = true) }
            }
            is PoemIntent.SortPoems -> sortPoemsBy(intent.sortKey)
            is PoemIntent.ToggleSortOrder -> toggleSortOrder()
        }
    }

    // ── TTS ──────────────────────────────────────────────────────────────

    private fun refreshVoices() {
        viewModelScope.launch {
            var retries = 5
            while (retries > 0) {
                val voices = ttsService?.getVoices() ?: emptyList()
                if (voices.isNotEmpty()) {
                    _viewState.update { it.copy(availableVoices = voices) }
                    return@launch
                }
                println("TTS voices empty, retrying... ($retries left)")
                delay(1000)
                retries--
            }
        }
    }

    private fun navigateToSettings(show: Boolean) {
        if (show) {
            refreshVoices()
            _viewState.update { it.copy(isShowingSettings = true) }
        } else {
            _viewState.update { it.copy(isShowingSettings = false) }
        }
    }

    private fun changeVoice(id: String) {
        _viewState.update { it.copy(selectedVoiceId = id) }
        ttsService?.setVoice(id)
        ttsService?.speak("你好，这是新的朗读声音", "voice_preview", false)
        viewModelScope.launch { settingsService?.setSelectedVoiceId(id) }
    }

    private fun toggleAutoPlay(enabled: Boolean) {
        _viewState.update { it.copy(isAutoPlay = enabled) }
        viewModelScope.launch { settingsService?.setAutoPlayEnabled(enabled) }
    }

    private fun changeSpeechRate(rate: Float) {
        _viewState.update { it.copy(speechRate = rate) }
        ttsService?.setSpeechRate(rate)
        viewModelScope.launch { settingsService?.setSpeechRate(rate) }
    }

    private fun togglePinyin() {
        val nextEnabled = !viewState.value.showPinyin
        _viewState.update { it.copy(showPinyin = nextEnabled) }
        viewModelScope.launch { settingsService?.setShowPinyinEnabled(nextEnabled) }
    }

    // ── Filter / Sort ────────────────────────────────────────────────────

    private fun sortPoemsBy(sortKey: String) {
        _viewState.update { it.copy(selectedSort = sortKey) }
        reapplyFilters()
    }

    private fun toggleSortOrder() {
        _viewState.update { it.copy(sortAscending = !it.sortAscending) }
        reapplyFilters()
    }

    private fun filterByGrade(grade: String) {
        _viewState.update { it.copy(selectedGrade = grade) }
        reapplyFilters()
    }

    private fun searchPoems(query: String) {
        _viewState.update { it.copy(searchQuery = query) }
        reapplyFilters()
    }

    private fun filterByAuthor(author: String) {
        _viewState.update { it.copy(selectedAuthor = author) }
        reapplyFilters()
    }

    private fun filterByDynasty(dynasty: String) {
        _viewState.update { it.copy(selectedDynasty = dynasty) }
        reapplyFilters()
    }

    private fun resetFilters() {
        _viewState.update {
            it.copy(
                selectedGrade = "全部",
                selectedAuthor = "全部",
                selectedDynasty = "全部",
                searchQuery = ""
            )
        }
        reapplyFilters()
    }

    private fun reapplyFilters() {
        _viewState.update { state ->
            val filtered = PoemFilterEngine.applyFilters(
                poems = state.poems,
                selectedGrade = state.selectedGrade,
                selectedDynasty = state.selectedDynasty,
                selectedAuthor = state.selectedAuthor,
                searchQuery = state.searchQuery
            )
            val sorted = PoemFilterEngine.sortPoems(
                poems = filtered,
                sortKey = state.selectedSort,
                ascending = state.sortAscending
            )
            val filterCount = PoemFilterEngine.countActiveFilters(
                selectedGrade = state.selectedGrade,
                selectedDynasty = state.selectedDynasty,
                selectedAuthor = state.selectedAuthor,
                searchQuery = state.searchQuery
            )
            val letterMap = PoemFilterEngine.computeLetterIndexMap(sorted)
            state.copy(filteredPoems = sorted, activeFilterCount = filterCount, letterIndexMap = letterMap)
        }
    }

    // ── Data Loading ─────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    private fun loadPoems() {
        viewModelScope.launch {
            println("PoemViewModel loading poems from JSON...")
            _viewState.update { it.copy(isLoading = true) }
            try {
                val bytes = Res.readBytes("files/poems.json")
                val poemsJson = bytes.decodeToString()
                val poems = json.decodeFromString<List<Poem>>(poemsJson)

                val categories = PoemFilterEngine.extractCategories(poems)
                val voices = ttsService?.getVoices() ?: emptyList()
                val letterMap = PoemFilterEngine.computeLetterIndexMap(poems)

                _viewState.update {
                    it.copy(
                        poems = poems,
                        filteredPoems = poems,
                        isLoading = false,
                        selectedGrade = "全部",
                        grades = categories.grades,
                        dynasties = categories.dynasties,
                        authors = categories.authors,
                        availableVoices = voices,
                        letterIndexMap = letterMap
                    )
                }
            } catch (e: Exception) {
                println("Error loading poems: ${e.message}")
                _effect.emit(PoemEffect.ShowError("加载古诗库失败: ${e.message}"))
                _viewState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadSettings() {
        settingsService?.let { service ->
            viewModelScope.launch {
                service.getSpeechRate().collect { rate ->
                    _viewState.update { it.copy(speechRate = rate) }
                    ttsService?.setSpeechRate(rate)
                }
            }
            viewModelScope.launch {
                service.isAutoPlayEnabled().collect { enabled ->
                    _viewState.update { it.copy(isAutoPlay = enabled) }
                }
            }
            viewModelScope.launch {
                service.getSelectedVoiceId().collect { id ->
                    _viewState.update { it.copy(selectedVoiceId = id) }
                    id?.let { ttsService?.setVoice(it) }
                }
            }
            viewModelScope.launch {
                service.isShowPinyinEnabled().collect { enabled ->
                    _viewState.update { it.copy(showPinyin = enabled) }
                }
            }
        }
    }

    // ── Poem Selection & TTS ─────────────────────────────────────────────

    private fun selectPoem(poem: Poem?) {
        println("PoemViewModel selectPoem: ${poem?.title}")
        stopSpeaking()
        _viewState.update { it.copy(selectedPoem = poem, highlightIndex = -1) }
    }

    private fun speak(poem: Poem) {
        println("PoemViewModel speak: ${poem.title}, ttsService: $ttsService")
        if (ttsService == null) {
            viewModelScope.launch { _effect.emit(PoemEffect.ShowError("TTS 服务不可用")) }
            return
        }
        if (!ttsService.isReady()) {
            viewModelScope.launch { _effect.emit(PoemEffect.ShowError("TTS 引擎正在初始化，请稍候...")) }
            return
        }

        _viewState.update { it.copy(isSpeaking = true) }

        val lines = listOf(poem.title, poem.dynasty, poem.author) +
                poem.content.split("\n").filter { it.isNotBlank() }

        println("PoemViewModel split into ${lines.size} lines")
        lastUtteranceId = (lines.size - 1).toString()

        lines.forEachIndexed { index, line ->
            val enqueue = index > 0
            ttsService.speak(line, index.toString(), enqueue)
        }
    }

    private fun updateHighlight(index: Int) {
        _viewState.update { it.copy(highlightIndex = index) }
    }

    private fun stopSpeaking() {
        ttsService?.stop()
        _viewState.update { it.copy(isSpeaking = false, highlightIndex = -1) }
    }

    private fun checkAutoPlayNext(utteranceId: String) {
        if (utteranceId == lastUtteranceId && viewState.value.isAutoPlay) {
            val currentPoem = viewState.value.selectedPoem ?: return
            val poems = viewState.value.filteredPoems
            val currentIndex = poems.indexOfFirst { it.id == currentPoem.id }

            if (currentIndex != -1 && poems.isNotEmpty()) {
                val nextIndex = (currentIndex + 1) % poems.size
                val nextPoem = poems[nextIndex]
                handleIntent(PoemIntent.SelectPoem(nextPoem))
                handleIntent(PoemIntent.Speak(nextPoem))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsService?.dispose()
    }
}
