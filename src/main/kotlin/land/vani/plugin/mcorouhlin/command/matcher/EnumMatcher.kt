package land.vani.plugin.mcorouhlin.command.matcher

import land.vani.plugin.mcorouhlin.command.CommandExecutionContext

inline fun <reified T : Enum<T>> enumMatcher(): CommandExecutionContext<T> = object : CommandExecutionContext<T> {
    override fun candidates(): List<String> = enumValues<T>().map { it.name }
    override fun parse(raw: String): T? = runCatching {
        enumValueOf<T>(raw.uppercase())
    }.getOrNull()
}
