package com.finley.android.qualitytime.ui.poem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    state: PoemState,
    onIntent: (PoemIntent) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onIntent(PoemIntent.SearchPoems(it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("搜索诗名、作者...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onIntent(PoemIntent.SearchPoems("")) }) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "清除搜索",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            )

            BadgedBox(
                badge = {
                    if (state.activeFilterCount > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                            Text(state.activeFilterCount.toString(), fontSize = 9.sp)
                        }
                    }
                }
            ) {
                FilterButton(
                    icon = {
                        Icon(
                            Icons.Filled.Tune,
                            contentDescription = "重置筛选",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = { onIntent(PoemIntent.ResetFilters) },
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val allGrade = state.grades.firstOrNull { it.name == "全部" }
            if (allGrade != null) {
                Spacer(modifier = Modifier.width(16.dp))
                FilterChip(
                    selected = state.selectedGrade == "全部",
                    onClick = { onIntent(PoemIntent.FilterByGrade("全部")) },
                    label = { Text("全部 (${allGrade.count})", fontSize = 11.sp) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            ScrollableTabRow(
                modifier = Modifier.weight(1f),
                selectedTabIndex = (state.grades.drop(1).indexOfFirst { it.name == state.selectedGrade })
                    .coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    val index = state.grades.drop(1).indexOfFirst { it.name == state.selectedGrade }
                    if (index != -1 && index < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                }
            ) {
                state.grades.drop(1).forEach { gradeOption ->
                    Tab(
                        selected = state.selectedGrade == gradeOption.name,
                        onClick = { onIntent(PoemIntent.FilterByGrade(gradeOption.name)) },
                        text = {
                            Text(
                                "${gradeOption.name} (${gradeOption.count})",
                                fontSize = 13.sp,
                                fontWeight = if (state.selectedGrade == gradeOption.name) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            FilterButton(
                icon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "收起筛选" else "展开筛选",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = { expanded = !expanded },
                modifier = Modifier.size(40.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                HorizontalFilterRow("朝代", state.dynasties, state.selectedDynasty) {
                    onIntent(PoemIntent.FilterByDynasty(it))
                }

                HorizontalFilterRow("作者", state.authors, state.selectedAuthor) {
                    onIntent(PoemIntent.FilterByAuthor(it))
                }

                SortSection(state, onIntent)
            }
        }
    }
}

@Composable
private fun FilterButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalFilterRow(
    label: String,
    options: List<CategoryOption>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )
        if (options.isNotEmpty()) {
            val first = options.first()
            FilterChip(
                selected = selectedOption == first.name,
                onClick = { onOptionSelected(first.name) },
                label = { Text("${first.name} (${first.count})", fontSize = 11.sp) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(options.drop(1)) { option ->
                FilterChip(
                    selected = selectedOption == option.name,
                    onClick = { onOptionSelected(option.name) },
                    label = { Text("${option.name} (${option.count})", fontSize = 11.sp) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSection(
    state: PoemState,
    onIntent: (PoemIntent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "排序",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )
        if (sortOptions.isNotEmpty()) {
            val first = sortOptions.first()
            FilterChip(
                selected = state.selectedSort == first.key,
                onClick = { onIntent(PoemIntent.SortPoems(first.key)) },
                label = { Text(first.label, fontSize = 11.sp) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(sortOptions.drop(1)) { option ->
                FilterChip(
                    selected = state.selectedSort == option.key,
                    onClick = { onIntent(PoemIntent.SortPoems(option.key)) },
                    label = { Text(option.label, fontSize = 11.sp) },
                    shape = CircleShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }
        }
        IconButton(
            onClick = { onIntent(PoemIntent.ToggleSortOrder) },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (state.sortAscending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = if (state.sortAscending) "升序" else "降序",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier, onReset: () -> Unit = {}) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(110.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "诗",
                    fontSize = 44.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "未找到相关诗词",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "换个关键词，或者重置筛选试试",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(onClick = onReset) {
            Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("重置筛选")
        }
    }
}
