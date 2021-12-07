package land.vani.plugin.mcorouhlin.config

import land.vani.plugin.mcorouhlin.config.type.BooleanConfigValueType
import land.vani.plugin.mcorouhlin.config.type.ColorConfigValueType
import land.vani.plugin.mcorouhlin.config.type.DoubleConfigValueType
import land.vani.plugin.mcorouhlin.config.type.IntConfigValueType
import land.vani.plugin.mcorouhlin.config.type.LocationConfigType
import land.vani.plugin.mcorouhlin.config.type.LongConfigValueType
import land.vani.plugin.mcorouhlin.config.type.StringConfigValueType
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.readText
import kotlin.io.path.writeText

abstract class Config(
    private val path: Path,
) {
    constructor(plugin: Plugin, fileName: String) : this(
        plugin.dataFolder.toPath() / fileName
    )

    private val config = YamlConfiguration()

    init {
        reload()
    }

    protected fun string(node: String) = ConfigValueDelegateNullable(this, node, StringConfigValueType)

    protected fun int(node: String) = ConfigValueDelegateNullable(this, node, IntConfigValueType)

    protected fun long(node: String) = ConfigValueDelegateNullable(this, node, LongConfigValueType)

    protected fun double(node: String) = ConfigValueDelegateNullable(this, node, DoubleConfigValueType)

    protected fun boolean(node: String) = ConfigValueDelegateNullable(this, node, BooleanConfigValueType)

    protected fun location(node: String) = ConfigValueDelegateNullable(this, node, LocationConfigType)

    protected fun color(node: String) = ConfigValueDelegateNullable(this, node, ColorConfigValueType)

    @Suppress("UNCHECKED_CAST")
    fun <T> getUnsafe(node: String): T =
        config[node] as T

    @Suppress("UNCHECKED_CAST")
    fun <T> getUnsafeList(node: String): MutableList<T> =
        (config.getList(node) ?: mutableListOf()) as MutableList<T>

    fun setUnsafe(node: String, value: Any?) {
        config[node] = value
    }

    fun reload() {
        config.loadFromString(path.readText())
    }

    fun save() {
        val configValue = config.saveToString()
        path.writeText(configValue)
    }
}
