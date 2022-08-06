package land.vani.mcorouhlin.paper.component

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.ComponentSerializer
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.network.chat.Component as NMNComponent

object WrapperAwareSerializer : ComponentSerializer<Component, Component, NMNComponent> {
    override fun deserialize(input: net.minecraft.network.chat.Component): Component {
        if (input is AdventureComponent) {
            return input.adventure
        }
        return GsonComponentSerializer.gson().serializer()
            .fromJson(NMNComponent.Serializer.toJsonTree(input), Component::class.java)
    }

    override fun serialize(component: Component): net.minecraft.network.chat.Component {
        return NMNComponent.Serializer.fromJson(
            GsonComponentSerializer.gson().serializer()
                .toJsonTree(component)
        ) ?: NMNComponent.empty()
    }
}
