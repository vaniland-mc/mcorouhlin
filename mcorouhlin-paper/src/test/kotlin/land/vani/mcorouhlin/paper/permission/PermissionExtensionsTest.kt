package land.vani.mcorouhlin.paper.permission

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import land.vani.mcorouhlin.permission.Permission
import land.vani.mcorouhlin.permission.PermissionDefault

class PermissionExtensionsTest : DescribeSpec({
    it("asBukkit") {
        val permission = object : Permission {
            override val node: String = "mcorouhlin.test"
            override val description: String = "some permission"
            override val children: Map<Permission, Boolean> = mapOf()
            override val default: PermissionDefault = PermissionDefault.OP
        }
        val bukkitPermission = permission.asBukkit

        bukkitPermission.name shouldBe "mcorouhlin.test"
        bukkitPermission.description shouldBe "some permission"
        bukkitPermission.children.shouldBeEmpty()
        bukkitPermission.default shouldBe org.bukkit.permissions.PermissionDefault.OP
    }
})
