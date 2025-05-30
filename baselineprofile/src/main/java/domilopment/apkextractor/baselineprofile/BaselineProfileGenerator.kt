package domilopment.apkextractor.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.Until
import domilopment.apkextractor.baselineprofile.Constants.PRIVACY_NOTICE

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.NullPointerException

/**
 * This test class generates a basic startup baseline profile for the target package.
 *
 * We recommend you start with this but add important user flows to the profile to improve their performance.
 * Refer to the [baseline profile documentation](https://d.android.com/topic/performance/baselineprofiles)
 * for more information.
 *
 * You can run the generator with the "Generate Baseline Profile" run configuration in Android Studio or
 * the equivalent `generateBaselineProfile` gradle task:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 * The run configuration runs the Gradle task and applies filtering to run only the generators.
 *
 * Check [documentation](https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args)
 * for more information about available instrumentation arguments.
 *
 * After you run the generator, you can verify the improvements running the [StartupBenchmarks] benchmark.
 *
 * When using this class to generate a baseline profile, only API 33+ or rooted API 28+ are supported.
 *
 * The minimum required version of androidx.benchmark to generate a baseline profile is 1.2.0.
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        // This example works only with the variant with application id `domilopment.apkextractor`."
        rule.collect(
            packageName = "domilopment.apkextractor",

            // See: https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations
            includeInStartupProfile = true
        ) {
            // This block defines the app's critical user journey. Here we are interested in
            // optimizing for app startup. But you can also navigate and scroll through your most important UI.

            // Start default activity for your app
            pressHome()
            startActivityAndWait()

            // TODO Write more interactions to optimize advanced journeys of your app.
            // For example:
            // 1. Wait until the content is asynchronously loaded
            // 2. Scroll the feed content
            // 3. Navigate to detail screen

            // Check UiAutomator documentation for more information how to interact with the app.
            // https://d.android.com/training/testing/other-components/ui-automator

            try {
                // Privacy Consent Dialog
                device.wait(Until.hasObject(By.textStartsWith(PRIVACY_NOTICE)), 5_000)
                while (!device.findObject(By.text("Deny All")).parent.isEnabled) {
                    try {
                        device.findObject(By.textStartsWith(PRIVACY_NOTICE))
                            .swipe(Direction.UP, 1f)
                    } catch (_: StaleObjectException) {
                        // Hot Fix
                    }
                }
                device.waitForIdle(1_000)
                device.findObject(By.text("Deny All")).clickAndWait(Until.newWindow(), 1_000)
            } catch (_: NullPointerException) {
                // Onboarding already done.
            }

            try {
                // Save dir Dialog
                device.findObject(By.text("OK")).clickAndWait(Until.newWindow(), 5_000)
                device.findObject(By.text("Documents")).clickAndWait(Until.newWindow(), 1_000)
                device.findObject(By.text("USE THIS FOLDER")).clickAndWait(Until.newWindow(), 1_000)
                device.findObject(By.text("ALLOW")).clickAndWait(Until.newWindow(), 5_000)
            } catch (_: NullPointerException) {
                // Onboarding already done.
            }

            device.findObject(By.text("Settings")).clickAndWait(Until.newWindow(), 1_000)
            device.findObject(By.res("SettingsLazyColumn")).also {
                it.fling(Direction.DOWN)
                it.fling(Direction.UP)
            }
            device.pressBack()
        }
    }
}