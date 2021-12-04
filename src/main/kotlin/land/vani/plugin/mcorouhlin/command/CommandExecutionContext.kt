package land.vani.plugin.mcorouhlin.command

interface CommandExecutionContext<T> {
    fun parse(raw: String): T?
    fun candidates(): List<String>
}
