package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import land.vani.mcorouhlin.paper.component.AdventureComponent;
import net.minecraft.Util;
import net.minecraft.network.chat.contents.BlockDataSource;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.EntityDataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.StorageDataSource;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

@SuppressWarnings("ALL")
public interface Component extends Message, FormattedText, Iterable<Component> { // CraftBukkit

    static Component nullToEmpty(@Nullable String string) {
        return (Component) (string != null ? Component.literal(string) : CommonComponents.EMPTY);
    }

    static MutableComponent literal(String string) {
        return MutableComponent.create(new LiteralContents(string));
    }
    // CraftBukkit end

    static MutableComponent translatable(String key) {
        return MutableComponent.create(new TranslatableContents(key));
    }

    static MutableComponent translatable(String key, Object... args) {
        return MutableComponent.create(new TranslatableContents(key, args));
    }

    static MutableComponent empty() {
        return MutableComponent.create(ComponentContents.EMPTY);
    }

    static MutableComponent keybind(String string) {
        return MutableComponent.create(new KeybindContents(string));
    }

    static MutableComponent nbt(String rawPath, boolean interpret, Optional<Component> separator, DataSource dataSource) {
        return MutableComponent.create(new NbtContents(rawPath, interpret, separator, dataSource));
    }

    static MutableComponent score(String name, String objective) {
        return MutableComponent.create(new ScoreContents(name, objective));
    }

    static MutableComponent selector(String pattern, Optional<Component> separator) {
        return MutableComponent.create(new SelectorContents(pattern, separator));
    }

    // CraftBukkit start
    default Stream<Component> stream() {
        return Streams.concat(new Stream[]{Stream.of(this), this.getSiblings().stream().flatMap(Component::stream)});
    }

    @Override
    default Iterator<Component> iterator() {
        return this.stream().iterator();
    }

    Style getStyle();

    ComponentContents getContents();

    @Override
    default String getString() {
        return FormattedText.super.getString();
    }

    default String getString(int length) {
        StringBuilder stringbuilder = new StringBuilder();

        this.visit((s) -> {
            int j = length - stringbuilder.length();

            if (j <= 0) {
                return Component.STOP_ITERATION;
            } else {
                stringbuilder.append(s.length() <= j ? s : s.substring(0, j));
                return Optional.empty();
            }
        });
        return stringbuilder.toString();
    }

    List<Component> getSiblings();

    default MutableComponent plainCopy() {
        return MutableComponent.create(this.getContents());
    }

    default MutableComponent copy() {
        return new MutableComponent(this.getContents(), new ArrayList(this.getSiblings()), this.getStyle());
    }

    FormattedCharSequence getVisualOrderText();

