package devnibbles.android.facialrecognition.classify.common

sealed class Resource<T, E>(open val data: T?)

data class LoadingResource<T, E> constructor(override val data: T? = null) : Resource<T, E>(data)
data class ErrorResource<T, E> constructor(val errorData: E?, override val data: T? = null) : Resource<T, E>(data)
data class SuccessResource<T, E> constructor(override val data: T) : Resource<T, E>(data)