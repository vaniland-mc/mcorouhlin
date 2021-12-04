package land.vani.plugin.mcorouhlin.permission

import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.Plugin

interface Permission {
    val node: String
    val description: String?
    val default: PermissionDefault
    val children: Map<Permission, Boolean>
}

@Suppress("unused")
inline fun <reified T> Plugin.registerPermissions() where T : Permission, T : Enum<T> {
    registerPermissions(enumValues<T>().toList())
}

@Suppress("unused")
fun Plugin.registerPermissions(vararg permission: Permission) {
    registerPermissions(permission.toList())
}

fun Plugin.registerPermissions(permissions: Iterable<Permission>) {
    permissions.forEach { perms ->
        val bukkitPerms = org.bukkit.permissions.Permission(
            perms.node,
            perms.description,
            perms.default,
            perms.children.mapKeys { it.key.node }
        )
        server.pluginManager.addPermission(bukkitPerms)
    }
}
