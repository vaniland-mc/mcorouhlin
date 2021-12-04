package land.vani.plugin.mcorouhlin.command

import land.vani.plugin.mcorouhlin.command.matcher.blockMatcher
import land.vani.plugin.mcorouhlin.command.matcher.boolMatcher
import land.vani.plugin.mcorouhlin.command.matcher.entityMatcher
import land.vani.plugin.mcorouhlin.command.matcher.enumMatcher
import land.vani.plugin.mcorouhlin.command.matcher.materialMatcher
import land.vani.plugin.mcorouhlin.command.matcher.offlinePlayerMatcher
import land.vani.plugin.mcorouhlin.command.matcher.playerMatcher
import land.vani.plugin.mcorouhlin.command.matcher.playerWithPermissionMatcher
import land.vani.plugin.mcorouhlin.command.matcher.stringMatcher
import land.vani.plugin.mcorouhlin.permission.Permission
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

@Suppress("unused")
@McrouhlinCommandDsl
open class CommandExecutorTree
internal constructor(
    val sender: CommandSender,
    val label: String,
    private val isAllowedEffects: Boolean,
) {
    internal val children = mutableListOf<Pair<CommandExecutionContext<*>, suspend CommandExecutorTree.(Any) -> Unit>>()

    suspend fun withEffects(block: suspend CommandExecutorTree.() -> Unit) {
        if (isAllowedEffects) {
            block()
        }
    }

    fun <T> argument(context: CommandExecutionContext<T>, block: (suspend CommandExecutorTree.(T) -> Unit)) {
        @Suppress("UNCHECKED_CAST")
        children.add(context to (block as suspend CommandExecutorTree.(Any) -> Unit))
    }

    @Suppress("unused")
    fun boolArgument(block: suspend CommandExecutorTree.(Boolean) -> Unit) =
        argument(boolMatcher(), block)

    @Suppress("unused")
    fun entityArgument(block: suspend CommandExecutorTree.(EntityType) -> Unit) =
        argument(entityMatcher(), block)

    @Suppress("unused")
    inline fun <reified T : Enum<T>> enumArgument(noinline block: suspend CommandExecutorTree.(T) -> Unit) =
        argument(enumMatcher<T>(), block)

    fun materialArgument(block: suspend CommandExecutorTree.(Material) -> Unit) =
        argument(materialMatcher(), block)

    @Suppress("unused")
    fun blockArgument(block: suspend CommandExecutorTree.(Material) -> Unit) =
        argument(blockMatcher(), block)

    fun playerArgument(block: suspend CommandExecutorTree.(Player) -> Unit) =
        argument(playerMatcher(), block)

    fun offlinePlayerArgument(block: suspend CommandExecutorTree.(OfflinePlayer) -> Unit) =
        argument(offlinePlayerMatcher(), block)

    fun playerWithPermissionArgument(
        vararg permissions: Permission,
        block: suspend CommandExecutorTree.(Player) -> Unit,
    ) = argument(playerWithPermissionMatcher(permissions.toList()), block)

    fun playerWithPermissionArgument(
        vararg permissions: String,
        block: suspend CommandExecutorTree.(Player) -> Unit,
    ) = argument(playerWithPermissionMatcher(permissions.toList()), block)

    fun stringArgument(vararg arguments: String, block: suspend CommandExecutorTree.(String) -> Unit) =
        argument(stringMatcher(arguments.toList()), block)

    fun stringArgument(arguments: Iterable<String>, block: suspend CommandExecutorTree.(String) -> Unit) =
        argument(stringMatcher(arguments), block)
}
