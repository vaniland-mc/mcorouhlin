package land.vani.plugin.mcrouhlin.command

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import org.bukkit.command.Command
import org.bukkit.entity.Player

fun completesWith(completes: List<String>) = object : Matcher<List<String>> {
    override fun test(value: List<String>): MatcherResult = MatcherResult(
        value == completes,
        { "tab completion should be $completes but expected is $value" },
        { "tab completion should not be $completes but expected is $value" }
    )
}

class TabCompleteAsserter(
    private val player: Player,
    private val command: Command,
    private val commandName: String,
) {
    infix fun List<String>.to(expected: List<String>) {
        val complete = command.tabComplete(player, commandName, toTypedArray())
        complete should completesWith(expected)
    }
}

fun tabCompleteAssert(
    player: Player,
    command: Command,
    commandName: String,
    block: TabCompleteAsserter.() -> Unit,
) {
    TabCompleteAsserter(player, command, commandName).run(block)
}
