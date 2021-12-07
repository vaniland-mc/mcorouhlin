package land.vani.plugin.mcorouhlin.config.type

import land.vani.plugin.mcorouhlin.config.Config

interface ConfigValueType<T> {
    fun get(config: Config, node: String): T?
    fun set(config: Config, node: String, value: T)
    fun getList(config: Config, node: String): MutableList<T>
    fun setList(config: Config, node: String, values: List<T>)
}
