package land.vani.plugin.mcorouhlin.config.type

import land.vani.plugin.mcorouhlin.config.Config
import org.bukkit.Location

object LocationConfigType : ConfigValueType<Location?> {
    override fun get(config: Config, node: String): Location? =
        config.getUnsafe(node)

    override fun set(config: Config, node: String, value: Location?) {
        config.setUnsafe(node, value)
    }

    override fun getList(config: Config, node: String): MutableList<Location?> =
        config.getUnsafeList(node)

    override fun setList(config: Config, node: String, values: List<Location?>) {
        config.setUnsafe(node, values)
    }
}
