package land.vani.mcorouhlin.command.argument

import com.mojang.brigadier.CommandDispatcher
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import land.vani.mcorouhlin.command.arguments.duration
import land.vani.mcorouhlin.command.dsl.command
import land.vani.mcorouhlin.command.register
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Suppress("RemoveExplicitTypeArguments")
class DurationArgumentTypeTest : DescribeSpec({
    it("parse") {
        var duration: Duration? = null
        val command = command<Any?>("testCommand") {
            val durationArg by duration("duration")

            runs {
                duration = durationArg
            }
        }
        val dispatcher = CommandDispatcher<Any?>()
        dispatcher.register(command)

        dispatcher.execute("testCommand 1w4d2h", null)

        duration shouldBe (1 * 7).days + 4.days + 2.hours
    }

    xit("suggest") {
        val command = command<Any?>("testCommand") {
            @Suppress("UNUSED_VARIABLE")
            val durationArg by duration("duration")
        }
        val dispatcher = CommandDispatcher<Any?>()
        dispatcher.register(command)

        val parsed = dispatcher.parse("testCommand 10y2", null)
        val completion = withContext(Dispatchers.IO) {
            dispatcher.getCompletionSuggestions(parsed).get()
        }

        completion.list.map { it.text } shouldContainExactly listOf()
    }
})
