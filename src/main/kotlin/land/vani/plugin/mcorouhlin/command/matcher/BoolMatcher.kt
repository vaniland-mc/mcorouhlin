package land.vani.plugin.mcorouhlin.command.matcher

import land.vani.plugin.mcorouhlin.command.CommandExecutionContext

private object BoolMatcher : CommandExecutionContext<Boolean> {
    private val candidates = listOf("true", "false")

    override fun candidates(): List<String> = candidates
    override fun parse(raw: String): Boolean? = raw.toBooleanStrictOrNull()
}

fun boolMatcher(): CommandExecutionContext<Boolean> = BoolMatcher
