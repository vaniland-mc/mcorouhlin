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

package land.vani.mcorouhlin.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext

sealed class CommandArgument<S, T, V> {

    abstract fun getValue(context: CommandContext<S>): V

    abstract fun buildArgument(): RequiredArgumentBuilder<S, T>

    abstract fun apply(block: RequiredArgumentBuilder<S, T>.() -> Unit): CommandArgument<S, T, V>
}

class RequiredArgument<S, T, V>(
    private val name: String,
    private val type: ArgumentType<T>,
    private val getter: (CommandContext<S>, String) -> V,
) : CommandArgument<S, T, V>() {
    override fun getValue(context: CommandContext<S>): V = getter(context, name)

    private var apply: (RequiredArgumentBuilder<S, T>.() -> Unit)? = null

    override fun buildArgument(): RequiredArgumentBuilder<S, T> =
        RequiredArgumentBuilder.argument<S, T>(name, type).also { apply?.invoke(it) }

    override fun apply(block: RequiredArgumentBuilder<S, T>.() -> Unit) =
        this.also { this.apply = block }
}

class OptionalArgument<S, T, V : D, D>(
    private val argument: RequiredArgument<S, T, V>,
    private val default: (S) -> D,
) : CommandArgument<S, T, D>() {

    override fun getValue(context: CommandContext<S>) = try {
        argument.getValue(context)
    } catch (ignored: IllegalArgumentException) {
        default(context.source)
    }

    override fun buildArgument() = argument.buildArgument()
    override fun apply(block: RequiredArgumentBuilder<S, T>.() -> Unit) =
        this.also { argument.apply(block) }
}

fun <S, T, V> RequiredArgument<S, T, V>.optional() = OptionalArgument(this) { null }
fun <S, T, V> RequiredArgument<S, T, V>.default(value: V) = OptionalArgument(this) { value }
fun <S, T, V> RequiredArgument<S, T, V>.defaultGetter(value: (S) -> V) = OptionalArgument(this, value)
