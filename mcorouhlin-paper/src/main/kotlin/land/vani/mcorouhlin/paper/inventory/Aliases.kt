package land.vani.mcorouhlin.paper.inventory

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

typealias InventoryOpenHandler = (InventoryOpenEvent) -> Unit

typealias InventoryCloseHandler = (InventoryCloseEvent) -> Unit

typealias InventoryClickHandler = (InventoryClickEvent) -> Unit
