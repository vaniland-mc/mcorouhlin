package land.vani.plugin.mcorouhlin.command

import kotlinx.coroutines.runBlocking
import land.vani.plugin.mcorouhlin.CoroutinePlugin
import land.vani.plugin.mcorouhlin.command.internal.CommandExecutor
import land.vani.plugin.mcorouhlin.permission.Permission
import org.bukkit.command.Command
import org.bukkit.command.CommandException
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand

@Suppress("unused")
fun PluginCommand.setCoroutineCommandExecutor(executor: CoroutineCommandExecutor) {
    setExecutor { sender, command, label, args ->
        runBlocking {
            executor.onCommand(sender, command, label, args)
        }
    }
}

@Suppress("unused")
fun PluginCommand.setCoroutineTabCompleter(completer: CoroutineTabCompleter) {
    setTabCompleter { sender, command, alias, args ->
        runBlocking {
            completer.onTabComplete(sender, command, alias, args)
        }
    }
}

fun CoroutinePlugin.command(label: String, block: CommandCreator.() -> Unit): Command {
    val command = CommandCreator(this, label).apply(block).build()
    try {
        val commandMapField = server.javaClass.getDeclaredField("commandMap").apply {
            isAccessible = true
        }
        val commandMap = commandMapField[server] as CommandMap
        commandMap.register(this.name, command)
        commandMapField.isAccessible = false
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return command
}

@McrouhlinCommandDsl
class CommandCreator internal constructor(
    private val plugin: CoroutinePlugin,
    val label: String,
) {
    var description: String = ""
    var usageMessage: String = "/"
    var aliases: List<String> = listOf()
    var permission: Permission? = null
    var permissionMessage: String? = null

    private var argumentsHandler: suspend CommandExecutorTree.() -> Unit = {}
    fun arguments(block: suspend (CommandExecutorTree.() -> Unit)) {
        argumentsHandler = block
    }

    internal fun build(): Command = object : Command(
        label, description, usageMessage, aliases
    ) {
        init {
            permission = this@CommandCreator.permission?.node
            permissionMessage = this@CommandCreator.permissionMessage
        }

        override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
            if (!plugin.isEnabled) {
                throw CommandException("Cannot execute command '$commandLabel' in plugin ${plugin.description.fullName} - plugin is disabled")
            }

            if (!testPermission(sender)) {
                return true
            }

            val success = try {
                runBlocking {
                    CommandExecutor(sender, label, args).apply {
                        this@CommandCreator.argumentsHandler(this@apply)
                    }.execute()
                }
                true
            } catch (ex: CommandCancelException) {
                false
            } catch (ex: Throwable) {
                throw CommandException("Unhandled exception executing command '$commandLabel' in plugin ${plugin.description.fullName}",
                    ex)
            }

            if (!success && usageMessage.isNotEmpty()) {
                for (line in usageMessage.replace("<command>", commandLabel).split("\n")) {
                    sender.sendMessage(line)
                }
            }

            return success
        }

        override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> =
            runBlocking {
                CommandExecutor(sender, alias, args).apply {
                    this@CommandCreator.argumentsHandler(this)
                }.complete()
            }
    }
}
