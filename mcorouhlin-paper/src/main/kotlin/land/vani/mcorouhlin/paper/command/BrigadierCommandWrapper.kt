package land.vani.mcorouhlin.paper.command

import com.google.common.cache.Cache
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.exceptions.CommandSyntaxException
import land.vani.mcorouhlin.paper.McorouhlinPlugin
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginIdentifiableCommand
import org.bukkit.plugin.Plugin

class BrigadierCommandWrapper(
    private val literal: String,
    private val plugin: McorouhlinPlugin,
    private val commandDispatcher: CommandDispatcher<CommandSender>,
    private val commandCache: Cache<Pair<String, CommandSender>, ParseResults<CommandSender>>,
) : Command(literal), PluginIdentifiableCommand {
    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        val joinedCommand = joinCommand(literal, args)
        val parsedCommand = parsedCommand(joinedCommand, sender)

        try {
            commandDispatcher.execute(parsedCommand)
        } catch (ex: CommandSyntaxException) {
            sender.sendMessage(
                text {
                    content(ex.message ?: "An error occurred in parsing command")
                    color(NamedTextColor.RED)
                }
            )
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
        val joinedCommand = joinCommand(alias, args)
        val parsedCommand = parsedCommand(joinedCommand, sender)

        try {
            return commandDispatcher.getCompletionSuggestions(
                parsedCommand,
                joinedCommand.length
            )
                .get().list
                .map { it.text }
        } catch (ex: CommandSyntaxException) {
            sender.sendMessage(
                text {
                    content(ex.message ?: "An error occurred in parsing command")
                    color(NamedTextColor.RED)
                }
            )
        }

        return listOf()
    }

    override fun getPlugin(): Plugin = plugin

    private fun joinCommand(label: String, args: Array<out String>): String =
        label + args.joinToString(" ", " ")

    private fun parsedCommand(command: String, sender: CommandSender): ParseResults<CommandSender> =
        commandCache.getIfPresent(command to sender)
            ?: commandDispatcher.parse(command, sender).also {
                commandCache.put(command to sender, it)
            }
}
