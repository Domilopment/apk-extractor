package domilopment.apkextractor.ui

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

object DeviceTypeUtils {
    enum class DeviceType {
        PHONE, TABLET, NONE
    }

    fun getDeviceBarType(adaptiveInfo: WindowAdaptiveInfo): DeviceType {
        return with(adaptiveInfo) {
            if (windowPosture.isTabletop || windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT) {
                DeviceType.PHONE
            } else if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED || windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM) {
                DeviceType.TABLET
            } else {
                DeviceType.PHONE
            }
        }
    }

    val isPhoneBars @Composable get() = getDeviceBarType(currentWindowAdaptiveInfo()) == DeviceType.PHONE

    val isTabletBars @Composable get() = getDeviceBarType(currentWindowAdaptiveInfo()) == DeviceType.TABLET
}