package land.vani.mcorouhlin.paper.config

import land.vani.mcorouhlin.config.Configuration
import org.bukkit.plugin.Plugin
import java.nio.file.Path
import kotlin.io.path.div

abstract class BukkitConfiguration<C : BukkitConfiguration<C>>(path: Path) : Configuration<C>(
    BukkitConfigurationSource(path)
) {
    constructor(plugin: Plugin, fileName: String) : this(plugin.dataFolder.toPath() / fileName)
}
