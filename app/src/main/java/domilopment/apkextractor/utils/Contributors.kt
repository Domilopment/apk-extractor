package domilopment.apkextractor.utils

sealed class BaseContributor(name: String, url: String) {
    data class Contributor(val name: String, val url: String) : BaseContributor(name, url)
    data class Translator(val name: String, val url: String, val languageTag: String) :
        BaseContributor(name, url)

    data class Documentation(val name: String, val url: String) : BaseContributor(name, url)

    data class Designer(val name: String, val url: String) : BaseContributor(name, url)
}

object Contributors {
    val mikropsoft = BaseContributor.Translator("mikropsoft", "https://github.com/mikropsoft", "tr")

    fun getTranslators(): List<BaseContributor.Translator> = listOf(mikropsoft)
}