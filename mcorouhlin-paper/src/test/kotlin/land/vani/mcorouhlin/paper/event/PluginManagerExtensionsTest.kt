package land.vani.mcorouhlin.paper.event

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.test.runTest
import land.vani.mcorouhlin.paper.TestMcorouhlinPlugin
import land.vani.mcorouhlin.paper.mockbukkit.loadSimple
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import kotlin.reflect.jvm.javaMethod

@OptIn(ExperimentalCoroutinesApi::class)
class PluginManagerExtensionsTest : DescribeSpec({
    lateinit var server: ServerMock
    lateinit var plugin: TestMcorouhlinPlugin

    beforeEach {
        server = MockBukkit.mock()
        plugin = server.pluginManager.loadSimple()
    }

    afterEach {
        MockBukkit.unmock()
    }

    it("registerSuspendEvents") {
        var executed = false
        var suspendExecuted = false

        server.pluginManager.registerSuspendEvents(
            object : Listener {
                @EventHandler
                fun onPlayerJoined(@Suppress("UNUSED_PARAMETER") event: PlayerJoinEvent) {
                    executed = true
                }

                @EventHandler
                @Suppress("RedundantSuspendModifier")
                suspend fun onPlayerJoinedSuspend(@Suppress("UNUSED_PARAMETER") event: PlayerJoinEvent) {
                    suspendExecuted = true
                }
            },
            plugin
        )
        server.pluginManager.callEvent(PlayerJoinEvent(server.addPlayer(), Component.empty()))

        executed shouldBe true
        suspendExecuted shouldBe true
    }

    it("registerSuspendEvent") {
        runTest {
            var executed = false

            server.pluginManager.registerSuspendEvent<PlayerJoinEvent>(
                object : Listener {},
                EventPriority.NORMAL,
                false,
                plugin
            ) {
                executed = true
            }
            server.pluginManager.callEvent(PlayerJoinEvent(server.addPlayer(), Component.empty()))

            executed shouldBe true
        }
    }

    it("callSuspendEvent") {
        runTest {
            var executed = false

            server.pluginManager.registerSuspendEvent<PlayerJoinEvent>(
                object : Listener {},
                EventPriority.NORMAL,
                false,
                plugin
            ) {
                executed = true
            }

            server.pluginManager.callSuspendEvent(PlayerJoinEvent(server.addPlayer(), Component.empty())).joinAll()
            executed shouldBe true
        }
    }

    it("isEventHandler") {
        Test::handler.javaMethod!!.isEventHandler shouldBe true
    }

    it("isSuspendEventHandler") {
        Test::suspendHandler.javaMethod!!.isSuspendEventHandler shouldBe true
    }
})

@Suppress("UNUSED_PARAMETER", "unused")
private class Test {
    fun handler(event: PlayerJoinEvent) {
        // empty
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun suspendHandler(event: PlayerJoinEvent) {
        // empty
    }
}
