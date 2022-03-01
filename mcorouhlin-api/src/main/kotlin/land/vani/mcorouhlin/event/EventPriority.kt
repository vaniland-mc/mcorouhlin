package land.vani.mcorouhlin.event

/**
 * Represents an event's priority in execution
 */
enum class EventPriority {
    /**
     * Event call is of very low importance and should be run first,
     * to allow other plugins to further customise the outcome.
     */
    LOWEST,

    /**
     * Event call is of low importance.
     */
    LOW,

    /**
     * Event call is neither important nor unimportant, and may be run normally.
     */
    NORMAL,

    /**
     * Event call is of high importance.
     */
    HIGH,

    /**
     * Event call is critical and must have the final say in what happens to the event.
     */
    HIGHEST,
}
