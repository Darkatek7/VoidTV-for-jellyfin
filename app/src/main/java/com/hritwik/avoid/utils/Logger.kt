package com.hritwik.avoid.utils

import android.util.Log
import com.hritwik.avoid.domain.error.AppError

object Logger {
    private const val TAG = "AvoidApp"

    fun d(tag: String, message: String) = Log.d(TAG, "[$tag] $message")

    fun i(tag: String, message: String) = Log.i(TAG, "[$tag] $message")

    fun w(tag: String, message: String, throwable: Throwable? = null) = Log.w(TAG, "[$tag] $message", throwable)

    fun e(tag: String, message: String, throwable: Throwable? = null) = Log.e(TAG, "[$tag] $message", throwable)

    fun logError(error: AppError, throwable: Throwable? = null) {
        Log.e(TAG, "[${error::class.simpleName}] ${error.message}", throwable)
    }
}
