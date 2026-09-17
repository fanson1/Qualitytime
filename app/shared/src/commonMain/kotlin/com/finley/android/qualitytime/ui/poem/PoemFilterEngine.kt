package com.finley.android.qualitytime.ui.poem

import com.finley.android.qualitytime.model.Poem

/**
 * Pure filter and sort logic for poems.
 * Extracted from ViewModel for testability and single responsibility.
 */
object PoemFilterEngine {

    fun applyFilters(
        poems: List<Poem>,
        selectedGrade: String,
        selectedDynasty: String,
        selectedAuthor: String,
        searchQuery: String
    ): List<Poem> {
        val strippedQuery = if (searchQuery.isNotBlank()) stripToneMarks(searchQuery) else ""
        return poems.filter { poem ->
            val matchesGrade = selectedGrade == FILTER_ALL || poem.grade == selectedGrade
            val matchesDynasty = selectedDynasty == FILTER_ALL || poem.dynasty == selectedDynasty
            val matchesAuthor = selectedAuthor == FILTER_ALL || poem.author == selectedAuthor
            val matchesQuery = searchQuery.isBlank() ||
                    poem.title.contains(searchQuery, ignoreCase = true) ||
                    poem.author.contains(searchQuery, ignoreCase = true) ||
                    strippedQuery.isNotEmpty() && (
                            stripToneMarks(poem.titlePinyin).contains(strippedQuery) ||
                            stripToneMarks(poem.authorPinyin).contains(strippedQuery) ||
                            stripToneMarks(poem.dynastyPinyin).contains(strippedQuery)
                            )

            matchesGrade && matchesDynasty && matchesAuthor && matchesQuery
        }
    }

    /**
     * Strip tone marks from pinyin for proper alphabetical sorting.
     * Tone-marked vowels (āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ) have higher Unicode
     * code points than plain ASCII letters, which breaks string sorting.
     */
    internal fun stripToneMarks(s: String): String {
        return s.lowercase()
            .replace("[āáǎà]".toRegex(), "a")
            .replace("[ēéěè]".toRegex(), "e")
            .replace("[īíǐì]".toRegex(), "i")
            .replace("[ōóǒò]".toRegex(), "o")
            .replace("[ūúǔù]".toRegex(), "u")
            .replace("[ǖǘǚǜ]".toRegex(), "ü")
    }

    fun sortPoems(
        poems: List<Poem>,
        sortKey: String,
        ascending: Boolean
    ): List<Poem> {
        val sorted = when (sortKey) {
            "title" -> poems.sortedBy { stripToneMarks(it.titlePinyin) }
            "grade" -> {
                val gradeOrder = GRADE_ORDER
                poems.sortedWith(
                    compareBy<Poem> { poem ->
                        val idx = gradeOrder.indexOf(poem.grade)
                        if (idx == -1) Int.MAX_VALUE else idx
                    }.thenBy { it.title }
                )
            }
            "dynasty" -> {
                val dynastyOrder = DYNASTY_ORDER
                poems.sortedWith(
                    compareBy<Poem> { poem ->
                        val idx = dynastyOrder.indexOf(poem.dynasty)
                        if (idx == -1) Int.MAX_VALUE else idx
                    }.thenBy { it.title }
                )
            }
            "author" -> poems.sortedWith(compareBy<Poem> { stripToneMarks(it.authorPinyin) }.thenBy { it.title })
            else -> poems
        }
        return if (ascending) sorted else sorted.reversed()
    }

    fun countActiveFilters(
        selectedGrade: String,
        selectedDynasty: String,
        selectedAuthor: String,
        searchQuery: String
    ): Int {
        var count = 0
        if (selectedGrade != FILTER_ALL) count++
        if (selectedDynasty != FILTER_ALL) count++
        if (selectedAuthor != FILTER_ALL) count++
        if (searchQuery.isNotBlank()) count++
        return count
    }

    fun extractCategories(poems: List<Poem>): CategoryResult {
        val allGrades = poems.map { it.grade }.distinct().filter { it.isNotEmpty() }
        val sortedGrades = listOf(FILTER_ALL) +
                GRADE_ORDER.filter { it in allGrades } +
                allGrades.filter { it !in GRADE_ORDER }

        val grades = sortedGrades.map { grade ->
            CategoryOption(grade, if (grade == FILTER_ALL) poems.size else poems.count { it.grade == grade })
        }

        val allDynasties = poems.map { it.dynasty }.distinct()
        val sortedDynasties = DYNASTY_ORDER.filter { it in allDynasties } +
                allDynasties.filter { it !in DYNASTY_ORDER }
        val dynasties = (listOf(FILTER_ALL) + sortedDynasties).map { dynasty ->
            CategoryOption(
                dynasty,
                if (dynasty == FILTER_ALL) poems.size else poems.count { it.dynasty == dynasty }
            )
        }

        val authors = (listOf(FILTER_ALL) + poems.map { it.author }.distinct().sorted()).map { author ->
            CategoryOption(
                author,
                if (author == FILTER_ALL) poems.size else poems.count { it.author == author }
            )
        }

        return CategoryResult(grades, dynasties, authors)
    }

    /**
     * Build a map from initial letter (A-Z) to the index of the first poem
     * in the sorted list whose pinyin starts with that letter.
     * Non-alphabetic entries are grouped under "#".
     */
    fun computeLetterIndexMap(poems: List<Poem>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        poems.forEachIndexed { index, poem ->
            val first = stripToneMarks(poem.titlePinyin)
                .firstOrNull()
                ?.let { if (it in 'a'..'z') it.uppercase() else null }
            val letter = first ?: "#"
            if (letter !in map) {
                map[letter] = index
            }
        }
        return map
    }

    /**
     * Canonical order of grades used for filtering chips and grade sorting.
     * Must match the labels used in `poems.json` ("幼儿园", "一年级" ... "十三年级")
     * with "课外" (outside-curriculum) always last.
     */
    private val GRADE_ORDER = listOf(
        "幼儿园", "一年级", "二年级", "三年级", "四年级",
        "五年级", "六年级", "七年级", "八年级", "九年级",
        "十年级", "十一年级", "十二年级", "十三年级", "课外"
    )

    private val DYNASTY_ORDER = listOf(
        "先秦", "秦", "汉", "东汉", "三国", "晋", "南北朝", "梁",
        "隋", "唐", "五代", "宋", "元", "明", "清", "近现代", "现代"
    )

    data class CategoryResult(
        val grades: List<CategoryOption>,
        val dynasties: List<CategoryOption>,
        val authors: List<CategoryOption>
    )
}
