package land.vani.mcorouhlin.paper.config

import com.google.common.jimfs.Jimfs
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.FileSystem
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

private class TestConfig(path: Path) : BukkitConfiguration<TestConfig>(path) {
    var nullableInt: Int? by value("nullableInt")

    var defaultInt: Int by value<Int>("defaultInt").default(10)
    var strictInt: Int by value<Int>("strictInt").strict()
}

private data class Data(val inner: String)

private class TestMapConfig(path: Path) : BukkitConfiguration<TestMapConfig>(path) {
    var map by value<Map<String, Any?>>("map")
        .transform<Map<String, Data>>(
            { raw ->
                raw.orEmpty()
                    .map { (key, value) -> key to Data("$value") }
                    .toMap()
            },
            { complex ->
                complex.map { (key, value) -> key to value.inner }
                    .toMap()
            }
        )
}

class BukkitConfigurationTest : DescribeSpec({
    lateinit var fs: FileSystem

    beforeTest {
        fs = Jimfs.newFileSystem()
    }

    afterTest {
        withContext(Dispatchers.IO) {
            fs.close()
        }
    }

    it("reload") {
        val path = fs.getPath("test.yml")
        path.writeText(
            """
                strictInt: 5
            """.trimIndent()
        )
        val config = TestConfig(path)

        config.nullableInt.shouldBeNull()
        config.defaultInt shouldBeExactly 10
        config.strictInt shouldBeExactly 5

        path.writeText(
            """
                nullableInt: 2
                defaultInt: 20
                strictInt: 10
            """.trimIndent()
        )
        config.reload()

        assertSoftly(config.nullableInt) {
            shouldNotBeNull()
            shouldBeExactly(2)
        }
        config.defaultInt shouldBeExactly 20
        config.strictInt shouldBeExactly 10
    }

    it("save") {
        val path = fs.getPath("foo/test.yml")

        val config = TestConfig(path)

        config.nullableInt = 10
        config.defaultInt = 20
        config.strictInt = 30
        config.save()

        path.readText() shouldBe """
            nullableInt: 10
            defaultInt: 20
            strictInt: 30
            
        """.trimIndent()
    }

    it("map") {
        val path = fs.getPath("test.yml")

        path.writeText(
            """
                map:
                    foo: true
                    bar: false
            """.trimIndent()
        )
        val config = TestMapConfig(path)
        config.reload()

        config.map shouldBe mapOf(
            "foo" to Data("true"),
            "bar" to Data("false"),
        )
    }
})
