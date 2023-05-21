package domilopment.apkextractor.data

import domilopment.apkextractor.UpdateTrigger

data class ApkOptionsBottomSheetUIState(
    var selectedApplicationModel: PackageArchiveModel? = null,
    var updateTrigger: UpdateTrigger = UpdateTrigger(false)
)