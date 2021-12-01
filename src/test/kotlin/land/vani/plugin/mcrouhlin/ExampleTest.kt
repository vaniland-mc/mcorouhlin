package land.vani.plugin.mcrouhlin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeExactly

class ExampleTest : DescribeSpec({
    describe("aaa") {
        it("bbb") {
            1 + 2 shouldBeExactly 3
        }
    }
})
