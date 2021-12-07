package land.vani.plugin.mcorouhlin.config.type

import land.vani.plugin.mcorouhlin.config.Config

object DoubleConfigValueType : ConfigValueType<Double?> {
    override fun get(config: Config, node: String): Double? =
        config.getUnsafe(node)

    override fun set(config: Config, node: String, value: Double?) {
        config.setUnsafe(node, value)
    }

    override fun getList(config: Config, node: String): MutableList<Double?> =
        config.getUnsafeList(node)

    override fun setList(config: Config, node: String, values: List<Double?>) {
        config.setUnsafe(node, values)
    }
}
