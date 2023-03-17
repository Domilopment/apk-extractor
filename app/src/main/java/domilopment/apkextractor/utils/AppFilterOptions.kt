package domilopment.apkextractor.utils

enum class AppFilterOptions(private val b: Int) {
    FAVORITES(1),
    GOOGLE(2),
    SAMSUNG(4),
    AMAZON(8),
    OTHERS(16);

    fun getByte(): Int {
        return b
    }
}