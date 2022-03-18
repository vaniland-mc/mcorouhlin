package land.vani.mcorouhlin.config

import kotlin.reflect.KClass

interface ConfigurationSource {
    suspend fun load()

    suspend fun save()

    fun get(node: String, clazz: KClass<*>): Any?

    fun set(node: String, value: Any?)

    override fun toString(): String
}
