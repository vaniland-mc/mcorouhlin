@file:Suppress("unused")

package land.vani.mcorouhlin.paper.command.arguments

import land.vani.mcorouhlin.command.RequiredArgument
import land.vani.mcorouhlin.command.arguments.argument
import land.vani.mcorouhlin.command.arguments.argumentImplied
import land.vani.mcorouhlin.command.arguments.enum
import land.vani.mcorouhlin.command.arguments.impliedGetter
import land.vani.mcorouhlin.command.dsl.DslCommandBuilder
import land.vani.mcorouhlin.paper.player.hasAllPermissions
import net.minecraft.commands.arguments.UuidArgument
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.UUID

fun <S> DslCommandBuilder<S>.entityType(
    name: String,
    predicate: (EntityType) -> Boolean = { true },
): RequiredArgument<S, EntityType, EntityType> = enum(name, predicate)

fun <S> DslCommandBuilder<S>.material(
    name: String,
    predicate: (Material) -> Boolean = { true },
): RequiredArgument<S, Material, Material> = enum(name, predicate)

fun <S> DslCommandBuilder<S>.uuid(name: String): RequiredArgument<S, UUID, UUID> =
    argumentImplied(name, UuidArgument.uuid(), impliedGetter())

fun <S> DslCommandBuilder<S>.player(
    name: String,
    permissions: List<String>,
): RequiredArgument<S, PlayerInput, Player> =
    player(name) { it.hasAllPermissions(permissions) }

fun <S> DslCommandBuilder<S>.player(
    name: String,
    predicate: (Player) -> Boolean = { true },
): RequiredArgument<S, PlayerInput, Player> =
    argument(name, PlayerArgumentType(predicate, false), PlayerArgumentType.Companion::getSinglePlayer)

fun <S> DslCommandBuilder<S>.players(
    name: String,
    permissions: List<String>,
): RequiredArgument<S, PlayerInput, Collection<Player>> =
    players(name) { it.hasAllPermissions(permissions) }

fun <S> DslCommandBuilder<S>.players(
    name: String,
    predicate: (Player) -> Boolean = { true },
): RequiredArgument<S, PlayerInput, Collection<Player>> =
    argument(name, PlayerArgumentType(predicate, true), PlayerArgumentType.Companion::getMultiplePlayers)

fun <S> DslCommandBuilder<S>.offlinePlayer(
    name: String,
    predicate: (OfflinePlayer) -> Boolean = { true },
): RequiredArgument<S, OfflinePlayerInput, OfflinePlayer> =
    argument(
        name,
        OfflinePlayerArgumentType(predicate, false),
        PlayerArgumentType.Companion::getSinglePlayer
    )

fun <S> DslCommandBuilder<S>.offlinePlayers(
    name: String,
    predicate: (OfflinePlayer) -> Boolean = { true },
): RequiredArgument<S, OfflinePlayerInput, Collection<OfflinePlayer>> =
    argument(
        name,
        OfflinePlayerArgumentType(predicate, true),
        PlayerArgumentType.Companion::getMultiplePlayers,
    )

fun <S> DslCommandBuilder<S>.world(
    name: String,
    predicate: (World) -> Boolean = { true },
): RequiredArgument<S, WorldInput, World> =
    argument(name, WorldArgumentType(predicate, false), WorldArgumentType.Companion::getSingleWorld)

fun <S> DslCommandBuilder<S>.worlds(
    name: String,
    predicate: (World) -> Boolean = { true },
): RequiredArgument<S, WorldInput, Collection<World>> =
    argument(name, WorldArgumentType(predicate, true), WorldArgumentType.Companion::getMultipleWorlds)
