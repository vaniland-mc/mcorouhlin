package land.vani.plugin.mcorouhlin.config.type

import land.vani.plugin.mcorouhlin.config.Config

class NotNullConfigValueType<T : Any>(
    private val inner: ConfigValueType<T?>,
) : ConfigValueType<T> {
    override fun get(config: Config, node: String): T? =
        inner.get(config, node)

    override fun set(config: Config, node: String, value: T) {
        inner.set(config, node, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getList(config: Config, node: String): MutableList<T> =
        inner.getList(config, node) as MutableList<T>

    override fun setList(config: Config, node: String, values: List<T>) {
        inner.setList(config, node, values)
    }
}
