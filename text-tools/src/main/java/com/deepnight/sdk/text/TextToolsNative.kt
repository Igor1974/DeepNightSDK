package com.deepnight.sdk.text

/**
 * DEEP NIGHT SDK - Text Tools Native Interface
 * High-performance text processing for Russian language and media titles.
 */
object TextToolsNative {
    init {
        System.loadLibrary("text-tools")
    }

    /**
     * Stems a Russian word (removes endings).
     */
    external fun stemWord(word: String): String

    /**
     * Stems a huge block of text separated by spaces in one call.
     */
    external fun stemHugeBlock(block: String): String

    /**
     * Runs a heavy math benchmark in C++ for performance comparison.
     * @return time in microseconds.
     */
    external fun runHeavyBenchmark(iterations: Int): Long

    /**
     * Converts a string to its phonetic representation for fuzzy matching.
     */
    external fun toPhonetic(str: String): String

    /**
     * Calculates a match score between two titles, considering phonetic similarity and year.
     * @return score (higher is better, typical match > 100)
     */
    external fun calculateMatchScore(
        title1: String, phonetic1: String, year1: Int,
        title2: String, phonetic2: String, year2: Int
    ): Int

    /**
     * Cleans a movie title from brackets and redundant words.
     */
    external fun cleanTitle(title: String): String

    /**
     * Extracts quality info (4K, 1080p, etc.) from a title.
     */
    external fun extractQuality(title: String): String

    /**
     * Extracts release year from a title.
     */
    external fun extractYear(title: String): String
}
