package land.vani.plugin.mcrouhlin.config

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import land.vani.plugin.mcorouhlin.config.Config
import land.vani.plugin.mcorouhlin.config.ConfigValueDelegateNullable
import land.vani.plugin.mcorouhlin.config.type.ConfigValueType
import land.vani.plugin.mcorouhlin.util.ObservableList

class ConfigTest : ShouldSpec({
    context("config value") {
        val path = kotlin.io.path.createTempFile()

        data class TestSection(
            var b: Int = 0,
        )

        class TestSectionValueType : ConfigValueType<TestSection?> {
            override fun get(config: Config, node: String): TestSection {
                val b = config.getUnsafe<Int?>("$node.b")
                return if (b != null) {
                    TestSection(b)
                } else {
                    TestSection()
                }
            }

            override fun set(config: Config, node: String, value: TestSection?) {
                config.setUnsafe("$node.b", value?.b)
            }

            override fun getList(config: Config, node: String): MutableList<TestSection?> {
                val list = config.getUnsafeList<Map<String, Any>>(node)
                return list.map { map ->
                    TestSection().apply {
                        b = map["b"] as Int
                    }
                }.toMutableList().let { v ->
                    @Suppress("UNCHECKED_CAST")
                    ObservableList(v as MutableList<TestSection?>) { values ->
                        val mapList = values.map {
                            mapOf("b" to it?.b)
                        }
                        config.setUnsafe(node, mapList)
                    }
                }
            }

            override fun setList(config: Config, node: String, values: List<TestSection?>) {
                val list = values.map {
                    mapOf("b" to it?.b)
                }
                config.setUnsafe(node, list)
            }
        }

        fun Config.testSection(node: String) = ConfigValueDelegateNullable(this, node, TestSectionValueType())

        val config = object : Config(path) {
            var a by string("a").notNull()
            var testSection by testSection("testSection").notNull()
            var testSectionList by testSection("testSectionList").notNull().list()

            var c by int("c").notNull().list()
        }
        config.a = "aaa"
        config.testSection = TestSection().apply { b = 100 }
        config.testSectionList += TestSection().apply { b = 200 }
        config.c.add(20)

        config.save()

        should("config value is set") {
            config.a shouldBe "aaa"
            config.testSection.b shouldBe 100
            config.testSectionList shouldContainExactly listOf(TestSection().apply { b = 200 })
            config.c shouldContainExactly listOf(20)
        }
    }
})
