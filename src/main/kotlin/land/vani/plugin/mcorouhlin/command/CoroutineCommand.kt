package land.vani.plugin.mcorouhlin.command

import kotlinx.coroutines.runBlocking
import land.vani.plugin.mcorouhlin.CoroutinePlugin
import org.bukkit.command.PluginCommand

fun PluginCommand.setCoroutineCommandExecutor(executor: CoroutineCommandExecutor) {
    require(plugin is CoroutinePlugin) { "setCoroutineCommandExecutor is only supported on CoroutinePlugin" }
    setExecutor { sender, command, label, args ->
        runBlocking((plugin as CoroutinePlugin).mainThreadDispatcher) {
            executor.onCommand(sender, command, label, args)
        }
    }
}

fun PluginCommand.setCoroutineTabCompleter(completer: CoroutineTabCompleter) {
    require(plugin is CoroutinePlugin) { "setCoroutineTabCompleter is only supported on CoroutinePlugin" }
    setTabCompleter { sender, command, alias, args ->
        runBlocking((plugin as CoroutinePlugin).mainThreadDispatcher) {
            completer.onTabComplete(sender, command, alias, args)
        }
    }
}
