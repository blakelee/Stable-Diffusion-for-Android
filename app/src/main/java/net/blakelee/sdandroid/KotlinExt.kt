package net.blakelee.sdandroid

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R
): Flow<R> {
    val first = combine(flow, flow2, flow3, ::Triple)
    val second = combine(flow4, flow5, flow6, ::Triple)

    return combine(first, second, flow7) { first, second, third ->
        transform(
            first.first,
            first.second,
            first.third,
            second.first,
            second.second,
            second.third,
            third
        )
    }
}