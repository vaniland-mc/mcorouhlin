package land.vani.plugin.mcorouhlin.command.internal

import land.vani.plugin.mcorouhlin.command.CommandExecutorTree
import org.bukkit.command.CommandSender

internal class CommandExecutor(
    sender: CommandSender,
    label: String,
    args: Array<out String>,
) : CommandExecutorTree(sender, label, true) {
    private val args = args.map { it.lowercase() }

    suspend fun complete(): List<String> {
        var tree: CommandExecutorTree = CommandExecutorTree(DummyCommandSender, label, false).also {
            it.children.addAll(children)
        }

        args.dropLast(1).forEach { arg ->
            val (context, action) = tree.children.firstOrNull { (context, _) ->
                context.candidates().map { it.lowercase() }
                    .any { it.startsWith(arg) }
            } ?: return emptyList()

            tree = CommandExecutorTree(DummyCommandSender, label, false).apply {
                context.parse(arg)?.let { action(it) } ?: return emptyList()
            }
        }

        val lastArg = args.last()
        return buildList {
            tree.children.forEach { (context, _) ->
                context.candidates()
                    .map { it.lowercase() }
                    .filter { it.startsWith(lastArg) }
                    .let { addAll(it) }
            }
        }
    }

    suspend fun execute() {
        var tree: CommandExecutorTree = this
        args.map { it.lowercase() }
            .forEach { arg ->
                val (context, action) = tree.children.firstOrNull { (context, _) ->
                    context.parse(arg) != null
                } ?: return
                val parsed = context.parse(arg) ?: return
                tree = CommandExecutorTree(sender, label, true).apply {
                    action(parsed)
                }
            }
    }
}
