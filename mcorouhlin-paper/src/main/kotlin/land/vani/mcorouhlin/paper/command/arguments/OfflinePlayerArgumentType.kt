package land.vani.mcorouhlin.paper.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import land.vani.mcorouhlin.command.arguments.MultipleArgumentParser
import land.vani.mcorouhlin.paper.component.AdventureComponent
import land.vani.mcorouhlin.paper.player.getOfflinePlayerByUuidOrName
import land.vani.mcorouhlin.paper.player.getPlayerByUuidOrName
import net.kyori.adventure.extra.kotlin.text
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.concurrent.CompletableFuture

class OfflinePlayerInput(
    internal val players: List<OfflinePlayer>,
)

class OfflinePlayerArgumentType(
    private val predicate: (OfflinePlayer) -> Boolean,
    private val allowMultiplePlayers: Boolean,
) : ArgumentType<OfflinePlayerInput> {
    companion object {
        private val EXAMPLES_SINGLE = listOf("Player")
        private val EXAMPLES_MULTIPLE = listOf("Player1", "Player2")

        private val ERROR_PLAYER_NOT_FOUND = SimpleCommandExceptionType(
            AdventureComponent(
                text {
                    content("Player not found")
                }
            )
        )
        private val ERROR_PLAYER_NOT_MEET_CONDITION =
            SimpleCommandExceptionType(
                AdventureComponent(
                    text {
                        content("Player does not meet condition")
                    }
                )
            )

        fun getSinglePlayer(context: CommandContext<*>, name: String): OfflinePlayer =
            context.getArgument(name, OfflinePlayerInput::class.java).players.single()

        fun getMultiplePlayers(context: CommandContext<*>, name: String): Collection<OfflinePlayer> =
            context.getArgument(name, OfflinePlayerInput::class.java).players
    }

    override fun parse(reader: StringReader): OfflinePlayerInput {
        val start = reader.cursor

        val players = if (allowMultiplePlayers) {
            val parser = MultipleArgumentParser(reader) { input ->
                (
                    input.getPlayerByUuidOrName()
                        ?: throw ERROR_PLAYER_NOT_FOUND.createWithContext(reader)
                    )
                    .takeIf(predicate)
                    ?: run {
                        reader.cursor = start
                        throw ERROR_PLAYER_NOT_MEET_CONDITION.createWithContext(reader)
                    }
            }
            parser.parse()
            parser.parsedValues
        } else {
            val string = reader.readUnquotedString()
            val player = (
                string.getOfflinePlayerByUuidOrName()
                    ?: throw ERROR_PLAYER_NOT_FOUND.createWithContext(reader)
                )
                .takeIf(predicate)
                ?: run {
                    reader.cursor = start
                    throw ERROR_PLAYER_NOT_MEET_CONDITION.createWithContext(reader)
                }

            listOf(player)
        }

        return OfflinePlayerInput(players)
    }

    override fun getExamples(): Collection<String> = if (allowMultiplePlayers) {
        EXAMPLES_MULTIPLE
    } else {
        EXAMPLES_SINGLE
    }

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> = if (allowMultiplePlayers) {
        suggestMultiple(builder)
    } else {
        suggestSingle(builder)
    }

    private fun suggestSingle(
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val reader = StringReader(builder.input)
        reader.cursor = builder.start
        val input = reader.readUnquotedString()

        suggestPlayers(builder, input)

        return builder.buildFuture()
    }

    private fun suggestMultiple(
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val reader = StringReader(builder.input)
        reader.cursor = builder.start
        val parser = MultipleArgumentParser(reader) { input ->
            (input.getOfflinePlayerByUuidOrName() ?: throw ERROR_PLAYER_NOT_FOUND.createWithContext(reader))
                .takeIf(predicate)
                ?: throw ERROR_PLAYER_NOT_MEET_CONDITION.createWithContext(reader)
        }

        try {
            parser.parse()
        } catch (ignored: CommandSyntaxException) {
        }

        return parser.fillSuggestions(
            builder,
            getPlayerSuggestions().mapNotNull { it.name },
        )
    }

    private fun suggestPlayers(builder: SuggestionsBuilder, input: String) {
        getPlayerSuggestions().flatMap { player ->
            buildList {
                if (player.name?.startsWith(input, ignoreCase = true) == true) {
                    add(player.name)
                }
                if (player.uniqueId.toString().startsWith(input, ignoreCase = true)) {
                    add(player.uniqueId.toString())
                }
            }
        }.forEach { suggest ->
            builder.suggest(suggest)
        }
    }

    private fun getPlayerSuggestions(): List<OfflinePlayer> =
        Bukkit.getOfflinePlayers()
            .filter(predicate)
}
