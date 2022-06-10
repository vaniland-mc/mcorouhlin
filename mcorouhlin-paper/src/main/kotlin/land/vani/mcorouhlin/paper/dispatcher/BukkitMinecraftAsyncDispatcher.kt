package land.vani.mcorouhlin.paper.dispatcher

import land.vani.mcorouhlin.dispatcher.MinecraftAsyncDispatcher
import land.vani.mcorouhlin.paper.McorouhlinKotlinPlugin
import land.vani.mcorouhlin.paper.util.ensureWakeup
import kotlin.coroutines.CoroutineContext

internal class BukkitMinecraftAsyncDispatcher(
    private val plugin: McorouhlinKotlinPlugin,
) : MinecraftAsyncDispatcher() {
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        plugin.ensureWakeup()
        return plugin.server.isPrimaryThread
    }

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
