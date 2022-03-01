package land.vani.mcorouhlin.permission

/**
 * Represents a registerer of permissions.
 */
interface PermissionRegisterer {
    /**
     * Register [permissions] to this registerer.
     */
    fun registerPermissions(permissions: Iterable<Permission>)

    /**
     * Register [permissions] to this registerer.
     */
    fun registerPermissions(vararg permissions: Permission) = registerPermissions(permissions.toList())
}

/**
 * Register [T] permissions to this registerer.
 */
inline fun <reified T> PermissionRegisterer.registerPermissions() where T : Permission, T : Enum<T> =
    registerPermissions(enumValues<T>().toList())
