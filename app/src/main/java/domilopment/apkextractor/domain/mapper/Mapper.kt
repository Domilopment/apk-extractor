package domilopment.apkextractor.domain.mapper

interface Mapper<in F, out T> {
    fun map(from: F): T
}

fun <F, T> Mapper<F, T>.mapAll(list: List<F>): List<T> = list.map(this::map)