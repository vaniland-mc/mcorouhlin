package land.vani.mcorouhlin.paper.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.adventure.AdventureComponent
import land.vani.mcorouhlin.command.arguments.MultipleArgumentParser
import net.kyori.adventure.extra.kotlin.text
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.concurrent.CompletableFuture

class WorldInput(
    internal val worlds: List<World>,
)

class WorldArgumentType(
    private val predicate: (World) -> Boolean,
    private val allowMultipleWorlds: Boolean,
) : ArgumentType<WorldInput> {
    companion object {
        private val ERROR_WORLD_NOT_FOUND = SimpleCommandExceptionType(
            AdventureComponent(
                text {
                    content("World not found")
                }
            )
        )
        private val ERROR_WORLD_NOT_MEET_CONDITION =
            SimpleCommandExceptionType(
                AdventureComponent(
                    text {
                        content("World not meed condition")
                    }
                )
            )

        private val EXAMPLES = listOf("world")

        fun getSingleWorld(context: CommandContext<*>, name: String): World =
            context.getArgument(name, WorldInput::class.java).worlds.single()

        fun getMultipleWorlds(context: CommandContext<*>, name: String): Collection<World> =
            context.getArgument(name, WorldInput::class.java).worlds
    }

    override fun parse(reader: StringReader): WorldInput {
        val start = reader.cursor

        val worlds = if (allowMultipleWorlds) {
            val parser = MultipleArgumentParser(reader) { input ->
                (
                    Bukkit.getWorld(input)
                        ?: throw ERROR_WORLD_NOT_FOUND.createWithContext(reader)
                    )
                    .takeIf(predicate)
                    ?: run {
                        reader.cursor = start
                        throw ERROR_WORLD_NOT_MEET_CONDITION.createWithContext(reader)
                    }
            }
            parser.parse()
            parser.parsedValues
        } else {
            val string = reader.readUnquotedString()
            val world = (
                Bukkit.getWorld(string)
                    ?: throw ERROR_WORLD_NOT_FOUND.createWithContext(reader)
                ).takeIf(predicate)
                ?: run {
                    reader.cursor = start
                    throw ERROR_WORLD_NOT_MEET_CONDITION.createWithContext(reader)
                }

            listOf(world)
        }

        return WorldInput(worlds)
    }

    override fun getExamples(): Collection<String> = EXAMPLES

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> = if (allowMultipleWorlds) {
        suggestSingle(builder)
    } else {
        suggestMultiple(builder)
    }

    private fun suggestSingle(
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val reader = StringReader(builder.input)
        reader.cursor = builder.start
        val input = reader.readUnquotedString()

        suggestWorlds(builder, input)

        return builder.buildFuture()
    }

    private fun suggestMultiple(
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val reader = StringReader(builder.input)
        reader.cursor = builder.start
        val parser = MultipleArgumentParser(reader) { input ->
            (Bukkit.getWorld(input) ?: throw ERROR_WORLD_NOT_FOUND.createWithContext(reader))
                .takeIf(predicate)
                ?: throw ERROR_WORLD_NOT_MEET_CONDITION.createWithContext(reader)
        }

        try {
            parser.parse()
        } catch (ignored: CommandSyntaxException) {
        }

        return parser.fillSuggestions(
            builder,
            Bukkit.getWorlds().map { it.name }
        )
    }

    private fun suggestWorlds(builder: SuggestionsBuilder, input: String) {
        Bukkit.getWorlds()
            .filter(predicate)
            .filter { it.name.startsWith(input, ignoreCase = true) }
            .forEach { world -> builder.suggest(world.name) }
    }
}
