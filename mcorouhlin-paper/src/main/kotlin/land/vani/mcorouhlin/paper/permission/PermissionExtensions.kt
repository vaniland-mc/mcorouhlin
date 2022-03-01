package land.vani.mcorouhlin.paper.permission

import land.vani.mcorouhlin.permission.Permission

/**
 * Get [Permission] as bukkit's Permission.
 */
val Permission.asBukkit: org.bukkit.permissions.Permission
    get() = org.bukkit.permissions.Permission(
        node,
        description,
        default?.asBukkit,
        children.mapKeys { it.key.node },
    )
