package land.vani.mcorouhlin.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.annotations.BlockingExecutor

@BlockingExecutor
abstract class MinecraftMainThreadDispatcher : CoroutineDispatcher()
