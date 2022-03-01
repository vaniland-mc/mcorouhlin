package land.vani.mcorouhlin.paper.dispatcher

import land.vani.mcorouhlin.dispatcher.MinecraftMainThreadDispatcher
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

internal class BukkitMinecraftMainThreadDispatcher(
    private val plugin: Plugin,
) : MinecraftMainThreadDispatcher() {
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
