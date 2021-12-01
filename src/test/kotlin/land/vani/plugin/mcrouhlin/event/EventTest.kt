package land.vani.plugin.mcrouhlin.event

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldNotBe
import land.vani.plugin.mcorouhlin.event.events
import land.vani.plugin.mcorouhlin.event.registerCoroutineEvents
import land.vani.plugin.mcrouhlin.MockCoroutinePlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

@Suppress("BlockingMethodInNonBlockingContext")
class EventTest : ShouldSpec({
    lateinit var server: ServerMock
    lateinit var plugin: MockCoroutinePlugin

    beforeTest {
        server = MockBukkit.getOrCreateMock()
        plugin = MockBukkit.loadSimple(MockCoroutinePlugin::class.java)
    }

    context("plugin#events block") {
        var event: PlayerJoinEvent? = null
        plugin.events {
            on<PlayerJoinEvent> {
                event = it
            }
        }
        server.addPlayer()

        should("passed event to event handler method") {
            server.pluginManager.assertEventFired(PlayerJoinEvent::class.java)
            event shouldNotBe null
        }
    }

    context("pluginManager#registerCoroutineEvents with suspend method") {
        var event: PlayerJoinEvent? = null

        plugin.server.pluginManager.registerCoroutineEvents(
            object : Listener {
                @Suppress("RedundantSuspendModifier")
                @EventHandler
                suspend fun onPlayerJoin(e: PlayerJoinEvent) {
                    event = e
                }
            },
            plugin
        )
        server.addPlayer()

        should("passed event to event handler method") {
            server.pluginManager.assertEventFired(PlayerJoinEvent::class.java)
            event shouldNotBe null
        }
    }

    context("pluginManager#registerCoroutineEvents with non-suspend method") {
        var event: PlayerJoinEvent? = null

        plugin.server.pluginManager.registerCoroutineEvents(
            object : Listener {
                @EventHandler
                fun onPlayerJoin(e: PlayerJoinEvent) {
                    event = e
                }
            },
            plugin
        )
        server.addPlayer()

        should("passed event to event handler method") {
            server.pluginManager.assertEventFired(PlayerJoinEvent::class.java)
            event shouldNotBe null
        }
    }
})
