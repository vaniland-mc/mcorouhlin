package land.vani.mcorouhlin.permission

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class PermissionDefaultTest : DescribeSpec({
    describe("getValue") {
        it("on OP with op = true arg") {
            PermissionDefault.OP.getValue(true) shouldBe true
        }

        it("on OP with op = false arg") {
            PermissionDefault.OP.getValue(false) shouldBe false
        }

        it("on NOT_OP with op = true arg") {
            PermissionDefault.NOT_OP.getValue(true) shouldBe false
        }

        it("on NOT_OP with op = false arg") {
            PermissionDefault.NOT_OP.getValue(false) shouldBe true
        }

        it("on TRUE with op = true arg") {
            PermissionDefault.TRUE.getValue(true) shouldBe true
        }

        it("on TRUE with op = false arg") {
            PermissionDefault.TRUE.getValue(false) shouldBe true
        }

        it("on FALSE with op = true arg") {
            PermissionDefault.FALSE.getValue(true) shouldBe false
        }

        it("on FALSE with op = false arg") {
            PermissionDefault.FALSE.getValue(false) shouldBe false
        }
    }
})
