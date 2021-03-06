package land.vani.mcorouhlin.paper.inventory

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import land.vani.mcorouhlin.paper.TestMcorouhlinPlugin
import land.vani.mcorouhlin.paper.item.itemStack
import land.vani.mcorouhlin.paper.mockbukkit.loadSimple
import net.kyori.adventure.text.Component
import org.bukkit.Material

class McorouhlinInventoryTest : DescribeSpec({
    lateinit var server: ServerMock
    lateinit var plugin: TestMcorouhlinPlugin

    beforeEach {
        server = MockBukkit.mock()
        plugin = server.pluginManager.loadSimple()
    }

    afterEach {
        MockBukkit.unmock()
    }

    xit("default") {
        val inventory = plugin.inventory(Component.text("some inventory")) {
            default(itemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE))
        }

        inventory.bukkitInventory.contents.forAll {
            it.shouldNotBeNull()
            it.type shouldBe Material.LIGHT_GRAY_STAINED_GLASS_PANE
            it.amount = 1
        }
    }

    // TODO: Currently MockPaper does not fire InventoryOpenEvent when player opening an inventory.
    xit("onOpen") {
        var executed = false
        val inventory = plugin.inventory(Component.text("some inventory")) {
            onOpen {
                executed = true
            }
        }
        val player = server.addPlayer()
        player.openInventory(inventory)

        executed shouldBe true
    }

    xit("onClose") {
        var executed = false
        val inventory = plugin.inventory(Component.text("some inventory")) {
            onClose {
                executed = true
            }
        }
        val player = server.addPlayer()
        player.openInventory(inventory)
        player.closeInventory()

        executed shouldBe true
    }

    it("onPreClick") {
        // TODO: how to test this?
    }

    it("onPostClick") {
        // TODO: how to test this?
    }

    xit("slot") {
        val inventory = plugin.inventory(Component.text("some inventory")) {
            slot(0, itemStack(Material.DIRT))
        }
        inventory.bukkitInventory.getItem(0) shouldBe itemStack(Material.DIRT)
    }

    xit("slots") {
        val inventory = plugin.inventory(Component.text("some inventory")) {
            slots(0..10, itemStack(Material.DIRT))
        }
        inventory.bukkitInventory.contents.copyOfRange(0, 10).forAll {
            it shouldBe itemStack(Material.DIRT)
        }
    }
})
