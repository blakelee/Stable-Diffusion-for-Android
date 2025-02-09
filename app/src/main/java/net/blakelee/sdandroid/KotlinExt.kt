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
    val first = combine(flow, flow2, flow3, flow4, ::Tuple4)
    val second = combine(flow5, flow6, flow7, ::Triple)

    return combine(first, second) { first, second ->
        transform(
            first.first,
            first.second,
            first.third,
            first.fourth,
            second.first,
            second.second,
            second.third
        )
    }
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R
): Flow<R> {
    val first = combine(flow, flow2, flow3, flow4, ::Tuple4)
    val second = combine(flow5, flow6, flow7, flow8, flow9, ::Tuple5)

    return combine(first, second, flow7) { first, second, third ->
        transform(
            first.first,
            first.second,
            first.third,
            first.fourth,
            second.first,
            second.second,
            second.third,
            second.fourth,
            second.fifth
        )
    }
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    flow10: Flow<T10>,
    flow11: Flow<T11>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) -> R
): Flow<R> {
    val first = combine(flow, flow2, flow3, flow4, flow5, ::Tuple5)
    val second = combine(flow6, flow7, flow8, flow9, flow10, ::Tuple5)

    return combine(first, second, flow11) { first, second, third ->
        transform(
            first.first,
            first.second,
            first.third,
            first.fourth,
            first.fifth,
            second.first,
            second.second,
            second.third,
            second.fourth,
            second.fifth,
            third
        )
    }
}

data class Tuple4<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class Tuple5<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

data class Tuple11<out T1, out T2, out T3, out T4, out T5, out T6, out T7, out T8, out T9, out T10, out T11>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4,
    val fifth: T5,
    val sixth: T6,
    val seventh: T7,
    val eighth: T8,
    val ninth: T9,
    val tenth: T10,
    val eleventh: T11
)