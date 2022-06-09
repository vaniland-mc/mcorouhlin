package land.vani.mcorouhlin.paper.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import land.vani.mcorouhlin.config.ConfigurationSource
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.notExists
import kotlin.io.path.reader
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class BukkitConfigurationSource(
    private val path: Path,
) : ConfigurationSource {
    private val config: YamlConfiguration = YamlConfiguration()

    init {
        if (path.exists()) {
            config.load(path.reader())
        }
    }

    override suspend fun load() {
        withContext(Dispatchers.IO) {
            config.load(path.reader())
        }
    }

    override suspend fun save() {
        withContext(Dispatchers.IO) {
            if (path.parent.notExists()) {
                path.parent.createDirectories()
            }
            path.writeText(config.saveToString())
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(node: String, clazz: KClass<*>): Any? =
        when {
            clazz.isSubclassOf(ConfigurationSerializable::class) ->
                config.getSerializable(node, clazz.java as Class<out ConfigurationSerializable>)
            clazz.isSubclassOf(List::class) -> config.getList(node)
            clazz.isSubclassOf(Map::class) -> config.getConfigurationSection(node)?.getValues(true)
            else -> config.get(node)
        }

    override fun set(node: String, value: Any?) {
        config.set(node, value)
    }

    override fun toString(): String = path.toString()
}
