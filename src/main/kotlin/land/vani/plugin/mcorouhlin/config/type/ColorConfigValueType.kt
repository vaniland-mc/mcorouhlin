package land.vani.plugin.mcorouhlin.config.type

import land.vani.plugin.mcorouhlin.config.Config
import org.bukkit.Color

object ColorConfigValueType : ConfigValueType<Color?> {
    override fun get(config: Config, node: String): Color? =
        config.getUnsafe(node)

    override fun set(config: Config, node: String, value: Color?) {
        config.setUnsafe(node, value)
    }

    override fun getList(config: Config, node: String): MutableList<Color?> =
        config.getUnsafeList(node)

    override fun setList(config: Config, node: String, values: List<Color?>) {
        config.setUnsafe(node, values)
    }
}
