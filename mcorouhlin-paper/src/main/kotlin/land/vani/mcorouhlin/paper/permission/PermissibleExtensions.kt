package land.vani.mcorouhlin.paper.permission

import land.vani.mcorouhlin.permission.Permission
import org.bukkit.permissions.Permissible

/**
 * [Permissible] has [permission].
 */
fun Permissible.hasPermission(permission: Permission): Boolean =
    hasPermission(permission.asBukkit)
