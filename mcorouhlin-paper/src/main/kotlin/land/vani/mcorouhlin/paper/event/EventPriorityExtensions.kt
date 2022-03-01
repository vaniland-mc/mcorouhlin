package land.vani.mcorouhlin.paper.event

import land.vani.mcorouhlin.event.EventPriority

/**
 * Get [EventPriority] as bukkit's EventPriority.
 */
val EventPriority.asBukkit: org.bukkit.event.EventPriority
    get() = when (this) {
        EventPriority.LOWEST -> org.bukkit.event.EventPriority.LOWEST
        EventPriority.LOW -> org.bukkit.event.EventPriority.LOW
        EventPriority.NORMAL -> org.bukkit.event.EventPriority.NORMAL
        EventPriority.HIGH -> org.bukkit.event.EventPriority.HIGH
        EventPriority.HIGHEST -> org.bukkit.event.EventPriority.HIGHEST
    }
