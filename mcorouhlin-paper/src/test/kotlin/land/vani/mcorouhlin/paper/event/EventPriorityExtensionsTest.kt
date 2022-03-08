package land.vani.mcorouhlin.paper.event

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.collection
import land.vani.mcorouhlin.event.EventPriority

class EventPriorityExtensionsTest : DescribeSpec({
    it("asBukkit") {
        val expected = listOf(
            EventPriority.HIGHEST to org.bukkit.event.EventPriority.HIGHEST,
            EventPriority.HIGH to org.bukkit.event.EventPriority.HIGH,
            EventPriority.NORMAL to org.bukkit.event.EventPriority.NORMAL,
            EventPriority.LOW to org.bukkit.event.EventPriority.LOW,
            EventPriority.LOWEST to org.bukkit.event.EventPriority.LOWEST,
        )

        checkAll(Exhaustive.collection(expected)) { (mcorouhlin, bukkit) ->
            mcorouhlin.asBukkit shouldBe bukkit
        }
    }
})
