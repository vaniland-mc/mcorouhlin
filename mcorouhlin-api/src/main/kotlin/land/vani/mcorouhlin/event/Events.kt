package land.vani.mcorouhlin.event

import kotlin.reflect.KClass

/**
 * Interface for registering events.
 */
interface Events<E : Any> {
    /**
     * Register event handler.
     *
     * @param clazz KClass of the event.
     * @param priority priority of event handler.
     * @param ignoreCancelled ignore when an event has been canceled.
     */
    fun <T : E> on(
        clazz: KClass<T>,
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        block: suspend (T) -> Unit,
    )
}
