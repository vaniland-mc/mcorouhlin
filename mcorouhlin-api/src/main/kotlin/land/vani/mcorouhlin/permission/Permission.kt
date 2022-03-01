package land.vani.mcorouhlin.permission

/**
 * Represents a unique permission that may be attached to a Permissible (e.x. player).
 */
interface Permission {
    /**
     * Returns the unique fully qualified node of this Permission
     */
    val node: String

    /**
     * Gets a brief description of this permission
     */
    val description: String?

    /**
     * Gets the default value of this permission.
     */
    val default: PermissionDefault?

    /**
     * Gets the children of this permission.
     */
    val children: Map<Permission, Boolean>
}
