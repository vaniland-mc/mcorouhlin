package land.vani.mcorouhlin.paper.item

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import kotlin.reflect.KClass
import kotlin.reflect.cast

fun itemStack(material: Material, amount: Int = 1, block: ItemStack.() -> Unit = {}) =
    ItemStack(material, amount).apply(block)

fun <T : ItemMeta> ItemStack.editMeta(metaClass: KClass<T>, block: T.() -> Unit): Boolean {
    val meta = itemMeta
    if (metaClass.isInstance(meta)) {
        metaClass.cast(meta).apply(block)
        itemMeta = meta
        return true
    }
    return false
}

inline fun <reified T : ItemMeta> ItemStack.editMeta(noinline block: T.() -> Unit): Boolean = editMeta(T::class, block)
