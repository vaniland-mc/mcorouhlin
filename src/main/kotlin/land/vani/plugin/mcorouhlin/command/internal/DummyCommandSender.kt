package land.vani.plugin.mcorouhlin.command.internal

import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.plugin.Plugin
import java.util.UUID

object DummyCommandSender : CommandSender {
    override fun isOp(): Boolean = true

    override fun setOp(value: Boolean) {}

    override fun isPermissionSet(name: String): Boolean = true

    override fun isPermissionSet(perm: Permission): Boolean = true

    override fun hasPermission(name: String): Boolean = true

    override fun hasPermission(perm: Permission): Boolean = true

    override fun addAttachment(plugin: Plugin, name: String, value: Boolean): PermissionAttachment {
        TODO("Not yet implemented")
    }

    override fun addAttachment(plugin: Plugin): PermissionAttachment {
        TODO("Not yet implemented")
    }

    override fun addAttachment(plugin: Plugin, name: String, value: Boolean, ticks: Int): PermissionAttachment? {
        TODO("Not yet implemented")
    }

    override fun addAttachment(plugin: Plugin, ticks: Int): PermissionAttachment? {
        TODO("Not yet implemented")
    }

    override fun removeAttachment(attachment: PermissionAttachment) {}

    override fun recalculatePermissions() {}

    override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> {
        TODO("Not yet implemented")
    }

    override fun sendMessage(message: String) {}

    override fun sendMessage(vararg messages: String?) {
    }

    override fun sendMessage(sender: UUID?, message: String) {
    }

    override fun sendMessage(sender: UUID?, vararg messages: String?) {
    }

    override fun getServer(): Server {
        TODO("Not yet implemented")
    }

    override fun getName(): String = "MockPlayer"

    override fun spigot(): CommandSender.Spigot {
        TODO("Not yet implemented")
    }
}
