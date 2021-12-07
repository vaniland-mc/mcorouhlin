package land.vani.plugin.mcorouhlin.config.type

import land.vani.plugin.mcorouhlin.config.Config

object LongConfigValueType : ConfigValueType<Long?> {
    override fun get(config: Config, node: String): Long? =
        config.getUnsafe(node)

    override fun set(config: Config, node: String, value: Long?) {
        config.setUnsafe(node, value)
    }

    override fun getList(config: Config, node: String): MutableList<Long?> =
        config.getUnsafeList(node)

    override fun setList(config: Config, node: String, values: List<Long?>) {
        config.setUnsafe(node, values)
    }
}
