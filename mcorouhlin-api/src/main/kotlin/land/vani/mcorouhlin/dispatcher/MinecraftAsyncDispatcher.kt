package land.vani.mcorouhlin.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.annotations.NonBlockingExecutor

@Suppress("UnnecessaryAbstractClass")
@NonBlockingExecutor
abstract class MinecraftAsyncDispatcher : CoroutineDispatcher()
