package com.finley.android.qualitytime.ui.poem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.flow.collectLatest

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
        state.isShowingSettings -> "全局设置"
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
        topBar = {
            Surface(tonalElevation = 4.dp, shadowElevation = 4.dp) {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                title,
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
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(size.width * 0.2f, size.height * 0.2f),
                            radius = size.minDimension
                        )
                    )
                }
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val listState = rememberLazyListState()
                var targetLetter by remember { mutableStateOf("") }
                var selectedLetter by remember { mutableStateOf("") }
                val activeLetter by remember {
                    derivedStateOf {
                        val index = listState.firstVisibleItemIndex
                        if (index < state.filteredPoems.size) {
                            val poem = state.filteredPoems.getOrNull(index)
                            if (poem != null) {
                                val first = PoemFilterEngine.stripToneMarks(poem.titlePinyin)
                                    .firstOrNull()
                                    ?.let { if (it in 'a'..'z') it.uppercaseChar().toString() else null }
                                first ?: "#"
                            } else ""
                        } else ""
                    }
                }
                LaunchedEffect(state.selectedSort, state.sortAscending) {
                    listState.animateScrollToItem(0)
                    targetLetter = ""
                    selectedLetter = ""
                }
                LaunchedEffect(targetLetter) {
                    if (targetLetter.isNotEmpty()) {
                        val index = state.letterIndexMap[targetLetter] ?: -1
                        if (index >= 0) {
                            listState.animateScrollToItem(index)
                            targetLetter = ""
                        }
                    }
                }
                AnimatedContent(
                    targetState = state.isShowingSettings to (state.selectedPoem != null),
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                    }
                ) { (isSettings, isDetail) ->
                    when {
                        isSettings -> {
                            SettingsScreen(
                                state = state,
                                onRateChange = { viewModel.handleIntent(PoemIntent.ChangeSpeechRate(it)) },
                                onAutoPlayToggle = { viewModel.handleIntent(PoemIntent.ToggleAutoPlay(it)) },
                                onTogglePinyin = { viewModel.handleIntent(PoemIntent.TogglePinyin) },
                                onVoiceChange = { viewModel.handleIntent(PoemIntent.ChangeVoice(it)) },
                                onRefreshVoices = { viewModel.handleIntent(PoemIntent.RefreshVoices) }
                            )
                        }
                        !isDetail -> {
                            if (state.filteredPoems.isEmpty() && state.searchQuery.isNotEmpty()) {
                                EmptyState(modifier = Modifier.align(Alignment.Center))
                            } else {
                                Box {
                                    PoemList(
                                        poems = state.filteredPoems,
                                        onPoemClick = { viewModel.handleIntent(PoemIntent.SelectPoem(it)) },
                                        searchQuery = state.searchQuery,
                                        selectedLetter = selectedLetter,
                                        sortKey = state.selectedSort,
                                        listState = listState
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
                        else -> {
                            state.selectedPoem?.let {
                                PoemDetail(
                                    poem = it,
                                    isSpeaking = state.isSpeaking,
                                    highlightIndex = state.highlightIndex,
                                    showPinyin = state.showPinyin,
                                    onSpeak = { viewModel.handleIntent(PoemIntent.Speak(it)) },
                                    onStop = { viewModel.handleIntent(PoemIntent.StopSpeaking) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
