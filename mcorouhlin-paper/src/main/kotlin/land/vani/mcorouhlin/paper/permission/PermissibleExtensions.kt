package land.vani.mcorouhlin.paper.permission

import land.vani.mcorouhlin.permission.Permission
import net.kyori.adventure.util.TriState
import org.bukkit.permissions.Permissible

/**
 * Gets the value of the specified [permission], if set.
 *
 * If a permission override is not set on this object, the default value of the permission will be returned.
 */
fun Permissible.hasPermission(permission: Permission): Boolean =
    hasPermission(permission.asBukkit)

/**
 * Checks if this object contains an override for the specified [Permission].
 */
fun Permissible.isPermissionSet(permission: Permission): Boolean =
    isPermissionSet(permission.asBukkit)

/**
 * Checks if this object has a [permission] set and, if it is set, the value of the permission.
 */
fun Permissible.permissionValue(permission: Permission): TriState =
    permissionValue(permission.asBukkit)
