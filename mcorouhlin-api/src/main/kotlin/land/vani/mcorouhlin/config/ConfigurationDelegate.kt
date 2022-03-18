package land.vani.mcorouhlin.config

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class ConfigurationDelegate<C : Configuration<C>, R, T : Any>(
    private val source: ConfigurationSource,
    private val node: String,
    private val clazz: KClass<*>,
    private val rawToComplex: (R) -> T?,
    private val complexToRaw: (T?) -> R,
) : ReadWriteProperty<C, T?> {
    override fun getValue(thisRef: C, property: KProperty<*>): T? =
        rawToComplex(source.get(node, clazz) as R)

    override fun setValue(thisRef: C, property: KProperty<*>, value: T?) {
        source.set(node, complexToRaw(value))
    }

    fun default(value: T): ConfigurationDelegateNotNull<C, R, T> = default { value }

    fun default(defaultFn: C.() -> T): ConfigurationDelegateNotNull<C, R, T> =
        ConfigurationDelegateNotNull(
            source,
            node,
            clazz,
            defaultFn,
            { raw ->
                rawToComplex(raw)
                    ?: throw ConfigurationException(
                        "cannot convert from $raw to complex type"
                    )
            },
            complexToRaw
        )

    fun strict(): ConfigurationDelegateNotNull<C, R, T> = default {
        throw NullPointerException("node '$node' is required but the value in file $source is null")
    }

    fun transform(
        rawToComplex: (R) -> T?,
        complexToRaw: (T?) -> R,
    ): ConfigurationDelegate<C, R, T> =
        ConfigurationDelegate(source, node, clazz, rawToComplex, complexToRaw)
}

@Suppress("UNCHECKED_CAST")
class ConfigurationDelegateNotNull<C : Configuration<C>, R, T : Any>(
    private val source: ConfigurationSource,
    private val node: String,
    private val clazz: KClass<*>,
    private val defaultFn: C.() -> T,
    private val rawToComplex: (R) -> T,
    private val complexToRaw: (T) -> R,
) : ReadWriteProperty<C, T> {
    override fun getValue(thisRef: C, property: KProperty<*>): T =
        (source.get(node, clazz) as? R?)
            ?.let(rawToComplex)
            ?: defaultFn(thisRef)

    override fun setValue(thisRef: C, property: KProperty<*>, value: T) {
        source.set(node, complexToRaw(value))
    }

    fun transform(
        rawToComplex: (R) -> T,
        complexToRaw: (T) -> R,
    ): ConfigurationDelegateNotNull<C, R, T> =
        ConfigurationDelegateNotNull(source, node, clazz, defaultFn, rawToComplex, complexToRaw)
}