    @Override
    default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledVisitor, Style style) {
        Style chatmodifier1 = this.getStyle().applyTo(style);
        Optional<T> optional = this.getContents().visit(styledVisitor, chatmodifier1);

        if (optional.isPresent()) {
            return optional;
        } else {
            Iterator iterator = this.getSiblings().iterator();

            Optional optional1;

            do {
                if (!iterator.hasNext()) {
                    return Optional.empty();
                }

                Component ichatbasecomponent = (Component) iterator.next();

                optional1 = ichatbasecomponent.visit(styledVisitor, chatmodifier1);
            } while (!optional1.isPresent());

            return optional1;
        }
    }

    @Override
    default <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
        Optional<T> optional = this.getContents().visit(visitor);

        if (optional.isPresent()) {
            return optional;
        } else {
            Iterator iterator = this.getSiblings().iterator();

            Optional optional1;

            do {
                if (!iterator.hasNext()) {
                    return Optional.empty();
                }

                Component ichatbasecomponent = (Component) iterator.next();

                optional1 = ichatbasecomponent.visit(visitor);
            } while (!optional1.isPresent());

            return optional1;
        }
    }

    default List<Component> toFlatList() {
        return this.toFlatList(Style.EMPTY);
    }

    default List<Component> toFlatList(Style style) {
        List<Component> list = Lists.newArrayList();

        this.visit((chatmodifier1, s) -> {
            if (!s.isEmpty()) {
                list.add(Component.literal(s).withStyle(chatmodifier1));
            }

            return Optional.empty();
        }, style);
        return list;
    }

    default boolean contains(Component text) {
        if (this.equals(text)) {
            return true;
        } else {
            List<Component> list = this.toFlatList();
            List<Component> list1 = text.toFlatList(this.getStyle());

            return Collections.indexOfSubList(list, list1) != -1;
        }
    }

    public static class Serializer implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {

        private static final Gson GSON = (Gson) Util.make(() -> {
            GsonBuilder gsonbuilder = new GsonBuilder();

            gsonbuilder.disableHtmlEscaping();
            gsonbuilder.registerTypeAdapter(AdventureComponent.class, new AdventureComponent.Serializer()); // Paper
            gsonbuilder.registerTypeHierarchyAdapter(Component.class, new Component.Serializer());
            gsonbuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
            gsonbuilder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
            return gsonbuilder.create();
        });
        private static final Field JSON_READER_POS = (Field) Util.make(() -> {
            try {
                new JsonReader(new StringReader(""));
                Field field = JsonReader.class.getDeclaredField("pos");

                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException nosuchfieldexception) {
                throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", nosuchfieldexception);
            }
        });
        private static final Field JSON_READER_LINESTART = (Field) Util.make(() -> {
            try {
                new JsonReader(new StringReader(""));
                Field field = JsonReader.class.getDeclaredField("lineStart");

                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException nosuchfieldexception) {
                throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", nosuchfieldexception);
            }
        });

        public Serializer() {
        }

        private static Object unwrapTextArgument(Object text) {
            if (text instanceof Component) {
                Component ichatbasecomponent = (Component) text;

                if (ichatbasecomponent.getStyle().isEmpty() && ichatbasecomponent.getSiblings().isEmpty()) {
                    ComponentContents componentcontents = ichatbasecomponent.getContents();

                    if (componentcontents instanceof LiteralContents) {
                        LiteralContents literalcontents = (LiteralContents) componentcontents;

                        return literalcontents.text();
                    }
                }
            }

            return text;
        }

        public static String toJson(Component text) {
            return Component.Serializer.GSON.toJson(text);
        }

        public static String toStableJson(Component text) {
            return GsonHelper.toStableString(Serializer.toJsonTree(text));
        }

        public static JsonElement toJsonTree(Component text) {
            return Component.Serializer.GSON.toJsonTree(text);
        }

        @Nullable
        public static MutableComponent fromJson(String json) {
            return (MutableComponent) GsonHelper.fromJson(Component.Serializer.GSON, json, MutableComponent.class, false);
        }

        @Nullable
        public static MutableComponent fromJson(JsonElement json) {
            return (MutableComponent) Component.Serializer.GSON.fromJson(json, MutableComponent.class);
        }

        @Nullable
        public static MutableComponent fromJsonLenient(String json) {
            return (MutableComponent) GsonHelper.fromJson(Component.Serializer.GSON, json, MutableComponent.class, true);
        }

        public static MutableComponent fromJson(com.mojang.brigadier.StringReader reader) {
            try {
                JsonReader jsonreader = new JsonReader(new StringReader(reader.getRemaining()));

                jsonreader.setLenient(false);
                MutableComponent ichatmutablecomponent = (MutableComponent) Component.Serializer.GSON.getAdapter(MutableComponent.class).read(jsonreader);

                reader.setCursor(reader.getCursor() + Serializer.getPos(jsonreader));
                return ichatmutablecomponent;
            } catch (StackOverflowError | IOException ioexception) {
                throw new JsonParseException(ioexception);
            }
        }

        private static int getPos(JsonReader reader) {
            try {
                return Component.Serializer.JSON_READER_POS.getInt(reader) - Component.Serializer.JSON_READER_LINESTART.getInt(reader) + 1;
            } catch (IllegalAccessException illegalaccessexception) {
                throw new IllegalStateException("Couldn't read position of JsonReader", illegalaccessexception);
            }
        }

        public MutableComponent deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            if (jsonelement.isJsonPrimitive()) {
                return Component.literal(jsonelement.getAsString());
            } else {
                MutableComponent ichatmutablecomponent;

                if (!jsonelement.isJsonObject()) {
                    if (jsonelement.isJsonArray()) {
                        JsonArray jsonarray = jsonelement.getAsJsonArray();

                        ichatmutablecomponent = null;
                        Iterator iterator = jsonarray.iterator();

                        while (iterator.hasNext()) {
                            JsonElement jsonelement1 = (JsonElement) iterator.next();
                            MutableComponent ichatmutablecomponent1 = this.deserialize(jsonelement1, jsonelement1.getClass(), jsondeserializationcontext);

                            if (ichatmutablecomponent == null) {
                                ichatmutablecomponent = ichatmutablecomponent1;
                            } else {
                                ichatmutablecomponent.append((Component) ichatmutablecomponent1);
                            }
                        }

                        return ichatmutablecomponent;
                    } else {
                        throw new JsonParseException("Don't know how to turn " + jsonelement + " into a Component");
                    }
                } else {
                    JsonObject jsonobject = jsonelement.getAsJsonObject();
                    String s;

                    if (jsonobject.has("text")) {
                        s = GsonHelper.getAsString(jsonobject, "text");
                        ichatmutablecomponent = s.isEmpty() ? Component.empty() : Component.literal(s);
                    } else if (jsonobject.has("translate")) {
                        s = GsonHelper.getAsString(jsonobject, "translate");
                        if (jsonobject.has("with")) {
                            JsonArray jsonarray1 = GsonHelper.getAsJsonArray(jsonobject, "with");
                            Object[] aobject = new Object[jsonarray1.size()];

                            for (int i = 0; i < aobject.length; ++i) {
                                aobject[i] = Serializer.unwrapTextArgument(this.deserialize(jsonarray1.get(i), type, jsondeserializationcontext));
                            }

                            ichatmutablecomponent = Component.translatable(s, aobject);
                        } else {
                            ichatmutablecomponent = Component.translatable(s);
                        }
                    } else if (jsonobject.has("score")) {
                        JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "score");

                        if (!jsonobject1.has("name") || !jsonobject1.has("objective")) {
                            throw new JsonParseException("A score component needs a least a name and an objective");
                        }

                        ichatmutablecomponent = Component.score(GsonHelper.getAsString(jsonobject1, "name"), GsonHelper.getAsString(jsonobject1, "objective"));
                    } else if (jsonobject.has("selector")) {
                        Optional<Component> optional = this.parseSeparator(type, jsondeserializationcontext, jsonobject);

                        ichatmutablecomponent = Component.selector(GsonHelper.getAsString(jsonobject, "selector"), optional);
                    } else if (jsonobject.has("keybind")) {
                        ichatmutablecomponent = Component.keybind(GsonHelper.getAsString(jsonobject, "keybind"));
                    } else {
                        if (!jsonobject.has("nbt")) {
                            throw new JsonParseException("Don't know how to turn " + jsonelement + " into a Component");
                        }

                        s = GsonHelper.getAsString(jsonobject, "nbt");
                        Optional<Component> optional1 = this.parseSeparator(type, jsondeserializationcontext, jsonobject);
                        boolean flag = GsonHelper.getAsBoolean(jsonobject, "interpret", false);
                        Object object;

                        if (jsonobject.has("block")) {
                            object = new BlockDataSource(GsonHelper.getAsString(jsonobject, "block"));
                        } else if (jsonobject.has("entity")) {
                            object = new EntityDataSource(GsonHelper.getAsString(jsonobject, "entity"));
                        } else {
                            if (!jsonobject.has("storage")) {
                                throw new JsonParseException("Don't know how to turn " + jsonelement + " into a Component");
                            }

                            object = new StorageDataSource(new ResourceLocation(GsonHelper.getAsString(jsonobject, "storage")));
                        }

                        ichatmutablecomponent = Component.nbt(s, flag, optional1, (DataSource) object);
                    }

                    if (jsonobject.has("extra")) {
                        JsonArray jsonarray2 = GsonHelper.getAsJsonArray(jsonobject, "extra");

                        if (jsonarray2.size() <= 0) {
                            throw new JsonParseException("Unexpected empty array of components");
                        }

                        for (int j = 0; j < jsonarray2.size(); ++j) {
                            ichatmutablecomponent.append((Component) this.deserialize(jsonarray2.get(j), type, jsondeserializationcontext));
                        }
                    }

                    ichatmutablecomponent.setStyle((Style) jsondeserializationcontext.deserialize(jsonelement, Style.class));
                    return ichatmutablecomponent;
                }
            }
        }

        private Optional<Component> parseSeparator(Type type, JsonDeserializationContext context, JsonObject json) {
            return json.has("separator") ? Optional.of(this.deserialize(json.get("separator"), type, context)) : Optional.empty();
        }

        private void serializeStyle(Style style, JsonObject json, JsonSerializationContext context) {
            JsonElement jsonelement = context.serialize(style);

            if (jsonelement.isJsonObject()) {
                JsonObject jsonobject1 = (JsonObject) jsonelement;
                Iterator iterator = jsonobject1.entrySet().iterator();

                while (iterator.hasNext()) {
                    Entry<String, JsonElement> entry = (Entry) iterator.next();

                    json.add((String) entry.getKey(), (JsonElement) entry.getValue());
                }
            }

        }

        public JsonElement serialize(Component ichatbasecomponent, Type type, JsonSerializationContext jsonserializationcontext) {
            if (ichatbasecomponent instanceof AdventureComponent)
                return jsonserializationcontext.serialize(ichatbasecomponent); // Paper
            JsonObject jsonobject = new JsonObject();

            if (!ichatbasecomponent.getStyle().isEmpty()) {
                this.serializeStyle(ichatbasecomponent.getStyle(), jsonobject, jsonserializationcontext);
            }

            if (!ichatbasecomponent.getSiblings().isEmpty()) {
                JsonArray jsonarray = new JsonArray();
                Iterator iterator = ichatbasecomponent.getSiblings().iterator();

                while (iterator.hasNext()) {
                    Component ichatbasecomponent1 = (Component) iterator.next();

                    jsonarray.add(this.serialize(ichatbasecomponent1, Component.class, jsonserializationcontext));
                }

                jsonobject.add("extra", jsonarray);
            }

            ComponentContents componentcontents = ichatbasecomponent.getContents();

            if (componentcontents == ComponentContents.EMPTY) {
                jsonobject.addProperty("text", "");
            } else if (componentcontents instanceof LiteralContents) {
                LiteralContents literalcontents = (LiteralContents) componentcontents;

                jsonobject.addProperty("text", literalcontents.text());
            } else if (componentcontents instanceof TranslatableContents) {
                TranslatableContents translatablecontents = (TranslatableContents) componentcontents;

                jsonobject.addProperty("translate", translatablecontents.getKey());
                if (translatablecontents.getArgs().length > 0) {
                    JsonArray jsonarray1 = new JsonArray();
                    Object[] aobject = translatablecontents.getArgs();
                    int i = aobject.length;

                    for (int j = 0; j < i; ++j) {
                        Object object = aobject[j];

                        if (object instanceof Component) {
                            jsonarray1.add(this.serialize((Component) object, object.getClass(), jsonserializationcontext));
                        } else {
                            jsonarray1.add(new JsonPrimitive(String.valueOf(object)));
                        }
                    }

                    jsonobject.add("with", jsonarray1);
                }
            } else if (componentcontents instanceof ScoreContents) {
                ScoreContents scorecontents = (ScoreContents) componentcontents;
                JsonObject jsonobject1 = new JsonObject();

                jsonobject1.addProperty("name", scorecontents.getName());
                jsonobject1.addProperty("objective", scorecontents.getObjective());
                jsonobject.add("score", jsonobject1);
            } else if (componentcontents instanceof SelectorContents) {
                SelectorContents selectorcontents = (SelectorContents) componentcontents;

                jsonobject.addProperty("selector", selectorcontents.getPattern());
                this.serializeSeparator(jsonserializationcontext, jsonobject, selectorcontents.getSeparator());
            } else if (componentcontents instanceof KeybindContents) {
                KeybindContents keybindcontents = (KeybindContents) componentcontents;

                jsonobject.addProperty("keybind", keybindcontents.getName());
            } else {
                if (!(componentcontents instanceof NbtContents)) {
                    throw new IllegalArgumentException("Don't know how to serialize " + componentcontents + " as a Component");
                }

                NbtContents nbtcontents = (NbtContents) componentcontents;

                jsonobject.addProperty("nbt", nbtcontents.getNbtPath());
                jsonobject.addProperty("interpret", nbtcontents.isInterpreting());
                this.serializeSeparator(jsonserializationcontext, jsonobject, nbtcontents.getSeparator());
                DataSource datasource = nbtcontents.getDataSource();

                if (datasource instanceof BlockDataSource) {
                    BlockDataSource blockdatasource = (BlockDataSource) datasource;

                    jsonobject.addProperty("block", blockdatasource.posPattern());
                } else if (datasource instanceof EntityDataSource) {
                    EntityDataSource entitydatasource = (EntityDataSource) datasource;

                    jsonobject.addProperty("entity", entitydatasource.selectorPattern());
                } else {
                    if (!(datasource instanceof StorageDataSource)) {
                        throw new IllegalArgumentException("Don't know how to serialize " + componentcontents + " as a Component");
                    }

                    StorageDataSource storagedatasource = (StorageDataSource) datasource;

                    jsonobject.addProperty("storage", storagedatasource.id().toString());
                }
            }

            return jsonobject;
        }

        private void serializeSeparator(JsonSerializationContext context, JsonObject json, Optional<Component> separator) {
            separator.ifPresent((ichatbasecomponent) -> {
                json.add("separator", this.serialize(ichatbasecomponent, ichatbasecomponent.getClass(), context));
            });
        }
    }
}
