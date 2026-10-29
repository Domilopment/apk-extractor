package domilopment.apkextractor.ui

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object DeviceTypeUtils {
    enum class DeviceType {
        PHONE, TABLET, NONE
    }

    fun getDeviceBarType(adaptiveInfo: WindowAdaptiveInfo): DeviceType {
        return with(adaptiveInfo) {
            if (windowSizeClass.minWidthDp.dp == 0.dp || windowPosture.isTabletop || windowSizeClass.minHeightDp.dp == 0.dp) {
                DeviceType.PHONE
            } else {
                DeviceType.TABLET
            }
        }
    }

    val isPhoneBars: Boolean
        @Composable get() = getDeviceBarType(currentWindowAdaptiveInfoV2()) == DeviceType.PHONE

    val isTabletBars: Boolean
        @Composable get() = getDeviceBarType(currentWindowAdaptiveInfoV2()) == DeviceType.TABLET
}