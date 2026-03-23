package com.hritwik.avoid.domain.usecase.common

import com.hritwik.avoid.data.common.NetworkResult
import com.hritwik.avoid.domain.error.AppError
import com.hritwik.avoid.utils.Logger
import com.hritwik.avoid.utils.CrashReporter
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseUseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    suspend operator fun invoke(parameters: P): NetworkResult<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute(parameters)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            val error = AppError.Network(e.message ?: "Network error")
            Logger.logError(error, e)
            NetworkResult.Error<R>(error, e)
        } catch (e: SecurityException) {
            val error = AppError.Auth(e.message ?: "Authentication error")
            Logger.logError(error, e)
            NetworkResult.Error<R>(error, e)
        } catch (e: IllegalArgumentException) {
            val error = AppError.Validation(e.message ?: "Validation error")
            Logger.logError(error, e)
            NetworkResult.Error<R>(error, e)
        } catch (e: Exception) {
            Logger.e("BaseUseCase", "Unexpected error: ${e.message}", e)
            CrashReporter.report(e)
            val error = AppError.Unknown(e.message ?: "Unknown error occurred")
            NetworkResult.Error<R>(error, e)
        }
    }

    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P): NetworkResult<R>
}
