package land.vani.plugin.mcorouhlin.config.type

import land.vani.plugin.mcorouhlin.config.Config

object IntConfigValueType : ConfigValueType<Int?> {
    override fun get(config: Config, node: String): Int? =
        config.getUnsafe(node)

    override fun set(config: Config, node: String, value: Int?) {
        config.setUnsafe(node, value)
    }

    override fun getList(config: Config, node: String): MutableList<Int?> =
        config.getUnsafeList(node)

    override fun setList(config: Config, node: String, values: List<Int?>) {
        config.setUnsafe(node, values)
    }
}
