package land.vani.mcorouhlin.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object DurationArgumentType : ArgumentType<Duration> {
    private val DURATION_REGEX =
        """^(?:(\d+)y)?(?:(\d+)M)?(?:(\d+)w)?(?:(\d+)d)?(?:(\d+)h)?(?:(\d+)m)?(?:(\d+)s)?$""".toRegex()

    private val INVALID_DURATION_FORMAT = SimpleCommandExceptionType { "Invalid duration format" }

    private const val SECONDS_PER_YEAR = 31556952L
    private const val SECONDS_PER_MONTH = SECONDS_PER_YEAR / 12

    private val EXAMPLES = listOf("1y2M3w4d5h6m7s")

    @Suppress("MagicNumber")
    override fun parse(reader: StringReader): Duration {
        val start = reader.cursor
        val input = reader.readUnquotedString()

        var result = Duration.ZERO

        val matchResult = DURATION_REGEX.matchEntire(input)
            ?: run {
                reader.cursor = start
                throw INVALID_DURATION_FORMAT.createWithContext(reader)
            }
        val destructured = matchResult.destructured.toList()

        val years = destructured[0].toLongOrNull() ?: 0
        result += (years * SECONDS_PER_YEAR).toDuration(DurationUnit.SECONDS)

        val months = destructured[1].toLongOrNull() ?: 0
        result += (months * SECONDS_PER_MONTH).toDuration(DurationUnit.SECONDS)

        val weeks = destructured[2].toLongOrNull() ?: 0
        result += (weeks * 7).toDuration(DurationUnit.DAYS)

        val days = destructured[3].toLongOrNull() ?: 0
        result += days.toDuration(DurationUnit.DAYS)

        val hours = destructured[4].toLongOrNull() ?: 0
        result += hours.toDuration(DurationUnit.HOURS)

        val minutes = destructured[5].toLongOrNull() ?: 0
        result += minutes.toDuration(DurationUnit.MINUTES)

        val seconds = destructured[6].toLongOrNull() ?: 0
        result += seconds.toDuration(DurationUnit.SECONDS)

        return result
    }

    override fun getExamples(): Collection<String> = EXAMPLES

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder,
    ): CompletableFuture<Suggestions> {
        // TODO: Implementing suggestions is difficult and can be done later.

        return Suggestions.empty()
    }
}
