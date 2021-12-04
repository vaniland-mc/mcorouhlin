package land.vani.plugin.mcorouhlin.command.matcher

import land.vani.plugin.mcorouhlin.command.CommandExecutionContext
import org.bukkit.entity.EntityType

private object EntityMatcher : CommandExecutionContext<EntityType> {
    private val candidates = enumValues<EntityType>().map { it.name }

    override fun candidates(): List<String> = candidates
    override fun parse(raw: String): EntityType? = runCatching {
        enumValueOf<EntityType>(raw.uppercase())
    }.getOrNull()
}

fun entityMatcher(): CommandExecutionContext<EntityType> = EntityMatcher
