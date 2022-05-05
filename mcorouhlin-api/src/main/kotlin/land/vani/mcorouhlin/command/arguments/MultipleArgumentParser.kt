package land.vani.mcorouhlin.command.arguments

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

class MultipleArgumentParser<T>(
    private val reader: StringReader,
    private val parser: (String) -> T,
) {
    companion object {
        private val NOT_ARRAY_ERROR =
            SimpleCommandExceptionType(LiteralMessage("Array argument is expected but actual not array"))

        private val ERROR_EXPECTED_END_OF_ARRAY =
            SimpleCommandExceptionType(LiteralMessage("Expected array end mark"))

        private val SUGGEST_NOTHING: (SuggestionsBuilder, Collection<String>) -> CompletableFuture<Suggestions> =
            { builder, _ -> builder.buildFuture() }
    }

    private var suggestions: (SuggestionsBuilder, Collection<String>) -> CompletableFuture<Suggestions> =
        SUGGEST_NOTHING

    private val _parsedValues = mutableListOf<T>()
    val parsedValues: List<T>
        get() = _parsedValues.toList()

    @Throws(CommandSyntaxException::class)
    @Suppress("TooGenericExceptionCaught", "ThrowsCount")
    fun parse() {
        if (!reader.canRead() || reader.peek() != '[') {
            throw NOT_ARRAY_ERROR.createWithContext(reader)
        }
        reader.skip() // skip "["
        suggestions = this::suggestArrayValueOrEnd
        var start = -1

        reader.skipWhitespace()

        while (true) {
            if (reader.canRead() && reader.peek() != ']') {
                reader.skipWhitespace()
                start = reader.cursor
                val string = reader.readString()

                try {
                    _parsedValues += parser(string)
                } catch (ex: Exception) {
                    reader.cursor = start
                    throw ex
                }

                reader.skipWhitespace()
                if (!reader.canRead()) {
                    continue
                }

                start = -1
                if (reader.peek() == ',') {
                    reader.skip() // skip ","
                    println("suggestArrayValue")
                    suggestions = this::suggestArrayValue
                    continue
                }

                if (reader.peek() != ']') {
                    throw ERROR_EXPECTED_END_OF_ARRAY.createWithContext(reader)
                }
            }

            if (reader.canRead()) {
                reader.skip() // skip "]"
                return
            }

            if (start >= 0) {
                reader.cursor = start
            }

            throw ERROR_EXPECTED_END_OF_ARRAY.createWithContext(reader)
        }
    }

    private fun suggestArrayValueOrEnd(
        builder: SuggestionsBuilder,
        values: Collection<String>,
    ): CompletableFuture<Suggestions> {
        if (builder.remaining.isEmpty()) {
            builder.suggest("]")
        }

        return suggestArrayValue(builder, values)
    }

    private fun suggestArrayValue(
        builder: SuggestionsBuilder,
        values: Collection<String>,
    ): CompletableFuture<Suggestions> {
        val s = builder.remaining

        values.filter { it.startsWith(s, ignoreCase = true) }
            .forEach { builder.suggest(it) }

        return builder.buildFuture()
    }

    fun fillSuggestions(builder: SuggestionsBuilder, values: List<String>): CompletableFuture<Suggestions> =
        suggestions(builder.createOffset(reader.cursor), values)
}
