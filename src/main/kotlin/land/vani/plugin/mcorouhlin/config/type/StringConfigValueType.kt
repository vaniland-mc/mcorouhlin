package land.vani.plugin.mcorouhlin.config.type

import land.vani.plugin.mcorouhlin.config.Config

object StringConfigValueType : ConfigValueType<String?> {
    override fun get(config: Config, node: String): String? =
        config.getUnsafe(node)

    override fun set(config: Config, node: String, value: String?) {
        config.setUnsafe(node, value)
    }

    override fun getList(config: Config, node: String): MutableList<String?> =
        config.getUnsafeList(node)

    override fun setList(config: Config, node: String, values: List<String?>) {
        config.setUnsafe(node, values)
    }
}
