package land.vani.mcorouhlin.paper.permission

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import land.vani.mcorouhlin.permission.PermissionDefault

class PermissionDefaultExtensionsTest : DescribeSpec({
    it("asBukkit") {
        listOf(
            PermissionDefault.TRUE to org.bukkit.permissions.PermissionDefault.TRUE,
            PermissionDefault.FALSE to org.bukkit.permissions.PermissionDefault.FALSE,
            PermissionDefault.OP to org.bukkit.permissions.PermissionDefault.OP,
            PermissionDefault.NOT_OP to org.bukkit.permissions.PermissionDefault.NOT_OP,
        ).exhaustive().checkAll { (mcorouhlin, bukkit) ->
            mcorouhlin.asBukkit shouldBe bukkit
        }
    }
})
