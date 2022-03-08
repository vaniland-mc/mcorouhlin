package land.vani.mcorouhlin.extension

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.Continuation

@Suppress("RedundantSuspendModifier")
private class Test {
    @Suppress("unused", "FunctionOnlyReturningConstant")
    suspend fun testMethodWithNoArg(): Int = 10

    @Suppress("unused")
    suspend fun testMethodWithOneArg(arg: Int): Int = arg + 5
}

@OptIn(ExperimentalCoroutinesApi::class)
class MethodExtensionsTest : DescribeSpec({
    it("invokeSuspend") {
        val instance = Test()
        val clazz = instance.javaClass
        val noArgMethod = clazz.getDeclaredMethod("testMethodWithNoArg", Continuation::class.java)
        val oneArgMethod = clazz.getDeclaredMethod(
            "testMethodWithOneArg",
            Int::class.java,
            Continuation::class.java
        )

        runTest {
            val noArgValue = noArgMethod.invokeSuspend(instance)

            noArgValue.shouldBeInstanceOf<Int>()
            noArgValue shouldBeExactly 10

            val oneArgValue = oneArgMethod.invokeSuspend(instance, 10)

            oneArgValue.shouldBeInstanceOf<Int>()
            oneArgValue shouldBeExactly 15
        }
    }
})
