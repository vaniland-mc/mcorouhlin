package land.vani.mcorouhlin.paper.component

import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.contents.LiteralContents
import net.minecraft.util.FormattedCharSequence
import java.lang.reflect.Type
import net.minecraft.network.chat.Component as NMNComponent

class AdventureComponent(
    val adventure: Component,
) : NMNComponent {
    val deepConverted: NMNComponent by lazy {
        WrapperAwareSerializer.serialize(adventure)
    }

    override fun getStyle(): Style = deepConverted.style

    override fun getContents(): ComponentContents = if (adventure is TextComponent) {
        LiteralContents(adventure.content())
    } else {
        deepConverted.contents
    }

    override fun getSiblings(): List<NMNComponent> = deepConverted.siblings

    override fun getVisualOrderText(): FormattedCharSequence = deepConverted.visualOrderText

    override fun getString(): String = PlainTextComponentSerializer.plainText().serialize(adventure)

    override fun plainCopy(): MutableComponent = deepConverted.plainCopy()

    override fun copy(): MutableComponent = deepConverted.copy()

    class Serializer : JsonSerializer<AdventureComponent> {
        override fun serialize(
            src: AdventureComponent,
            typeOfSrc: Type,
            context: JsonSerializationContext?,
        ): JsonElement {
            return GsonComponentSerializer.gson()
                .serializer()
                .toJsonTree(src.adventure, Component::class.java)
        }
    }
}
