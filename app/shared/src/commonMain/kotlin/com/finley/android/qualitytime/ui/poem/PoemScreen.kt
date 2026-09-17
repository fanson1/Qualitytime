package com.finley.android.qualitytime.ui.poem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finley.android.qualitytime.ui.theme.PoemFont
import kotlinx.coroutines.flow.collectLatest

private enum class ScreenKind { HOME, SETTINGS, DETAIL }

private fun screenOrder(kind: ScreenKind): Int = when (kind) {
    ScreenKind.HOME -> 0
    ScreenKind.SETTINGS -> 1
    ScreenKind.DETAIL -> 2
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoemScreen(viewModel: PoemViewModel) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PoemEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (!state.isSplashComplete) {
        SplashScreen()
        return
    }

    val isHome = state.selectedPoem == null && !state.isShowingSettings
    val title = when {
        state.isShowingSettings -> "设置"
        state.selectedPoem != null -> state.selectedPoem?.title ?: "古诗详情"
        else -> "AI 宝宝"
    }

    BackHandler(enabled = !isHome) {
        if (state.isShowingSettings) {
            viewModel.handleIntent(PoemIntent.NavigateToSettings(false))
        } else if (state.selectedPoem != null) {
            viewModel.handleIntent(PoemIntent.SelectPoem(null))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            val primaryColor = MaterialTheme.colorScheme.primary
            Surface(tonalElevation = 0.dp, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)) {
                Column(modifier = Modifier.drawBehind {
                    drawRect(
                        Brush.verticalGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    )
                }) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = title,
                                fontFamily = PoemFont,
                                fontWeight = FontWeight.Black,
                                letterSpacing = if (isHome) 2.sp else 1.sp,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        navigationIcon = {
                            if (!isHome) {
                                IconButton(onClick = {
                                    if (state.isShowingSettings) {
                                        viewModel.handleIntent(PoemIntent.NavigateToSettings(false))
                                    } else {
                                        viewModel.handleIntent(PoemIntent.SelectPoem(null))
                                    }
                                }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "返回",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        actions = {
                            if (isHome) {
                                IconButton(onClick = {
                                    viewModel.handleIntent(PoemIntent.NavigateToSettings(true))
                                }) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "设置",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    )
                    if (isHome && !state.isLoading) {
                        FilterSection(state, onIntent = { viewModel.handleIntent(it) })
                    }
                }
            }
        }
    ) { paddingValues ->
        val primaryColor = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.2f, size.height * 0.1f),
                            radius = size.minDimension * 0.8f
                        )
                    )
                }
        ) {
            val kind = when {
                state.isShowingSettings -> ScreenKind.SETTINGS
                state.selectedPoem != null -> ScreenKind.DETAIL
                else -> ScreenKind.HOME
            }
            val targetState = kind to state.selectedPoem?.id

            AnimatedContent(
                targetState = targetState,
                transitionSpec = {
                    val direction = screenOrder(targetState.first) - screenOrder(initialState.first)
                    if (direction >= 0) {
                        (slideInHorizontally(tween(320)) { it } + fadeIn(tween(320)))
                            .togetherWith(slideOutHorizontally(tween(320)) { -it } + fadeOut(tween(320)))
                    } else {
                        (slideInHorizontally(tween(320)) { -it } + fadeIn(tween(320)))
                            .togetherWith(slideOutHorizontally(tween(320)) { it } + fadeOut(tween(320)))
                    }
                },
                label = "screen"
            ) { (screenKind, poemId) ->
                when (screenKind) {
                    ScreenKind.SETTINGS -> {
                        SettingsScreen(
                            state = state,
                            onRateChange = { viewModel.handleIntent(PoemIntent.ChangeSpeechRate(it)) },
                            onAutoPlayToggle = { viewModel.handleIntent(PoemIntent.ToggleAutoPlay(it)) },
                            onTogglePinyin = { viewModel.handleIntent(PoemIntent.TogglePinyin) },
                            onVoiceChange = { viewModel.handleIntent(PoemIntent.ChangeVoice(it)) },
                            onRefreshVoices = { viewModel.handleIntent(PoemIntent.RefreshVoices) }
                        )
                    }

                    ScreenKind.DETAIL -> {
                        val poem = state.poems.firstOrNull { it.id == poemId } ?: state.selectedPoem
                        if (poem != null) {
                            PoemDetail(
                                poem = poem,
                                isSpeaking = state.isSpeaking,
                                highlightIndex = state.highlightIndex,
                                showPinyin = state.showPinyin,
                                onSpeak = { viewModel.handleIntent(PoemIntent.Speak(it)) },
                                onStop = { viewModel.handleIntent(PoemIntent.StopSpeaking) }
                            )
                        }
                    }

                    ScreenKind.HOME -> {
                        when {
                            state.isLoading -> PoemListSkeleton()
                            state.loadError != null -> LoadErrorState(
                                message = state.loadError,
                                onRetry = { viewModel.handleIntent(PoemIntent.RetryLoad) }
                            )
                            else -> HomeContent(
                                state = state,
                                onIntent = { viewModel.handleIntent(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: PoemState,
    onIntent: (PoemIntent) -> Unit
) {
    val listState = rememberLazyListState()
    var targetLetter by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf("") }

    val showHero = state.searchQuery.isBlank() &&
            state.activeFilterCount == 0 &&
            state.filteredPoems.isNotEmpty()
    val headerOffset = if (showHero) 1 else 0

    val featuredPoem = remember(state.poems) {
        state.poems.firstOrNull { it.id == "k-1" } ?: state.poems.firstOrNull()
    }

    val currentHeaderOffset by rememberUpdatedState(headerOffset)
    val currentFilteredPoems by rememberUpdatedState(state.filteredPoems)

    val activeLetter by remember {
        derivedStateOf {
            val listIndex = listState.firstVisibleItemIndex
            val index = listIndex - currentHeaderOffset
            if (index in 0 until currentFilteredPoems.size) {
                val poem = currentFilteredPoems.getOrNull(index)
                if (poem != null) {
                    val first = PoemFilterEngine.stripToneMarks(poem.titlePinyin)
                        .firstOrNull()
                        ?.let { if (it in 'a'..'z') it.uppercaseChar().toString() else null }
                    first ?: "#"
                } else ""
            } else ""
        }
    }

    val filterKey = "${state.selectedGrade}|${state.selectedAuthor}|${state.selectedDynasty}|${state.selectedSort}|${state.sortAscending}"
    LaunchedEffect(filterKey) {
        listState.animateScrollToItem(0)
        targetLetter = ""
        selectedLetter = ""
    }

    LaunchedEffect(targetLetter) {
        if (targetLetter.isNotEmpty()) {
            val index = state.letterIndexMap[targetLetter] ?: -1
            if (index >= 0) {
                listState.animateScrollToItem(index + headerOffset)
                targetLetter = ""
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        if (state.filteredPoems.isEmpty() && state.searchQuery.isNotEmpty()) {
            EmptyState(
                modifier = Modifier.align(Alignment.Center),
                onReset = { onIntent(PoemIntent.ResetFilters) }
            )
        } else {
            Box {
                PoemList(
                    poems = state.filteredPoems,
                    onPoemClick = { onIntent(PoemIntent.SelectPoem(it)) },
                    searchQuery = state.searchQuery,
                    selectedLetter = selectedLetter,
                    sortKey = state.selectedSort,
                    listState = listState,
                    header = if (showHero && featuredPoem != null) {
                        {
                            HomeHero(
                                poem = featuredPoem,
                                poemCount = state.poems.size,
                                authorCount = state.poems.map { it.author }.distinct().size,
                                dynastyCount = state.poems.map { it.dynasty }.distinct().size,
                                onPoemClick = { onIntent(PoemIntent.SelectPoem(it)) }
                            )
                        }
                    } else null
                )
                if (state.filteredPoems.isNotEmpty()) {
                    AlphabetSidebar(
                        letterIndexMap = state.letterIndexMap,
                        activeLetter = activeLetter,
                        onLetterClick = {
                            targetLetter = it
                            selectedLetter = it
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}
