package land.vani.mcorouhlin.paper.permission

import land.vani.mcorouhlin.permission.PermissionDefault

/**
 * Get [PermissionDefault] as bukkit's EventPriority.
 */
val PermissionDefault.asBukkit: org.bukkit.permissions.PermissionDefault
    get() = when (this) {
        PermissionDefault.TRUE -> org.bukkit.permissions.PermissionDefault.TRUE
        PermissionDefault.FALSE -> org.bukkit.permissions.PermissionDefault.FALSE
        PermissionDefault.OP -> org.bukkit.permissions.PermissionDefault.OP
        PermissionDefault.NOT_OP -> org.bukkit.permissions.PermissionDefault.NOT_OP
    }
