package land.vani.mcorouhlin.paper.inventory

import land.vani.mcorouhlin.paper.McorouhlinPlugin
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

fun McorouhlinPlugin.inventory(
    title: Component,
    type: InventoryType = InventoryType.CHEST,
    block: McorouhlinInventory.() -> Unit = {},
): McorouhlinInventory =
    McorouhlinInventory(
        this,
        server.createInventory(null, type, title)
    ).apply(block)

fun McorouhlinPlugin.inventory(
    title: Component,
    size: Int,
    block: McorouhlinInventory.() -> Unit = {},
): McorouhlinInventory =
    McorouhlinInventory(
        this,
        server.createInventory(null, size, title)
    ).apply(block)

fun Player.openInventory(inventory: McorouhlinInventory) {
    inventory.open(this)
}
