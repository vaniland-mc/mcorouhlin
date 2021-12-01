package land.vani.plugin.mcrouhlin

import land.vani.plugin.mcorouhlin.CoroutineJavaPlugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File

class MockCoroutinePlugin(
    loader: JavaPluginLoader,
    description: PluginDescriptionFile,
    dataFolder: File,
    file: File,
) : CoroutineJavaPlugin(loader, description, dataFolder, file)
