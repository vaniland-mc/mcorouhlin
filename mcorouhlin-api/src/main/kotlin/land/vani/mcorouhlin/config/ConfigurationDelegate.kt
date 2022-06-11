package land.vani.mcorouhlin.config

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class ConfigurationDelegate<C : Configuration<C>, T>(
    private val source: ConfigurationSource,
    private val node: String,
    private val clazz: KClass<*>,
) : ReadWriteProperty<C, T> {
    override fun getValue(thisRef: C, property: KProperty<*>): T = source.get(node, clazz) as T

    override fun setValue(thisRef: C, property: KProperty<*>, value: T) {
        source.set(node, value)
    }

    fun default(value: T & Any): ConfigurationNotNullDelegate<C, T & Any> = default { value }

    fun default(defaultFn: C.() -> T & Any): ConfigurationNotNullDelegate<C, T & Any> =
        ConfigurationNotNullDelegate(source, node, clazz, defaultFn)

    fun strict(): ConfigurationNotNullDelegate<C, T & Any> = default {
        throw NullPointerException("node '$node' is required but the value in file $source is null")
    }

    fun <COMPLEX> transform(
        rawToComplex: C.(T) -> COMPLEX,
        complexToRaw: C.(COMPLEX) -> T,
    ): ConfigurationTransformDelegate<C, T, COMPLEX> = ConfigurationTransformDelegate(
        source,
        node,
        clazz,
        rawToComplex,
        complexToRaw,
    )
}

class ConfigurationTransformDelegate<C : Configuration<C>, RAW, COMPLEX>(
    private val source: ConfigurationSource,
    private val node: String,
    private val clazz: KClass<*>,
    private val rawToComplex: C.(RAW) -> COMPLEX,
    private val complexToRaw: C.(COMPLEX) -> RAW,
) : ReadWriteProperty<C, COMPLEX> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: C, property: KProperty<*>): COMPLEX =
        thisRef.rawToComplex(source.get(node, clazz) as RAW)

    override fun setValue(thisRef: C, property: KProperty<*>, value: COMPLEX) {
        source.set(node, thisRef.complexToRaw(value))
    }

    fun <NEW_COMPLEX> transform(
        newRawToComplex: C.(COMPLEX) -> NEW_COMPLEX,
        newComplexToRaw: C.(NEW_COMPLEX) -> COMPLEX,
    ): ConfigurationTransformDelegate<C, RAW, NEW_COMPLEX> = ConfigurationTransformDelegate(
        source,
        node,
        clazz,
        { newRawToComplex(rawToComplex(it)) },
        { complexToRaw(newComplexToRaw(it)) },
    )
}

class ConfigurationNotNullDelegate<C : Configuration<C>, T : Any>(
    private val source: ConfigurationSource,
    private val node: String,
    private val clazz: KClass<*>,
    private val defaultFn: C.() -> T,
) : ReadWriteProperty<C, T> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: C, property: KProperty<*>): T =
        source.get(node, clazz) as T? ?: thisRef.defaultFn()

    override fun setValue(thisRef: C, property: KProperty<*>, value: T) {
        source.set(node, value)
    }

    fun <COMPLEX> transform(
        rawToComplex: C.(T) -> COMPLEX,
        complexToRaw: C.(COMPLEX) -> T,
    ): ConfigurationTransformDelegate<C, T, COMPLEX> =
        ConfigurationTransformDelegate(source, node, clazz, rawToComplex, complexToRaw)
}
