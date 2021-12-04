package land.vani.plugin.mcrouhlin.command

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import land.vani.plugin.mcorouhlin.command.CommandCancelException
import land.vani.plugin.mcorouhlin.command.command
import land.vani.plugin.mcorouhlin.command.matcher.listMatcher
import land.vani.plugin.mcrouhlin.MockCoroutinePlugin
import land.vani.plugin.mcrouhlin.util.randomString
import java.util.concurrent.atomic.AtomicBoolean

class CommandTest : ShouldSpec({
    lateinit var server: ServerMock
    lateinit var plugin: MockCoroutinePlugin

    beforeTest {
        server = MockBukkit.getOrCreateMock()
        plugin = MockBukkit.loadSimple(MockCoroutinePlugin::class.java)
    }

    afterTest {
        MockBukkit.unmock()
    }

    context("plugin#command block") {
        context("command can be created") {
            val commandName = randomString()
            val executed = AtomicBoolean(false)
            plugin.command(commandName) {
                arguments {
                    withEffects {
                        executed.set(true)
                    }
                }
            }
            val player = server.addPlayer()
            should("command is executed successfully") {
                player.performCommand(commandName) shouldBe true
                executed.get() shouldBe true
            }
        }

        context("command can be completed") {
            val commandName = randomString()
            val command = plugin.command(commandName) {
                arguments {
                    argument(listMatcher("a", "abc")) {}
                    argument(listMatcher("b", "bb")) {
                        argument(listMatcher("c", "cc")) {}
                    }
                    argument(listMatcher("あいうえお")) {}
                }
            }
            val player = server.addPlayer()

            should("command is completed correctly") {
                tabCompleteAssert(player, command, commandName) {
                    listOf("") to listOf("a", "abc", "b", "bb", "あいうえお")
                    listOf("a") to listOf("a", "abc")
                    listOf("b") to listOf("b", "bb")
                    listOf("b", "c") to listOf("c", "cc")
                    listOf("あ") to listOf("あいうえお")
                }
            }
        }

        context("inner arguments") {
            val commandName = randomString()
            val executed1 = AtomicBoolean(false)
            val executed2 = AtomicBoolean(false)

            beforeTest {
                plugin.command(commandName) {
                    arguments {
                        argument(listMatcher("a")) {
                            withEffects {
                                executed1.set(true)
                            }
                            argument(listMatcher("b")) {
                                withEffects {
                                    executed2.set(true)
                                }
                            }
                        }
                    }
                }
                executed1.set(false)
                executed2.set(false)
            }

            context("run command with arguments ['a']") {
                val player = server.addPlayer()

                should("only the withEffects block of 'a-block' will be executed.") {
                    player.performCommand("$commandName a") shouldBe true
                    executed1.get() shouldBe true
                    executed2.get() shouldBe false
                }
            }

            context("run command with arguments ['a', 'b']") {
                val player = server.addPlayer()

                should("withEffects of 'a-block' and 'b-block' works fine") {
                    player.performCommand("$commandName a b") shouldBe true
                    executed1.get() shouldBe true
                    executed2.get() shouldBe true
                }
            }
        }

        context("command can be cancelled") {
            val commandName = randomString()
            plugin.command(commandName) {
                arguments {
                    withEffects {
                        throw CommandCancelException
                    }
                }
            }
            val player = server.addPlayer()

            should("command is canceled successfully") {
                player.performCommand(commandName) shouldBe false
            }
        }
    }
})
