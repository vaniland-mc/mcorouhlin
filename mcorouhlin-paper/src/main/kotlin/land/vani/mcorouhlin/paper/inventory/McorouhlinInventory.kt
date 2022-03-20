package land.vani.mcorouhlin.paper.inventory

import land.vani.mcorouhlin.paper.McorouhlinPlugin
import land.vani.mcorouhlin.paper.event.on
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.VisibleForTesting

/**
 * DSL class to describe inventory more clearly
 */
class McorouhlinInventory internal constructor(
    private val plugin: McorouhlinPlugin,
    @get:VisibleForTesting
    internal val bukkitInventory: Inventory,
) {
    private var onOpen: InventoryOpenHandler? = null
    private var onClose: InventoryCloseHandler? = null
    private var onPreClick: InventoryClickHandler? = { it.isCancelled = true }
    private var onPostClick: InventoryClickHandler? = null

    private var slotHandlers: MutableMap<Int, InventoryClickHandler> = mutableMapOf()

    init {
        registerListeners()
    }

    /**
     * Set default items on this inventory slots.
     */
    fun default(item: (slot: Int) -> ItemStack) {
        for (i in bukkitInventory.contents.orEmpty().indices) {
            bukkitInventory.contents?.set(i, item(i))
        }
    }

    fun default(item: ItemStack) = default { item }

    /**
     * Action performed when the inventory is opened.
     */
    fun onOpen(action: InventoryOpenHandler?) {
        onOpen = action
    }

    /**
     * Action performed when the inventory is closed.
     */
    fun onClose(action: InventoryCloseHandler?) {
        onClose = action
    }

    /**
     * Action performed pre the slot handler, when the inventory is clicked.
     */
    fun onPreClick(action: InventoryClickHandler?) {
        onPreClick = action
    }

    /**
     * Action performed post the slot handler, when the inventory is clicked.
     */
    fun onPostClick(action: InventoryClickHandler?) {
        onPostClick = action
    }

    /**
     * Set [item] to [index] in this inventory.
     *
     * The [handler] is performed when this slot is clicked.
     */
    fun slot(index: Int, item: ItemStack, handler: InventoryClickHandler? = null) {
        bukkitInventory.setItem(index, item)
        if (handler != null) {
            slotHandlers[index] = handler
        } else {
            slotHandlers -= index
        }
    }

    fun slot(index: Int, itemBuilder: (Int) -> ItemStack, handler: InventoryClickHandler? = null) {
        slot(index, itemBuilder(index), handler)
    }

    /**
     * Set [item] to [indices] in this inventory.
     *
     * The [handler] is performed when this slot is clicked.
     */
    fun slots(indices: Iterable<Int>, item: ItemStack, handler: InventoryClickHandler? = null) {
        indices.forEach { slot(it, item, handler) }
    }

    fun slots(index: Iterable<Int>, itemBuilder: (Int) -> ItemStack, handler: InventoryClickHandler? = null) {
        index.forEach { slot(it, itemBuilder(it), handler) }
    }

    internal fun open(player: Player) {
        player.openInventory(bukkitInventory)
    }

    private fun registerListeners() = plugin.events {
        on<InventoryClickEvent> { event ->
            if (event.inventory != bukkitInventory) return@on

            onPreClick?.invoke(event)
            slotHandlers[event.slot]?.invoke(event)
            onPostClick?.invoke(event)
        }

        on<InventoryOpenEvent> { event ->
            if (event.inventory != bukkitInventory) return@on

            onOpen?.invoke(event)
        }

        on<InventoryCloseEvent> { event ->
            if (event.inventory != bukkitInventory) return@on

            onClose?.invoke(event)
        }
    }
}
