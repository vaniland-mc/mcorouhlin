package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

@SuppressWarnings("ALL")
public interface ComponentContents {
    ComponentContents EMPTY = new ComponentContents() {
        @Override
        public String toString() {
            return "empty";
        }
    };

    default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> visitor, Style style) {
        return Optional.empty();
    }

    default <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
        return Optional.empty();
    }

    default MutableComponent resolve(@Nullable CommandSourceStack source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        return MutableComponent.create(this);
    }
}
