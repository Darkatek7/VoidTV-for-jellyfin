package com.hritwik.avoid.utils.helpers

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.coroutines.delay
import kotlin.math.abs
import android.util.Log
import java.util.Locale
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import kotlin.math.roundToInt
import kotlin.coroutines.resume

object FrameRateHelper {
    private const val TAG = "FrameRateHelper"

    data class DisplaySwitchKey(
        val rate: Float,
        val width: Int?,
        val height: Int?
    )

    fun findSurfaceView(root: View?): SurfaceView? {
        if (root == null) return null
        if (root is SurfaceView) return root
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                val child = root.getChildAt(i)
                val surface = findSurfaceView(child)
                if (surface != null) return surface
            }
        }
        return null
    }

    fun applyFrameRate(surfaceView: SurfaceView?, frameRate: Float): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        if (!frameRate.isFinite() || frameRate <= 0f) return false
        val surface = surfaceView?.holder?.surface ?: return false
        if (!surface.isValid) return false
        applyFrameRate(surface, frameRate)
        return true
    }

    suspend fun requestFrameRate(
        surfaceProvider: () -> SurfaceView?,
        frameRate: Float,
        attempts: Int = 3,
        delayMs: Long = 200
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        if (!frameRate.isFinite() || frameRate <= 0f) return false
        repeat(attempts.coerceAtLeast(1)) {
            if (applyFrameRate(surfaceProvider(), frameRate)) {
                return true
            }
            delay(delayMs)
        }
        return false
    }

    data class FrameRateVerification(
        val matched: Boolean,
        val currentRate: Float?
    )

    data class DisplayModeInfo(
        val modeId: Int,
        val width: Int,
        val height: Int,
        val refreshRate: Float
    )

    fun getDisplayRefreshRate(view: View?): Float? {
        val display = view?.display ?: return null
        return display.mode?.refreshRate
    }

    fun getDisplayModes(view: View?): List<DisplayModeInfo> {
        val display = view?.display ?: return emptyList()
        return display.supportedModes.map { mode ->
            DisplayModeInfo(
                modeId = mode.modeId,
                width = mode.physicalWidth,
                height = mode.physicalHeight,
                refreshRate = mode.refreshRate
            )
        }
    }

    fun selectBestDisplayMode(
        display: android.view.Display,
        streamWidth: Int?,
        streamHeight: Int?,
        targetRate: Float,
        preferExactResolution: Boolean
    ): android.view.Display.Mode? {
        if (targetRate <= 0f) return null
        val modes = display.supportedModes.toList()
        if (modes.isEmpty()) return null
        val targetRateMs = rateToMillis(targetRate)
        if (streamWidth == null || streamHeight == null) {
            return selectClosestUpperRate(modes, targetRateMs, preferHigherResolution = true)
        }
        val compatibleModes = modes.filter { mode ->
            mode.physicalWidth >= streamWidth &&
                mode.physicalHeight >= streamHeight &&
                isRateCompatible(rateToMillis(mode.refreshRate), targetRateMs)
        }
        if (compatibleModes.isEmpty()) {
            return selectClosestUpperRate(
                modes = modes.filter { it.physicalWidth >= streamWidth && it.physicalHeight >= streamHeight },
                targetRateMs = targetRateMs,
                preferHigherResolution = false
            ) ?: selectClosestUpperRate(modes, targetRateMs, preferHigherResolution = true)
        }
        if (!preferExactResolution) {
            return compatibleModes.maxByOrNull { it.physicalWidth * it.physicalHeight }
        }
        val exactResolution = compatibleModes.filter {
            it.physicalWidth == streamWidth && it.physicalHeight == streamHeight
        }
        if (exactResolution.isNotEmpty()) {
            return selectClosestUpperRate(exactResolution, targetRateMs, preferHigherResolution = false)
        }
        val matchingWidth = compatibleModes.filter {
            it.physicalWidth == streamWidth && it.physicalHeight >= streamHeight
        }
        if (matchingWidth.isNotEmpty()) {
            return selectClosestUpperRate(matchingWidth, targetRateMs, preferHigherResolution = false)
        }
        return selectClosestUpperRate(compatibleModes, targetRateMs, preferHigherResolution = false)
    }

    suspend fun requestPreferredDisplayMode(
        window: Window,
        displayManager: DisplayManager,
        displayId: Int,
        modeId: Int,
        timeoutMs: Long = 5000L
    ): Boolean {
        val attributes = window.attributes
        if (attributes.preferredDisplayModeId == modeId) return false
        return awaitDisplayChange(displayManager, displayId, timeoutMs) {
            attributes.preferredDisplayModeId = modeId
            window.attributes = attributes
        }
    }

    fun setPreferredDisplayMode(window: Window, modeId: Int): Boolean {
        val attributes = window.attributes
        if (attributes.preferredDisplayModeId == modeId) return false
        attributes.preferredDisplayModeId = modeId
        window.attributes = attributes
        return true
    }

    fun logDisplayModes(view: View?) {
        val display = view?.display
        if (display == null) {
            Log.d(TAG, "Display not available for refresh rate logging.")
            return
        }
        val current = display.mode
        Log.d(
            TAG,
            "Current display mode: id=${current.modeId} ${current.physicalWidth}x${current.physicalHeight} @ ${current.refreshRate}hz"
        )
        display.supportedModes.forEach { mode ->
            Log.d(
                TAG,
                "Supported mode: id=${mode.modeId} ${mode.physicalWidth}x${mode.physicalHeight} @ ${mode.refreshRate}hz"
            )
        }
    }

    suspend fun verifyFrameRate(
        viewProvider: () -> View?,
        targetRate: Float,
        attempts: Int = 5,
        delayMs: Long = 200,
        tolerance: Float = 0.2f
    ): FrameRateVerification {
        var lastObserved: Float? = null
        repeat(attempts.coerceAtLeast(1)) {
            val current = getDisplayRefreshRate(viewProvider())
            if (current != null) {
                lastObserved = current
                if (abs(current - targetRate) <= tolerance) {
                    return FrameRateVerification(true, current)
                }
            }
            delay(delayMs)
        }
        return FrameRateVerification(false, lastObserved)
    }

    private fun rateToMillis(rate: Float): Int = (rate * 1000).roundToInt()

    private fun isRateCompatible(modeRateMs: Int, targetRateMs: Int): Boolean {
        val tolerance = 500
        if (abs(modeRateMs - targetRateMs) <= tolerance) return true
        if (modeRateMs > 0 && targetRateMs > 0) {
            if (modeRateMs % targetRateMs == 0 || targetRateMs % modeRateMs == 0) return true
            val diff = abs(modeRateMs * 2 - targetRateMs * 5)
            if (diff <= tolerance * 5) return true
        }
        return false
    }

    private fun selectClosestUpperRate(
        modes: List<android.view.Display.Mode>,
        targetRateMs: Int,
        preferHigherResolution: Boolean
    ): android.view.Display.Mode? {
        if (modes.isEmpty()) return null
        val above = modes.filter { rateToMillis(it.refreshRate) >= targetRateMs }
        val candidates = if (above.isNotEmpty()) above else modes
        return candidates.minWithOrNull { a, b ->
            val rateA = rateToMillis(a.refreshRate)
            val rateB = rateToMillis(b.refreshRate)
            val diffA = if (rateA >= targetRateMs) rateA - targetRateMs else targetRateMs - rateA
            val diffB = if (rateB >= targetRateMs) rateB - targetRateMs else targetRateMs - rateB
            if (diffA != diffB) {
                diffA.compareTo(diffB)
            } else {
                val areaA = a.physicalWidth * a.physicalHeight
                val areaB = b.physicalWidth * b.physicalHeight
                if (preferHigherResolution) areaB.compareTo(areaA) else areaA.compareTo(areaB)
            }
        }
    }

    private suspend fun awaitDisplayChange(
        displayManager: DisplayManager,
        displayId: Int,
        timeoutMs: Long,
        onStart: () -> Unit
    ): Boolean {
        val handler = Handler(Looper.getMainLooper())
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            val listener = object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(displayId: Int) = Unit

                override fun onDisplayChanged(changedId: Int) {
                    if (changedId == displayId && continuation.isActive) {
                        displayManager.unregisterDisplayListener(this)
                        handler.removeCallbacksAndMessages(null)
                        continuation.resume(true)
                    }
                }

                override fun onDisplayRemoved(displayId: Int) = Unit
            }
            displayManager.registerDisplayListener(listener, handler)
            onStart()
            handler.postDelayed({
                if (continuation.isActive) {
                    displayManager.unregisterDisplayListener(listener)
                    continuation.resume(false)
                }
            }, timeoutMs)
            continuation.invokeOnCancellation {
                displayManager.unregisterDisplayListener(listener)
                handler.removeCallbacksAndMessages(null)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun applyFrameRate(surface: Surface, frameRate: Float) {
        if (!frameRate.isFinite() || frameRate <= 0f) return
        surface.setFrameRate(frameRate, Surface.FRAME_RATE_COMPATIBILITY_DEFAULT)
    }

    fun showFrameRateToast(
        context: Context,
        currentRate: Float,
        requestedRate: Float? = null,
        verified: Boolean = true,
        modeMatched: Boolean = false,
        resolutionLabel: String? = null
    ) {
        val currentLabel = String.format(Locale.US, "%s hz", currentRate.toString())
        val resolutionSuffix = resolutionLabel?.let { " $it" } ?: ""
        val message = when {
            verified || requestedRate == null -> {
                "Screen refresh rate changed to $currentLabel$resolutionSuffix"
            }
            modeMatched -> {
                val requestedLabel = String.format(Locale.US, "%s hz", requestedRate.toString())
                "Requested $requestedLabel, display mode set (current $currentLabel$resolutionSuffix)"
            }
            requestedRate != null -> {
                val requestedLabel = String.format(Locale.US, "%s hz", requestedRate.toString())
                "Requested $requestedLabel, using $currentLabel$resolutionSuffix"
            }
            else -> "Screen refresh rate changed to $currentLabel"
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
