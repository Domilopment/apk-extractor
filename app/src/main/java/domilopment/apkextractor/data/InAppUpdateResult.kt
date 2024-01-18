package domilopment.apkextractor.data

enum class InAppUpdateResultType {
    CANCELED, UPDATE_FAILED
}

data class InAppUpdateResult(val resultType: InAppUpdateResultType)
