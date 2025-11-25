package com.example.kmaerm.core.result

/**
 * Sealed class đại diện cho kết quả của API calls
 * Giúp handle states một cách type-safe
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: Exception, val message: String? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
    
    /**
     * Kiểm tra xem result có phải Success không
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Kiểm tra xem result có phải Error không
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Kiểm tra xem result có phải Loading không
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Lấy data nếu Success, ngược lại trả về null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * Lấy data nếu Success, ngược lại throw exception
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Still loading")
    }
}

/**
 * Extension function để execute action khi Success
 */
inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

/**
 * Extension function để execute action khi Error
 */
inline fun <T> ApiResult<T>.onError(action: (Exception, String?) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) action(exception, message)
    return this
}

/**
 * Extension function để execute action khi Loading
 */
inline fun <T> ApiResult<T>.onLoading(action: () -> Unit): ApiResult<T> {
    if (this is ApiResult.Loading) action()
    return this
}

/**
 * Map data từ type T sang type R
 */
inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Error -> ApiResult.Error(exception, message)
    is ApiResult.Loading -> ApiResult.Loading
}

/**
 * FlatMap cho nested ApiResult
 */
inline fun <T, R> ApiResult<T>.flatMap(transform: (T) -> ApiResult<R>): ApiResult<R> = when (this) {
    is ApiResult.Success -> transform(data)
    is ApiResult.Error -> ApiResult.Error(exception, message)
    is ApiResult.Loading -> ApiResult.Loading
}