package land.vani.plugin.mcorouhlin.command.matcher

import land.vani.plugin.mcorouhlin.command.CommandExecutionContext
import land.vani.plugin.mcorouhlin.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

private object OnlinePlayerMatcher : CommandExecutionContext<Player> {
    override fun candidates(): List<String> = Bukkit.getOnlinePlayers().map { it.name }
    override fun parse(raw: String): Player? = Bukkit.getPlayer(raw)
}

@Suppress("unused")
fun playerMatcher(): CommandExecutionContext<Player> = OnlinePlayerMatcher

private object OfflinePlayerMatcher : CommandExecutionContext<OfflinePlayer> {
    override fun candidates(): List<String> = Bukkit.getOfflinePlayers().mapNotNull { it.name }

    @Suppress("DEPRECATION")
    override fun parse(raw: String): OfflinePlayer = Bukkit.getOfflinePlayer(raw)
}

@Suppress("unused")
fun offlinePlayerMatcher(): CommandExecutionContext<OfflinePlayer> = OfflinePlayerMatcher

@JvmName("playerWithPermissionMatcherString")
fun playerWithPermissionMatcher(permissions: Iterable<String>) =
    object : CommandExecutionContext<Player> {
        override fun candidates(): List<String> = Bukkit.getOnlinePlayers().filter { player ->
            permissions.all { player.hasPermission(it) }
        }.map { it.name }

        override fun parse(raw: String): Player? = Bukkit.getPlayer(raw)?.takeIf { player ->
            permissions.all { player.hasPermission(it) }
        }
    }

@Suppress("unused")
fun playerWithPermissionMatcher(vararg permissions: String) = playerWithPermissionMatcher(permissions.toList())

@JvmName("playerWithPermissionMatcherPermission")
fun playerWithPermissionMatcher(permissions: Iterable<Permission>) =
    playerWithPermissionMatcher(permissions.map { it.node })

@Suppress("unused")
fun playerWithPermissionMatcher(vararg permissions: Permission) = playerWithPermissionMatcher(permissions.toList())
