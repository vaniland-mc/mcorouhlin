package land.vani.plugin.mcorouhlin.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

fun interface CoroutineCommandExecutor {
    suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean
}
