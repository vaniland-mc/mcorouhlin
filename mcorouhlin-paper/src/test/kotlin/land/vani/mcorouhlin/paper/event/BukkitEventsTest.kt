package land.vani.mcorouhlin.paper.event

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.block.BlockMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import land.vani.mcorouhlin.paper.TestMcorouhlinPlugin
import land.vani.mcorouhlin.paper.mockbukkit.assertEventFired
import land.vani.mcorouhlin.paper.mockbukkit.loadSimple
import net.kyori.adventure.text.Component
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerJoinEvent

class BukkitEventsTest : DescribeSpec({
    lateinit var server: ServerMock
    lateinit var plugin: TestMcorouhlinPlugin

    beforeEach {
        server = MockBukkit.mock()
        plugin = server.pluginManager.loadSimple()
    }

    afterEach {
        MockBukkit.unmock()
    }

    describe("events") {
        it("on") {
            val events = BukkitEvents(plugin)
            val player = server.addPlayer()
            var executed = false
            events.on<PlayerJoinEvent> {
                executed = true
            }
            executed shouldBe false

            server.pluginManager.callEvent(PlayerJoinEvent(player, Component.empty()))

            executed shouldBe true
        }

        it("cancelIf") {
            val events = BukkitEvents(plugin)
            val player = server.addPlayer()
            val block = BlockMock()
            events.cancelIf<BlockBreakEvent> {
                true
            }
            val event = BlockBreakEvent(block, player)
            server.pluginManager.callEvent(event)

            server.pluginManager.assertEventFired<BlockBreakEvent> {
                event.isCancelled && it == event
            }
        }

        it("cancelIfNot") {
            val events = BukkitEvents(plugin)
            val player = server.addPlayer()
            val block = BlockMock()
            events.cancelIfNot<BlockBreakEvent> {
                false
            }
            val event = BlockBreakEvent(block, player)
            server.pluginManager.callEvent(event)

            server.pluginManager.assertEventFired<BlockBreakEvent> {
                event.isCancelled && it == event
            }
        }
    }
})
