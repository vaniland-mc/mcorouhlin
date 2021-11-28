package land.vani.plugin.mcorouhlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import land.vani.plugin.mcorouhlin.dispatcher.MinecraftMainThreadDispatcher
import org.bukkit.plugin.java.JavaPlugin
import kotlin.coroutines.CoroutineContext

abstract class CoroutineJavaPlugin: JavaPlugin(), CoroutinePlugin, CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()

    override val mainThreadDispatcher: CoroutineDispatcher by lazy {
        MinecraftMainThreadDispatcher(this)
    }

    override fun onEnable() {
        runBlocking {
            onEnableAsync()
        }
    }

    override fun onDisable() {
        runBlocking {
            onDisableAsync()
            cancel()
        }
    }

    override fun onLoad() {
        runBlocking {
            onLoadAsync()
        }
    }
}
