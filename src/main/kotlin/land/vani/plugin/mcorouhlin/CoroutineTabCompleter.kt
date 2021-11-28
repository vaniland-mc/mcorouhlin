package land.vani.plugin.mcorouhlin

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

interface CoroutineTabCompleter {
    suspend fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>
}
