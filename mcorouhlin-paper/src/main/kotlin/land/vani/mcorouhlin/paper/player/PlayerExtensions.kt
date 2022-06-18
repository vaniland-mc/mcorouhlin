package land.vani.mcorouhlin.paper.player

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.permissions.Permissible
import java.util.UUID

private val UUID_REGEX =
    "^([0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12})\$".toRegex()

fun String.getPlayerByUuidOrName(): Player? = if (UUID_REGEX in this) {
    Bukkit.getPlayer(UUID.fromString(this))
} else {
    Bukkit.getPlayerExact(this)
}

fun String.getOfflinePlayerByUuidOrName(): OfflinePlayer? = if (UUID_REGEX in this) {
    Bukkit.getOfflinePlayer(UUID.fromString(this))
} else {
    Bukkit.getOfflinePlayer(this)
}

fun Permissible.hasAllPermissions(permissions: Iterable<String>): Boolean =
    permissions.all { hasPermission(it) }
