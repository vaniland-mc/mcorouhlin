package land.vani.plugin.mcorouhlin.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

internal class MinecraftAsyncDispatcher(
    private val plugin: Plugin,
) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) {
            return
        }

        if (plugin.server.isPrimaryThread) {
            plugin.server.scheduler.runTaskAsynchronously(plugin, block)
        } else {
            block.run()
        }
    }
}
