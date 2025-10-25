package com.yogeshpaliyal.deepr.review

object ReviewManagerFactory {
    fun create(): ReviewManager {
        return PlayStoreReviewManager()
    }
}
