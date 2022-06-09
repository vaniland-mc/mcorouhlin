package land.vani.mcorouhlin.config

import kotlin.reflect.KClass

@Suppress("UnnecessaryAbstractClass")
abstract class Configuration<C : Configuration<C>>(
    private val source: ConfigurationSource,
) {
    suspend fun reload() {
        source.load()
    }

    suspend fun save() {
        source.save()
    }

    fun <T : Any> value(node: String, clazz: KClass<T>): ConfigurationDelegate<C, T?, T> =
        ConfigurationDelegate(source, node, clazz, { it }, { it })

    inline fun <reified T : Any> value(node: String) = value(node, T::class)
}
