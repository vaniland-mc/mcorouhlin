package land.vani.mcorouhlin.paper

import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File

class TestMcorouhlinPlugin(
    loader: JavaPluginLoader,
    description: PluginDescriptionFile,
    dataFolder: File,
    file: File,
) : McorouhlinKotlinPlugin(loader, description, dataFolder, file)
