package land.vani.plugin.mcorouhlin.command.matcher

import land.vani.plugin.mcorouhlin.command.CommandExecutionContext
import org.bukkit.Material

private object MaterialMatcher : CommandExecutionContext<Material> {
    private val candidates = enumValues<Material>().map { it.name }

    override fun candidates(): List<String> = candidates

    override fun parse(raw: String): Material? = runCatching {
        enumValueOf<Material>(raw.uppercase())
    }.getOrNull()
}

fun materialMatcher(): CommandExecutionContext<Material> = MaterialMatcher

private object BlockMatcher : CommandExecutionContext<Material> {
    private val candidates = enumValues<Material>().filter { it.isBlock }.map { it.name }

    override fun candidates(): List<String> = candidates
    override fun parse(raw: String): Material? = runCatching {
        enumValueOf<Material>(raw.uppercase())
    }.getOrNull()?.takeIf { it.isBlock }
}

fun blockMatcher(): CommandExecutionContext<Material> = BlockMatcher
