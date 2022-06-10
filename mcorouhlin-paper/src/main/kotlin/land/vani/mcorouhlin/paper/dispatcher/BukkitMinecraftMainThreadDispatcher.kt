package land.vani.mcorouhlin.paper.dispatcher

import land.vani.mcorouhlin.dispatcher.MinecraftMainThreadDispatcher
import land.vani.mcorouhlin.paper.McorouhlinKotlinPlugin
import land.vani.mcorouhlin.paper.util.ensureWakeup
import kotlin.coroutines.CoroutineContext

internal class BukkitMinecraftMainThreadDispatcher(
    private val plugin: McorouhlinKotlinPlugin,
) : MinecraftMainThreadDispatcher() {
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        plugin.ensureWakeup()
        return plugin.server.isPrimaryThread
    }

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
