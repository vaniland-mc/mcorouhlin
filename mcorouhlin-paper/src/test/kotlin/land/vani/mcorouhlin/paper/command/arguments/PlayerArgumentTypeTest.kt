package land.vani.mcorouhlin.paper.command.arguments

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import land.vani.mcorouhlin.command.dsl.command
import land.vani.mcorouhlin.command.register
import land.vani.mockpaper.MockPaper
import land.vani.mockpaper.ServerMock
import org.bukkit.entity.Player
import java.util.UUID

class PlayerArgumentTypeTest : DescribeSpec({
    lateinit var server: ServerMock
    beforeEach {
        server = MockPaper.mock()
    }

    afterEach {
        MockPaper.unmock()
    }

    context("single player") {
        context("player name") {
            it("parse single player with invalid player name") {
                val dispatcher = CommandDispatcher<Any?>()

                val command = command<Any?>("testCommand") {
                    @Suppress("UNUSED_VARIABLE")
                    val playerArg by player("player")
                }
                dispatcher.register(command)

                val exception = shouldThrow<CommandSyntaxException> {
                    dispatcher.execute("testCommand test", null)
                }

                exception.message shouldBe "Player not found at position "
            }

            it("parse single player with valid player name") {
                val dispatcher = CommandDispatcher<Any?>()
                val player = server.addPlayer()

                var parsedPlayer: Player? = null

                val command = command<Any?>("testCommand") {
                    val playerArg by player("player")

                    runs {
                        parsedPlayer = playerArg
                    }
                }
                dispatcher.register(command)

                dispatcher.execute("testCommand ${player.name}", null)

                parsedPlayer shouldBe player
            }
        }

        context("player uuid") {
            it("parse single player with invalid player uuid") {
                val dispatcher = CommandDispatcher<Any?>()
                val command = command<Any?>("testCommand") {
                    @Suppress("UNUSED_VARIABLE")
                    val playerArg by player("player")
                }
                dispatcher.register(command)

                val exception = shouldThrow<CommandSyntaxException> {
                    dispatcher.execute("testCommand ${UUID.randomUUID()}", null)
                }

                exception.message shouldStartWith "Player not found at position "
            }

            it("parse single player with valid player uuid") {
                val dispatcher = CommandDispatcher<Any?>()
                val player = server.addPlayer()
                var parsedPlayer: Player? = null
                val command = command<Any?>("testCommand") {
                    val playerArg by player("player")

                    runs {
                        parsedPlayer = playerArg
                    }
                }
                dispatcher.register(command)
                dispatcher.execute("testCommand ${player.uniqueId}", null)

                parsedPlayer shouldBe player
            }
        }

        it("suggest") {
            val dispatcher = CommandDispatcher<Any?>()
            val player = server.addPlayer()
            val command = command<Any?>("testCommand") {
                @Suppress("UNUSED_VARIABLE")
                val playerArg by player("player")
            }
            dispatcher.register(command)

            val parsedCommand = dispatcher.parse("testCommand ", null)
            val completes = withContext(Dispatchers.IO) {
                dispatcher.getCompletionSuggestions(parsedCommand).get().list.map { it.text }
            }

            completes shouldHaveSize 2
            completes shouldContainAll listOf(player.uniqueId.toString(), player.name)
        }
    }

    context("multiple players") {
        context("player name") {
            it("parse multiple players with invalid player name") {
                val dispatcher = CommandDispatcher<Any?>()
                val command = command<Any?>("testCommand") {
                    @Suppress("UNUSED_VARIABLE")
                    val playersArg by players("players")
                }
                dispatcher.register(command)

                val exception = shouldThrow<CommandSyntaxException> {
                    dispatcher.execute("testCommand [player1,player2]", null)
                }
                exception.message shouldBe "Player not found at position 20: ...d [player1<--[HERE]"
            }

            it("parse multiple players with valid player name") {
                val dispatcher = CommandDispatcher<Any?>()
                val player1 = server.addPlayer()
                val player2 = server.addPlayer()
                var parsedPlayers: Collection<Player>? = null

                val command = command<Any?>("testCommand") {
                    val playersArg by players("players")

                    runs {
                        parsedPlayers = playersArg
                    }
                }
                dispatcher.register(command)
                dispatcher.execute("testCommand [${player1.name}, ${player2.name}]", null)

                parsedPlayers.shouldNotBeNull()
                parsedPlayers!! shouldContainAll listOf(player1, player2)
            }
        }

        context("player uuid") {
            it("parse multiple players with invalid uuid") {
                val dispatcher = CommandDispatcher<Any?>()
                val command = command<Any?>("testCommand") {
                    @Suppress("UNUSED_VARIABLE")
                    val playersArg by players("players")
                }
                dispatcher.register(command)

                val exception = shouldThrow<CommandSyntaxException> {
                    dispatcher.execute(
                        "testCommand [dddcff18-3f7b-414b-8c73-06ffe36fbb9d, ae767079-6584-495d-b93c-cc8263550d2e]",
                        null
                    )
                }
                exception.message shouldBe "Player not found at position 49: ...ffe36fbb9d<--[HERE]"
            }

            it("parse multiple players with valid uuid") {
                val dispatcher = CommandDispatcher<Any?>()
                val player1 = server.addPlayer()
                val player2 = server.addPlayer()
                var parsedPlayers: Collection<Player>? = null

                val command = command<Any?>("testCommand") {
                    val playersArg by players("players")

                    runs {
                        parsedPlayers = playersArg
                    }
                }
                dispatcher.register(command)
                dispatcher.execute("testCommand [${player1.uniqueId}, ${player2.uniqueId}]", null)

                parsedPlayers.shouldNotBeNull()
                parsedPlayers!! shouldContainAll listOf(player1, player2)
            }
        }

        it("suggest") {
            val dispatcher = CommandDispatcher<Any?>()
            val player1 = server.addPlayer()
            val player2 = server.addPlayer()
            val command = command<Any?>("testCommand") {
                @Suppress("UNUSED_VARIABLE")
                val playersArg by players("player")
            }
            dispatcher.register(command)

            val parsedCommand = dispatcher.parse("testCommand [", null)
            val completes = withContext(Dispatchers.IO) {
                dispatcher.getCompletionSuggestions(parsedCommand).get().list.map { it.text }
            }

            completes shouldHaveSize 3
            completes shouldContainAll listOf(player1.name, player2.name, "]")
        }
    }
})
