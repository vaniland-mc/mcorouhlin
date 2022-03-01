package land.vani.mcorouhlin.permission

/**
 * Represents the possible default values for permissions.
 */
enum class PermissionDefault {
    TRUE,
    FALSE,
    OP,
    NOT_OP,
    ;

    /**
     * Calculates the value of this PermissionDefault for the given operator value
     *
     * @param op If the target is op
     */
    fun getValue(op: Boolean): Boolean = when (this) {
        TRUE -> true
        FALSE -> false
        OP -> op
        NOT_OP -> !op
    }
}
