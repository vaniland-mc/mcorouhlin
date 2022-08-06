package land.vani.mcorouhlin.paper

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.block.BlockMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import land.vani.mcorouhlin.paper.event.cancelIf
import land.vani.mcorouhlin.paper.event.cancelIfNot
import land.vani.mcorouhlin.paper.event.on
import land.vani.mcorouhlin.paper.mockbukkit.assertEventFired
import land.vani.mcorouhlin.paper.mockbukkit.loadSimple
import land.vani.mcorouhlin.paper.permission.asBukkit
import land.vani.mcorouhlin.permission.Permission
import land.vani.mcorouhlin.permission.PermissionDefault
import land.vani.mcorouhlin.permission.registerPermissions
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File

private class McorouhlinPluginImpl(
    loader: JavaPluginLoader,
    description: PluginDescriptionFile,
    dataFolder: File,
    file: File,
    @Suppress("UNUSED_PARAMETER")
    parameters: Array<Any>,
) : McorouhlinKotlinPlugin(loader, description, dataFolder, file) {
    var onEnableAsyncExecuted = false
    var onDisableAsyncExecuted = false

    override suspend fun onEnableAsync() {
        onEnableAsyncExecuted = true
    }

    override suspend fun onDisableAsync() {
        onDisableAsyncExecuted = true
    }
}

private enum class TestPermissions(
    override val node: String,
    override val description: String?,
    override val default: PermissionDefault?,
    override val children: Map<Permission, Boolean>,
) : Permission {
    TEST("mcorouhlin.test", "test permission", PermissionDefault.OP, mapOf()),
    ;
}

@OptIn(ExperimentalCoroutinesApi::class)
class McorouhlinKotlinPluginTest : DescribeSpec({
    lateinit var server: ServerMock
    lateinit var plugin: McorouhlinPluginImpl

    beforeEach {
        server = MockBukkit.mock()
        plugin = server.pluginManager.loadSimple()
    }

    afterEach {
        MockBukkit.unmock()
    }

    it("asyncDispatcher") {
        runTest {
            withContext(plugin.asyncDispatcher) {
                server.isPrimaryThread shouldBe false
            }
        }
    }

    it("mainThreadDispatcher") {
        runTest {
            withContext(plugin.mainThreadDispatcher) {
                server.isPrimaryThread shouldBe true
            }
        }
    }

    it("onEnableAsync") {
        plugin.onEnableAsyncExecuted shouldBe true
    }

    it("onDisableAsync") {
        server.pluginManager.disablePlugin(plugin)

        plugin.onDisableAsyncExecuted shouldBe true
    }

    it("registerPermissions") {
        plugin.registerPermissions<TestPermissions>()
        val permission = server.pluginManager.getPermission(TestPermissions.TEST.node)

        permission.shouldNotBeNull()
        permission.name shouldBe TestPermissions.TEST.node
        permission.description shouldBe TestPermissions.TEST.description
        permission.default shouldBe TestPermissions.TEST.default?.asBukkit
        permission.children shouldBe TestPermissions.TEST.children.mapKeys { (key, _) -> key.node }
    }

    describe("events") {
        it("on") {
            LegacyComponentSerializer.legacySection()
            val player = server.addPlayer()
            var executed = false
            plugin.events {
                on<PlayerJoinEvent> {
                    executed = true
                }
            }
            executed shouldBe false

            server.pluginManager.callEvent(PlayerJoinEvent(player, Component.empty()))

            executed shouldBe true
        }

        it("cancelIf") {
            val player = server.addPlayer()
            val block = BlockMock()
            plugin.events {
                cancelIf<BlockBreakEvent> {
                    true
                }
            }
            val event = BlockBreakEvent(block, player)
            server.pluginManager.callEvent(event)

            server.pluginManager.assertEventFired<BlockBreakEvent> {
                event.isCancelled && it == event
            }
        }

        it("cancelIfNot") {
            val player = server.addPlayer()
            val block = BlockMock()
            plugin.events {
                cancelIfNot<BlockBreakEvent> {
                    false
                }
            }
            val event = BlockBreakEvent(block, player)
            server.pluginManager.callEvent(event)

            server.pluginManager.assertEventFired<BlockBreakEvent> {
                event.isCancelled && it == event
            }
        }
    }
})
