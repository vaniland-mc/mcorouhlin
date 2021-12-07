package land.vani.plugin.mcorouhlin.config

import land.vani.plugin.mcorouhlin.config.type.ConfigValueType
import land.vani.plugin.mcorouhlin.config.type.NotNullConfigValueType
import land.vani.plugin.mcorouhlin.util.ObservableList
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class ConfigValueDelegate<T>(
    private val config: Config,
    private val node: String,
    private val valueType: ConfigValueType<T>,
) : ReadWriteProperty<Config, T> {
    override fun getValue(thisRef: Config, property: KProperty<*>): T {
        @Suppress("UNCHECKED_CAST")
        return valueType.get(config, node).let {
            if (!property.returnType.isMarkedNullable && it == null) {
                requireNotNull()
            } else {
                it as T
            }
        }
    }

    override fun setValue(thisRef: Config, property: KProperty<*>, value: T) {
        if (!property.returnType.isMarkedNullable && value == null) {
            requireNotNull()
        }
        valueType.set(config, node, value)
    }

    private fun requireNotNull(): Nothing = throw ConfigValueIsNullException(node)

    fun list(): ConfigValueListDelegate<T> = ConfigValueListDelegate(
        config,
        node,
        valueType
    )
}

class ConfigValueDelegateNullable<T : Any>(
    private val config: Config,
    private val node: String,
    private val valueType: ConfigValueType<T?>,
) : ConfigValueDelegate<T?>(config, node, valueType) {
    override fun getValue(thisRef: Config, property: KProperty<*>): T? =
        valueType.get(config, node)

    override fun setValue(thisRef: Config, property: KProperty<*>, value: T?) =
        valueType.set(config, node, value)

    fun notNull(): ConfigValueDelegate<T> = ConfigValueDelegate(config, node, NotNullConfigValueType(valueType))
}

class ConfigValueListDelegate<T>(
    private val config: Config,
    private val node: String,
    private val valueType: ConfigValueType<T>,
) : ReadWriteProperty<Config, MutableList<T>> {
    override fun getValue(thisRef: Config, property: KProperty<*>): MutableList<T> {
        return ObservableList(valueType.getList(config, node)) {
            valueType.setList(config, node, it)
        }
    }

    override fun setValue(thisRef: Config, property: KProperty<*>, value: MutableList<T>) {
        valueType.setList(config, node, value)
    }
}
