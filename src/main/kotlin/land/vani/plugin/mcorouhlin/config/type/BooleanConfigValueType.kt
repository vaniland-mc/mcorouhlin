package land.vani.plugin.mcorouhlin.config.type

import land.vani.plugin.mcorouhlin.config.Config

object BooleanConfigValueType : ConfigValueType<Boolean?> {
    override fun get(config: Config, node: String): Boolean? =
        config.getUnsafe(node)

    override fun set(config: Config, node: String, value: Boolean?) {
        config.setUnsafe(node, value)
    }

    override fun getList(config: Config, node: String): MutableList<Boolean?> =
        config.getUnsafeList(node)

    override fun setList(config: Config, node: String, values: List<Boolean?>) {
        config.setUnsafe(node, values)
    }
}
