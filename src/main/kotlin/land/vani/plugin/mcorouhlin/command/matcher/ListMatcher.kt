package land.vani.plugin.mcorouhlin.command.matcher

import land.vani.plugin.mcorouhlin.command.CommandExecutionContext

fun listMatcher(args: Iterable<String>) = object : CommandExecutionContext<String> {
    override fun candidates(): List<String> = args.toList()
    override fun parse(raw: String): String? = args.firstOrNull { it == raw }
}

fun listMatcher(vararg args: String) = listMatcher(args.toList())
