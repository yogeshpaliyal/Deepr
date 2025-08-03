package com.yogeshpaliyal.deepr.util

import androidx.annotation.Keep

sealed class RequestResult<out R> {
    @Keep
    data class Success<out T>(val data: T) : RequestResult<T>()

    @Keep
    data class Error(val message: String) : RequestResult<Nothing>()
}