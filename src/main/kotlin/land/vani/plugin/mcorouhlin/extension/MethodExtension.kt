package land.vani.plugin.mcorouhlin.extension

import java.lang.reflect.Method
import kotlin.coroutines.suspendCoroutine

internal suspend fun Method.invokeSuspend(obj: Any, vararg args: Any?): Any? =
    suspendCoroutine { invoke(obj, args, it) }
