package land.vani.plugin.mcorouhlin.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

internal class MinecraftMainThreadDispatcher(
    private val plugin: Plugin,
) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) {
            return
        }

        if (plugin.server.isPrimaryThread) {
            block.run()
        } else {
            plugin.server.scheduler.runTask(plugin, block)
        }
    }
}
