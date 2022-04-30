package land.vani.mcorouhlin.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

class EnumArgumentType<T : Enum<T>>(
    private val clazz: KClass<T>,
    private val predicate: (T) -> Boolean,
) : ArgumentType<T> {
    private val constantsNotFoundExceptionType = SimpleCommandExceptionType {
        "Constants not found in $clazz"
    }

    override fun parse(reader: StringReader): T {
        val start = reader.cursor
        val string = reader.readUnquotedString()

        return clazz.java.enumConstants
            .find { it.name == string.uppercase() }
            ?.takeIf(predicate)
            ?: run {
                reader.cursor = start
                throw constantsNotFoundExceptionType.createWithContext(reader)
            }
    }

    fun get(context: CommandContext<*>, name: String): T =
        context.getArgument(name, clazz.java)

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        val reader = StringReader(builder.input)
        reader.cursor = builder.start
        val input = reader.readUnquotedString()

        clazz.java.enumConstants
            .filter(predicate)
            .forEach {
                if (it.name.startsWith(input, ignoreCase = true)) {
                    builder.suggest(it.name.lowercase())
                }
            }

        return builder.buildFuture()
    }
}
