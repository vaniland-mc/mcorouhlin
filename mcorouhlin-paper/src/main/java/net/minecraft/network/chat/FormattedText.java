package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Unit;

@SuppressWarnings("ALL")
public interface FormattedText {
    Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);
    FormattedText EMPTY = new FormattedText() {
        @Override
        public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
            return Optional.empty();
        }

        @Override
        public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledVisitor, Style style) {
            return Optional.empty();
        }
    };

    static FormattedText of(final String string) {
        return new FormattedText() {
            @Override
            public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
                return visitor.accept(string);
            }

            @Override
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledVisitor, Style style) {
                return styledVisitor.accept(style, string);
            }
        };
    }

    static FormattedText of(final String string, final Style style) {
        return new FormattedText() {
            @Override
            public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
                return visitor.accept(string);
            }

            @Override
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledVisitor, Style stylex) {
                return styledVisitor.accept(style.applyTo(style), string);
            }
        };
    }

    static FormattedText composite(FormattedText... visitables) {
        return composite(ImmutableList.copyOf(visitables));
    }

    static FormattedText composite(final List<? extends FormattedText> visitables) {
        return new FormattedText() {
            @Override
            public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
                for (FormattedText formattedText : visitables) {
                    Optional<T> optional = formattedText.visit(visitor);
                    if (optional.isPresent()) {
                        return optional;
                    }
                }

                return Optional.empty();
            }

            @Override
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledVisitor, Style style) {
                for (FormattedText formattedText : visitables) {
                    Optional<T> optional = formattedText.visit(styledVisitor, style);
                    if (optional.isPresent()) {
                        return optional;
                    }
                }

                return Optional.empty();
            }
        };
    }

    <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor);

    <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledVisitor, Style style);

    default String getString() {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit((string) -> {
            stringBuilder.append(string);
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public interface ContentConsumer<T> {
        Optional<T> accept(String asString);
    }

    public interface StyledContentConsumer<T> {
        Optional<T> accept(Style style, String asString);
    }
}
