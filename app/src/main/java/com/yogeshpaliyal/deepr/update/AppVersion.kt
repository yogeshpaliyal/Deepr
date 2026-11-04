package com.yogeshpaliyal.deepr.update

/**
 * Returns true when [candidate] (e.g. a release tag like "v1.0.29") is a newer
 * version than [current] (e.g. BuildConfig.VERSION_NAME like "1.0.28" or "1.0.28-debug").
 * Any suffix after '-' or '+' is ignored, and missing parts count as 0.
 */
fun isNewerVersion(
    candidate: String,
    current: String,
): Boolean {
    val candidateParts = parseVersion(candidate) ?: return false
    val currentParts = parseVersion(current) ?: return true
    val size = maxOf(candidateParts.size, currentParts.size)
    for (i in 0 until size) {
        val a = candidateParts.getOrElse(i) { 0 }
        val b = currentParts.getOrElse(i) { 0 }
        if (a != b) return a > b
    }
    return false
}

private fun parseVersion(version: String): List<Int>? {
    val core =
        version
            .trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore('-')
            .substringBefore('+')
    if (core.isEmpty()) return null
    return core.split('.').map { it.toIntOrNull() ?: return null }
}
