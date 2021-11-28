package land.vani.plugin.mcorouhlin.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

fun interface CoroutineTabCompleter {
    suspend fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String>
}
