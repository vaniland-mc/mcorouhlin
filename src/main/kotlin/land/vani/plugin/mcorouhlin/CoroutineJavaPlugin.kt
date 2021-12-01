package land.vani.plugin.mcorouhlin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import land.vani.plugin.mcorouhlin.dispatcher.MinecraftAsyncDispatcher
import land.vani.plugin.mcorouhlin.dispatcher.MinecraftMainThreadDispatcher
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File
import kotlin.coroutines.CoroutineContext

abstract class CoroutineJavaPlugin : JavaPlugin, CoroutinePlugin, CoroutineScope {
    @Suppress("unused")
    constructor() : super()

    constructor(
        loader: JavaPluginLoader,
        description: PluginDescriptionFile,
        dataFolder: File,
        file: File,
    ) : super(loader, description, dataFolder, file)

    override val coroutineContext: CoroutineContext by lazy {
        Job() + mainThreadDispatcher
    }

    override val mainThreadDispatcher: CoroutineDispatcher by lazy {
        MinecraftMainThreadDispatcher(this)
    }
    override val asyncDispatcher: CoroutineDispatcher by lazy {
        MinecraftAsyncDispatcher(this)
    }

    override suspend fun onEnableAsync() {}
    override suspend fun onDisableAsync() {}
    override suspend fun onLoadAsync() {}

    override fun onEnable() {
        runBlocking {
            onEnableAsync()
        }
    }

    override fun onDisable() {
        runBlocking {
            onDisableAsync()
        }
    }

    override fun onLoad() {
        runBlocking {
            onLoadAsync()
        }
    }
}
