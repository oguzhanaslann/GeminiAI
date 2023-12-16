package com.oguzhanaslann.geminiai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

sealed class DataState<out T> {
    object Loading : DataState<Nothing>()
    object Initial : DataState<Nothing>()
    data class Success<T>(val data: T) : DataState<T>()
    data class Error(val exception: Throwable) : DataState<Nothing>()
}

val DataState<*>?.isLoading: Boolean
    get() = this != null && this is DataState.Loading

val DataState<*>?.isInitial: Boolean
    get() = this != null && this is DataState.Initial

val DataState<*>?.isNotLoading: Boolean
    get() = !isLoading

val DataState<*>.isSuccess: Boolean
    get() = this is DataState.Success

val DataState<*>.isError: Boolean
    get() = this is DataState.Error

val DataState<*>.isFinalized: Boolean
    get() = (this != DataState.Loading && this != DataState.Initial)

inline fun <T> DataState<T>.isLoading(crossinline block: (isLoading: Boolean) -> Unit): DataState<T> {
    block(this is DataState.Loading)
    return this
}

inline fun <T> DataState<T>.onLoading(block: () -> Unit): DataState<T> {
    if (this is DataState.Loading) {
        block()
    }
    return this
}

inline fun <T> DataState<T>.onInitial(block: () -> Unit): DataState<T> {
    if (this is DataState.Initial) {
        block()
    }
    return this
}

inline fun <T> DataState<T>.onSuccess(block: (T) -> Unit): DataState<T> {
    if (this is DataState.Success) {
        block(this.data)
    }
    return this
}

inline fun <T> DataState<T>.onError(block: (error: Throwable) -> Unit): DataState<T> {
    if (this is DataState.Error) {
        block(this.exception)
    }
    return this
}

inline fun <T> DataState<T>.isFinalized(block: () -> Unit): DataState<T> {
    if (isFinalized) {
        block()
    }
    return this
}

val DataState<*>.isRefreshable: Boolean
    get() = isNotLoading && !isSuccess

inline fun <T> DataState<T>.onRefreshable(block: (T?) -> Unit): DataState<T> {
    if (isRefreshable) {
        block(dataOrNull())
    }
    return this
}

@JvmName("mapByStateNullable")
inline fun <reified T, reified R> DataState<T>.mapToNullable(
    crossinline block: (T) -> R?,
): DataState<R?> {
    return when (this) {
        is DataState.Error -> DataState.Error(this.exception)
        DataState.Initial -> DataState.Initial
        DataState.Loading -> DataState.Loading
        is DataState.Success -> DataState.Success(block(this.data))
    }
}

inline fun <T, R> DataState<T>.map(
     block: (T) -> R,
): DataState<R> {
    return when (this) {
        is DataState.Error -> DataState.Error(this.exception)
        DataState.Initial -> DataState.Initial
        DataState.Loading -> DataState.Loading
        is DataState.Success -> DataState.Success(block(this.data))
    }
}


fun <T> DataState<T>.mapUnit() = map { Unit }

fun <T> DataState<T>.dataOrNull(): T? {
    return if (this is DataState.Success) this.data else null
}

fun <T> DataState<T>.dataOrNull(default: T): T {
    return if (this is DataState.Success) this.data else default
}

fun <T> DataState<T>.dataOrDefault(default: T): T {
    return dataOrNull(default)
}

fun <T> DataState<List<T>>.dataOrEmpty(): List<T> {
    return if (this is DataState.Success) this.data else emptyList()
}

fun <T> DataState<T>.errorOrNull(): Throwable? {
    return if (this is DataState.Error) this.exception else null
}

fun <T> DataState<List<T>>.onData(block: (List<T>) -> Unit): DataState<List<T>> {
    if (this is DataState.Success && this.data.isNotEmpty()) {
        block(this.data)
    }
    return this
}

val <T> DataState<List<T>>.hasData: Boolean
    get() = this is DataState.Success && this.data.isNotEmpty()

fun <T, R> DataState<List<T>>.flatMap(block: (T) -> R): DataState<List<R>> {
    return when (this) {
        is DataState.Success -> DataState.Success(this.data.map(block))
        is DataState.Error -> DataState.Error(this.exception)
        DataState.Initial -> DataState.Initial
        DataState.Loading -> DataState.Loading
    }
}

fun <T> DataState<List<T>>.onEmpty(block: () -> Unit): DataState<List<T>> {
    if (this is DataState.Success && this.data.isEmpty()) {
        block()
    }
    return this
}

fun <T> Result<T>.toDataState(): DataState<T> {
    val finalState = when {
        isSuccess -> DataState.Success(getOrNull()!!)
        isFailure -> DataState.Error(exceptionOrNull()!!)
        else -> DataState.Error(UnknownError())
    }

    return finalState
}

fun <T, K, R> DataState<T>.combine(other: DataState<K>, map: (T, K) -> R): DataState<R> {
    return when {
        this is DataState.Success && other is DataState.Success -> DataState.Success(
            map(
                this.data,
                other.data
            )
        )

        this is DataState.Error -> DataState.Error(this.exception)
        other is DataState.Error -> DataState.Error(other.exception)
        this is DataState.Loading || other is DataState.Loading -> DataState.Loading
        else -> DataState.Initial
    }
}

fun <T, K, L, R> DataState<T>.combine(
    other: DataState<K>,
    other2: DataState<L>,
    map: (T, K, L) -> R,
): DataState<R> {
    return when {
        this is DataState.Success && other is DataState.Success && other2 is DataState.Success -> DataState.Success(
            map(this.data, other.data, other2.data)
        )

        this is DataState.Error -> DataState.Error(this.exception)
        other is DataState.Error -> DataState.Error(other.exception)
        other2 is DataState.Error -> DataState.Error(other2.exception)
        this is DataState.Loading || other is DataState.Loading || other2 is DataState.Loading -> DataState.Loading
        else -> DataState.Initial
    }
}

fun <T, K> DataState<T>.combine(other: DataState<K>): DataState<Pair<T, K>> {
    return when {
        this is DataState.Success && other is DataState.Success -> DataState.Success(this.data to other.data)
        this is DataState.Error -> DataState.Error(this.exception)
        other is DataState.Error -> DataState.Error(other.exception)
        this is DataState.Loading || other is DataState.Loading -> DataState.Loading
        else -> DataState.Initial
    }
}

fun <T> Flow<DataState<T>>.reduceToDataFlow(): Flow<T> =
    filter { it is DataState.Success }.map { (it as DataState.Success).data }


fun <T, K> DataState<List<T>>.flatmap(mapper: (T) -> K): DataState<List<K>> {
    return this.map { it.map(mapper) }
}

fun <T> Flow<DataState<T>>.filterSuccess(): Flow<T> =
    filter { it is DataState.Success }.map { (it as DataState.Success).data }
