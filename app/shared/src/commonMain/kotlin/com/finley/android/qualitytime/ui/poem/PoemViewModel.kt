package com.finley.android.qualitytime.ui.poem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finley.android.qualitytime.model.Poem
import com.finley.android.qualitytime.service.SettingsService
import com.finley.android.qualitytime.service.TextToSpeechService
import com.finley.android.qualitytime.util.AppLog
import kotlinx.coroutines.Job
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

    companion object {
        private const val TAG = "PoemViewModel"
        private const val SPLASH_DURATION_MS = 2000L
        private const val SEARCH_DEBOUNCE_MS = 250L
        private const val VOICE_REFRESH_RETRIES = 3
        private const val VOICE_REFRESH_DELAY_MS = 800L
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val _viewState = MutableStateFlow(PoemState())
    val viewState: StateFlow<PoemState> = _viewState.asStateFlow()

    private val _effect = MutableSharedFlow<PoemEffect>()
    val effect: SharedFlow<PoemEffect> = _effect.asSharedFlow()

    /** id of the last utterance of the currently-read poem; used to auto-advance. */
    private var lastUtteranceId: String = ""

    /** Cancel and replace on each keystroke to debounce filtering. */
    private var searchJob: Job? = null

    init {
        ttsService?.setProgressListener(
            onStart = { utteranceId ->
                utteranceId.toIntOrNull()?.let { handleIntent(PoemIntent.UpdateHighlight(it)) }
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
            delay(SPLASH_DURATION_MS)
            handleIntent(PoemIntent.SplashComplete)
        }
    }

    fun handleIntent(intent: PoemIntent) {
        when (intent) {
            is PoemIntent.LoadPoems -> loadPoems()
            is PoemIntent.RetryLoad -> loadPoems()
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
            is PoemIntent.SplashComplete -> completeSplash()
            is PoemIntent.RefreshVoices -> refreshVoices()
            is PoemIntent.SortPoems -> sortPoemsBy(intent.sortKey)
            is PoemIntent.ToggleSortOrder -> toggleSortOrder()
        }
    }

    // ── TTS ──────────────────────────────────────────────────────────────

    private fun refreshVoices() {
        viewModelScope.launch {
            var retries = VOICE_REFRESH_RETRIES
            while (retries > 0) {
                val voices = ttsService?.getVoices().orEmpty()
                if (voices.isNotEmpty()) {
                    _viewState.update { it.copy(availableVoices = voices) }
                    return@launch
                }
                AppLog.d(TAG) { "No voices available, retrying ($retries left)" }
                delay(VOICE_REFRESH_DELAY_MS)
                retries--
            }
        }
    }

    private fun completeSplash() {
        refreshVoices()
        _viewState.update { it.copy(isSplashComplete = true) }
    }

    private fun navigateToSettings(show: Boolean) {
        if (show) refreshVoices()
        _viewState.update { it.copy(isShowingSettings = show) }
    }

    private fun changeVoice(id: String) {
        _viewState.update { it.copy(selectedVoiceId = id) }
        ttsService?.setVoice(id)
        viewModelScope.launch { settingsService?.setSelectedVoiceId(id) }
        // Short preview so the user hears the newly selected voice.
        ttsService?.speak("你好，这是新的朗读声音", "voice_preview", false)
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
        // Update the field immediately so typing stays responsive, then filter
        // after a short debounce so rapid keystrokes don't recompute the list.
        _viewState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            reapplyFilters()
        }
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
        searchJob?.cancel()
        _viewState.update {
            it.copy(
                selectedGrade = FILTER_ALL,
                selectedAuthor = FILTER_ALL,
                selectedDynasty = FILTER_ALL,
                searchQuery = ""
            )
        }
        reapplyFilters()
    }

    private fun reapplyFilters() {
        val state = _viewState.value
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
        _viewState.update {
            it.copy(
                filteredPoems = sorted,
                activeFilterCount = filterCount,
                letterIndexMap = PoemFilterEngine.computeLetterIndexMap(sorted)
            )
        }
    }

    // ── Data Loading ─────────────────────────────────────────────────────

    @OptIn(ExperimentalResourceApi::class)
    private fun loadPoems() {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, loadError = null) }
            try {
                val bytes = Res.readBytes("files/poems.json")
                val poems = json.decodeFromString<List<Poem>>(bytes.decodeToString())

                val categories = PoemFilterEngine.extractCategories(poems)
                val voices = ttsService?.getVoices().orEmpty()

                _viewState.update {
                    it.copy(
                        poems = poems,
                        filteredPoems = poems,
                        isLoading = false,
                        loadError = null,
                        selectedGrade = FILTER_ALL,
                        grades = categories.grades,
                        dynasties = categories.dynasties,
                        authors = categories.authors,
                        availableVoices = voices,
                        letterIndexMap = PoemFilterEngine.computeLetterIndexMap(poems)
                    )
                }
                AppLog.i(TAG) { "Loaded ${poems.size} poems" }
            } catch (e: Exception) {
                AppLog.e(TAG, "Failed to load poems", e)
                val message = "古诗库加载失败，请检查应用数据后重试"
                _viewState.update { it.copy(isLoading = false, loadError = message) }
                _effect.emit(PoemEffect.ShowError("$message: ${e.message}"))
            }
        }
    }

    private fun loadSettings() {
        settingsService ?: return
        viewModelScope.launch {
            settingsService.getSpeechRate().collect { rate ->
                _viewState.update { it.copy(speechRate = rate) }
                ttsService?.setSpeechRate(rate)
            }
        }
        viewModelScope.launch {
            settingsService.isAutoPlayEnabled().collect { enabled ->
                _viewState.update { it.copy(isAutoPlay = enabled) }
            }
        }
        viewModelScope.launch {
            settingsService.getSelectedVoiceId().collect { id ->
                _viewState.update { it.copy(selectedVoiceId = id) }
                id?.let { ttsService?.setVoice(it) }
            }
        }
        viewModelScope.launch {
            settingsService.isShowPinyinEnabled().collect { enabled ->
                _viewState.update { it.copy(showPinyin = enabled) }
            }
        }
    }

    // ── Poem Selection & TTS ─────────────────────────────────────────────

    private fun selectPoem(poem: Poem?) {
        AppLog.d(TAG) { "selectPoem: ${poem?.title}" }
        stopSpeaking()
        _viewState.update { it.copy(selectedPoem = poem, highlightIndex = -1) }
    }

    private fun speak(poem: Poem) {
        val service = ttsService
        if (service == null) {
            emitError("语音服务不可用")
            return
        }
        if (!service.isReady()) {
            emitError("语音引擎正在初始化，请稍后重试")
            return
        }

        _viewState.update { it.copy(isSpeaking = true) }

        val lines = listOf(poem.title, poem.dynasty, poem.author) +
                poem.content.split("\n").filter { it.isNotBlank() }
        lastUtteranceId = (lines.size - 1).toString()

        lines.forEachIndexed { index, line ->
            service.speak(line, index.toString(), enqueue = index > 0)
        }
    }

    private fun emitError(message: String) {
        viewModelScope.launch { _effect.emit(PoemEffect.ShowError(message)) }
    }

    private fun updateHighlight(index: Int) {
        _viewState.update { it.copy(highlightIndex = index) }
    }

    private fun stopSpeaking() {
        ttsService?.stop()
        _viewState.update { it.copy(isSpeaking = false, highlightIndex = -1) }
    }

    private fun checkAutoPlayNext(utteranceId: String) {
        if (utteranceId != lastUtteranceId || !viewState.value.isAutoPlay) return

        val currentPoem = viewState.value.selectedPoem ?: return
        val poems = viewState.value.filteredPoems
        if (poems.isEmpty()) return

        val currentIndex = poems.indexOfFirst { it.id == currentPoem.id }
        val nextIndex = (currentIndex + 1).mod(poems.size)
        val nextPoem = poems[nextIndex]
        handleIntent(PoemIntent.SelectPoem(nextPoem))
        handleIntent(PoemIntent.Speak(nextPoem))
    }
}