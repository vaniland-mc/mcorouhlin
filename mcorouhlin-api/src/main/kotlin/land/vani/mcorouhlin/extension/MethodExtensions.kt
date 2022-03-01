package land.vani.mcorouhlin.extension

import java.lang.reflect.Method

/**
 * Call this method with coroutine.
 */
suspend fun Method.invokeSuspend(obj: Any, vararg args: Any?): Any? =
    kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn { cont ->
        invoke(obj, *args, cont)
    }
