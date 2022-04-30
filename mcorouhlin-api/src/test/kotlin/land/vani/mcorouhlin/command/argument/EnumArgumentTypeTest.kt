package land.vani.mcorouhlin.command.argument

import com.mojang.brigadier.CommandDispatcher
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import land.vani.mcorouhlin.command.arguments.enum
import land.vani.mcorouhlin.command.dsl.command
import land.vani.mcorouhlin.command.register

@Suppress("unused")
private enum class TestEnum {
    TEST1,
    TEST2,
    ;
}

class EnumArgumentTypeTest : DescribeSpec({
    it("parse") {
        val dispatcher = CommandDispatcher<Any?>()
        var parsedEnum: TestEnum? = null

        val command = command<Any?>("testCommand") {
            val enumArg by enum("enum", TestEnum::class)

            runs {
                parsedEnum = enumArg
            }
        }
        dispatcher.register(command)

        dispatcher.execute("testCommand test1", null)
        parsedEnum.shouldNotBeNull()
        parsedEnum shouldBe TestEnum.TEST1
    }

    it("suggest") {
        val dispatcher = CommandDispatcher<Any?>()

        @Suppress("UNUSED_VARIABLE")
        val command = command<Any?>("testCommand") {
            val enumArg by enum("enum", TestEnum::class)
        }
        dispatcher.register(command)

        val parsed = dispatcher.parse("testCommand test", null)
        val completions = withContext(Dispatchers.IO) {
            dispatcher.getCompletionSuggestions(parsed).get()
        }

        completions.list.map { it.text } shouldContainExactly listOf(
            "test1",
            "test2",
        )
    }
})
