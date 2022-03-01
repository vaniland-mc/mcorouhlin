package land.vani.mcorouhlin.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.annotations.NonBlockingExecutor

@NonBlockingExecutor
abstract class MinecraftAsyncDispatcher : CoroutineDispatcher()
