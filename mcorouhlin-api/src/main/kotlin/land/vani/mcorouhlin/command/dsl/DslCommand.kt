/*
 * Copyright 2020-present Nicolai Christophersen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package land.vani.mcorouhlin.command.dsl

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import land.vani.mcorouhlin.command.Command
import land.vani.mcorouhlin.command.CommandArgument
import land.vani.mcorouhlin.command.ExecutableCommand
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import com.mojang.brigadier.Command as BrigadierCommand

open class DslCommand<S>(
    override val literal: String,
    private val apply: (LiteralArgumentBuilder<S>.() -> Unit)? = null,
    private val block: (DslCommandBuilder<S>.() -> Unit),
) : Command<S> {
    constructor(
        literal: String,
        builderBlock: DslCommandBuilder<S>.() -> Unit,
    ) : this(literal, null, builderBlock)

    final override fun buildLiteral(): LiteralArgumentBuilder<S> {
        val dslNode = LiteralNode<S>(literal, ContextRef())
        dslNode.apply(apply)

        val builder = DslCommandBuilder(dslNode)
        block(builder)

        return dslNode.buildTree()
    }
}

@CommandDsl
class DslCommandBuilder<S>(private var dslNode: DslCommandTree<S, *>) {
    fun executes(command: BrigadierCommand<S>) {
        dslNode.executes(command)
    }

    infix fun runs(command: ExecutableCommand<S>) {
        dslNode.runs(command)
    }

    fun required(condition: (S) -> Boolean) {
        dslNode.required(condition)
    }

    fun literal(
        literal: String,
        apply: (LiteralArgumentBuilder<S>.() -> Unit)? = null,
        block: (DslCommandBuilder<S>.() -> Unit)? = null,
    ): DslCommandBuilder<S> {
        val literalNode = dslNode.literal(literal, apply)

        return DslCommandBuilder(literalNode).also { block?.invoke(it) }
    }

    fun literals(
        vararg literals: String,
        block: (DslCommandBuilder<S>.() -> Unit)? = null,
    ): DslCommandBuilder<S> {
        val literalNode = literals.fold(dslNode) { node, literal -> node.literal(literal, null) }

        return DslCommandBuilder(literalNode).also { block?.invoke(it) }
    }

    operator fun <T, V> CommandArgument<S, T, V>.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>,
    ): ReadOnlyProperty<Any?, V> {
        val argument = dslNode.argument(this, null).also { dslNode = it }
        return ReadOnlyProperty { _, _ -> argument.getter() }
    }

    fun subCommands(vararg commands: Command<S>) {
        dslNode.subCommands(*commands)
    }
}

fun <S> command(
    literal: String,
    apply: (LiteralArgumentBuilder<S>.() -> Unit)? = null,
    block: DslCommandBuilder<S>.() -> Unit,
) = DslCommand(literal, apply, block)
