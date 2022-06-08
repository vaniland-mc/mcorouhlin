package land.vani.mcorouhlin.paper.permission

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import land.vani.mcorouhlin.paper.TestMcorouhlinPlugin
import land.vani.mcorouhlin.paper.mockbukkit.loadSimple
import land.vani.mcorouhlin.permission.Permission
import land.vani.mcorouhlin.permission.PermissionDefault
import net.kyori.adventure.util.TriState
import org.bukkit.permissions.Permissible

private enum class TestPermissions(
    override val node: String,
    override val description: String?,
    override val default: PermissionDefault?,
    override val children: Map<Permission, Boolean>,
) : Permission {
    TEST(
        "mcorouhlin.test",
        null,
        PermissionDefault.FALSE,
        mapOf(),
    ),
    ;
}

class PermissibleExtensionsTest : DescribeSpec({
    lateinit var server: ServerMock
    lateinit var plugin: TestMcorouhlinPlugin
    lateinit var permissible: Permissible

    beforeEach {
        server = MockBukkit.mock()
        plugin = server.pluginManager.loadSimple()
        permissible = server.addPlayer()
    }

    afterEach {
        MockBukkit.unmock()
    }

    describe("hasPermission") {
        it("default when permission is not set") {
            permissible.hasPermission(TestPermissions.TEST) shouldBe false
        }

        it("false when permission set to false") {
            permissible.addAttachment(
                plugin,
                TestPermissions.TEST.node,
                false,
            )

            permissible.hasPermission(TestPermissions.TEST) shouldBe false
        }

        it("true when permission set to true") {
            permissible.addAttachment(
                plugin,
                TestPermissions.TEST.node,
                true,
            )

            permissible.hasPermission(TestPermissions.TEST) shouldBe true
        }
    }

    describe("isPermissionSet") {
        it("false when permission is not set") {
            permissible.isPermissionSet(TestPermissions.TEST) shouldBe false
        }

        it("true when permission is set to true") {
            permissible.addAttachment(
                plugin,
                TestPermissions.TEST.node,
                true,
            )

            permissible.isPermissionSet(TestPermissions.TEST) shouldBe true
        }
    }

    describe("permissionValue") {
        it("NOT_SET when permission is not set") {
            permissible.permissionValue(TestPermissions.TEST) shouldBe TriState.NOT_SET
        }

        it("TRUE when permission is set to true") {
            permissible.addAttachment(
                plugin,
                TestPermissions.TEST.node,
                true,
            )

            permissible.permissionValue(TestPermissions.TEST) shouldBe TriState.TRUE
        }

        // FYI: https://github.com/MockBukkit/MockBukkit/issues/444
        xit("FALSE when permission is set to false") {
            permissible.addAttachment(
                plugin,
                TestPermissions.TEST.node,
                false,
            )

            permissible.isPermissionSet(TestPermissions.TEST) shouldBe true
            permissible.permissionValue(TestPermissions.TEST) shouldBe TriState.FALSE
        }
    }
})
