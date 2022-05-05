package land.vani.mcorouhlin.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.annotations.BlockingExecutor

@Suppress("UnnecessaryAbstractClass")
@BlockingExecutor
abstract class MinecraftMainThreadDispatcher : CoroutineDispatcher()
