package land.vani.plugin.mcorouhlin.command

import org.bukkit.command.CommandSender

@McrouhlinCommandDsl
open class CommandExecutorTree
internal constructor(
    val sender: CommandSender,
    val label: String,
    private val isAllowedEffects: Boolean,
) {
    internal val children = mutableListOf<Pair<CommandExecutionContext<*>, suspend CommandExecutorTree.(Any) -> Unit>>()

    suspend fun withEffects(block: suspend CommandExecutorTree.() -> Unit) {
        if (isAllowedEffects) {
            block()
        }
    }

    fun <T> argument(context: CommandExecutionContext<T>, block: (suspend CommandExecutorTree.(T) -> Unit)) {
        @Suppress("UNCHECKED_CAST")
        children.add(context to (block as suspend CommandExecutorTree.(Any) -> Unit))
    }
}
