package land.vani.mcorouhlin.paper.dispatcher

import land.vani.mcorouhlin.dispatcher.MinecraftAsyncDispatcher
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

internal class BukkitMinecraftAsyncDispatcher(
    private val plugin: Plugin,
) : MinecraftAsyncDispatcher() {
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
