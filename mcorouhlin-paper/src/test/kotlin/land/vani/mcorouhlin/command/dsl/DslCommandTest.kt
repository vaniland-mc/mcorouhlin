package land.vani.mcorouhlin.command.dsl

import com.mojang.brigadier.CommandDispatcher
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import land.vani.mcorouhlin.command.arguments.integer
import land.vani.mcorouhlin.command.arguments.string
import land.vani.mcorouhlin.command.optional
import land.vani.mcorouhlin.command.register

class DslCommandTest : DescribeSpec({
    it("buildLiteral") {
        val result = mutableListOf<Any?>()

        val command = command<String>("someCommand") {
            val testArg1 by string("testArg1")
            val testArg2 by integer("testArg2").optional()

            runs {
                result += testArg1
                result += testArg2
            }
        }

        val dispatcher = CommandDispatcher<String>()
        dispatcher.register(command)
        dispatcher.execute("someCommand hoge 1", "source")

        result shouldContainExactly listOf("hoge", 1)
    }
})
