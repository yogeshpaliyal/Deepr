package com.yogeshpaliyal.deepr.backup

import androidx.annotation.Keep

/**
 * Represents a link candidate for import with selection state.
 */
@Keep
data class LinkImportCandidate(
    val link: String,
    val isValid: Boolean,
    val isDuplicate: Boolean = false,
    val isSelected: Boolean = true, // By default all are selected
)
