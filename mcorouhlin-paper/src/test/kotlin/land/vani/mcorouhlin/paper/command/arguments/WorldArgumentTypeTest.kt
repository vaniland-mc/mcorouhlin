package land.vani.mcorouhlin.paper.command.arguments

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import land.vani.mcorouhlin.command.dsl.command
import land.vani.mcorouhlin.command.register
import land.vani.mockpaper.MockPaper
import land.vani.mockpaper.ServerMock
import org.bukkit.World

class WorldArgumentTypeTest : DescribeSpec({
    lateinit var server: ServerMock
    beforeEach {
        server = MockPaper.mock()
    }

    afterEach {
        MockPaper.unmock()
    }

    context("single world") {
        it("invalid world name") {
            val dispatcher = CommandDispatcher<Any?>()

            val command = command<Any?>("testCommand") {
                @Suppress("UNUSED_VARIABLE")
                val worldArg by world("world")
            }
            dispatcher.register(command)

            val exception = shouldThrow<CommandSyntaxException> {
                dispatcher.execute("testCommand test", null)
            }

            exception.message shouldBe "World not found at position 16: ...mmand test<--[HERE]"
        }

        it("valid world name") {
            val dispatcher = CommandDispatcher<Any?>()
            var parsedWorld: World? = null

            val world = server.addSimpleWorld("testWorld")

            val command = command<Any?>("testCommand") {
                val worldArg by world("world")

                runs {
                    parsedWorld = worldArg
                }
            }
            dispatcher.register(command)
            dispatcher.execute("testCommand ${world.name}", null)

            parsedWorld shouldBe world
        }
    }

    context("multiple worlds") {
        it("invalid world names") {
            val dispatcher = CommandDispatcher<Any?>()

            val command = command<Any?>("testCommand") {
                @Suppress("UNUSED_VARIABLE")
                val worldsArgument by worlds("worlds")
            }
            dispatcher.register(command)

            val exception = shouldThrow<CommandSyntaxException> {
                dispatcher.execute("testCommand [test1, test2]", null)
            }
            exception.message shouldBe "World not found at position 18: ...and [test1<--[HERE]"
        }

        it("valid world names") {
            val dispatcher = CommandDispatcher<Any?>()
            val world1 = server.addSimpleWorld("world1")
            val world2 = server.addSimpleWorld("world2")

            var parsedWorlds: Collection<World>? = null

            val command = command<Any?>("testCommand") {
                val worldArgument by worlds("worlds")

                runs {
                    parsedWorlds = worldArgument
                }
            }
            dispatcher.register(command)
            dispatcher.execute("testCommand [${world1.name}, ${world2.name}]", null)

            parsedWorlds.shouldNotBeNull()
            parsedWorlds!! shouldContainAll listOf(world1, world2)
        }
    }
})
