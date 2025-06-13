package me.ajh123.poly_navigate.map_object;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

// 1) Sealed base interface
public sealed interface TagValueSchema<T>
        permits TagValueSchema.StringSchema,
        TagValueSchema.IntSchema,
        TagValueSchema.FloatSchema,
        TagValueSchema.BooleanSchema,
        TagValueSchema.EnumSchema
{
    String type();

    /** Parse a string value from input (quoted/unquoted), throw if invalid */
    T parse(String raw) throws CommandSyntaxException;

    /** Suggest possible completions for a partial prefix */
    List<String> suggest(String prefix);

    Tag<T> createTag(Identifier identifier, Object value, TagDefinition definition);

    // 2) DispatchMap only refers to the static codecs on each subclass
    MapCodec<TagValueSchema<?>> MAP_CODEC = Codec.STRING.dispatchMap(
            TagValueSchema::type,
            t -> switch (t) {
                case "string"  -> StringSchema.MAP_CODEC;
                case "boolean" -> BooleanSchema.MAP_CODEC;
                case "int"     -> IntSchema.MAP_CODEC;
                case "float"   -> FloatSchema.MAP_CODEC;
                case "enum"    -> EnumSchema.MAP_CODEC;
                default        -> throw new IllegalArgumentException("Unknown schema type: " + t);
            }
    );

    // 3) Expose the Codec<TagValueSchema> by calling .codec() on the MapCodec
    Codec<TagValueSchema<?>> CODEC = MAP_CODEC.codec();

    record StringSchema() implements TagValueSchema<String> {
        @Override public String type() { return "string"; }

        @Override
        public String parse(String raw) throws CommandSyntaxException {
            if (raw.isEmpty()) {
                throw new CommandSyntaxException(null, Text.literal("String cannot be empty"));
            }
            return raw; // no special parsing, just return the raw string
        }

        @Override
        public List<String> suggest(String prefix) {
            return List.of();
        }

        @Override
        public Tag<String> createTag(Identifier identifier, Object value, TagDefinition definition) {
            if (!(value instanceof String)) {
                throw new IllegalArgumentException("Value must be a String for StringSchema");
            }
            return new Tag<>(identifier, (String) value, definition);
        }

        // zero‑field: unit() gives you an encoder+decoder that never reads or writes extra keys
        public static final MapCodec<StringSchema> MAP_CODEC = MapCodec.unit(StringSchema::new);
    }

    record BooleanSchema() implements TagValueSchema<Boolean> {
        @Override public String type() { return "boolean"; }

        @Override
        public Boolean parse(String raw) throws CommandSyntaxException {
            if ("true".equalsIgnoreCase(raw) || "1".equals(raw)) {
                return true;
            } else if ("false".equalsIgnoreCase(raw) || "0".equals(raw)) {
                return false;
            } else {
                throw new CommandSyntaxException(null, Text.literal("Invalid boolean: " + raw));
            }
        }

        @Override
        public List<String> suggest(String prefix) {
            return List.of("true", "false", "1", "0");
        }

        @Override
        public Tag<Boolean> createTag(Identifier identifier, Object value, TagDefinition definition) {
            if (!(value instanceof Boolean)) {
                throw new IllegalArgumentException("Value must be a Boolean for BooleanSchema");
            }
            return new Tag<>(identifier, (Boolean) value, definition);
        }

        public static final MapCodec<BooleanSchema> MAP_CODEC = MapCodec.unit(BooleanSchema::new);
    }

    record IntSchema(Optional<Integer> min, Optional<Integer> max) implements TagValueSchema<Integer> {
        @Override public String type() { return "int"; }

        @Override
        public Integer parse(String raw) throws CommandSyntaxException {
            try {
                int val = Integer.parseInt(raw);
                if (min.isPresent() && val < min.get())
                    throw new CommandSyntaxException(null, Text.literal("Too small: " + val));
                if (max.isPresent() && val > max.get())
                    throw new CommandSyntaxException(null, Text.literal("Too large: " + val));
                return val;
            } catch (NumberFormatException e) {
                throw new CommandSyntaxException(null, Text.literal("Invalid integer: " + raw));
            }
        }

        @Override
        public List<String> suggest(String prefix) {
            return List.of(); // optional: you could suggest min/max
        }

        @Override
        public Tag<Integer> createTag(Identifier identifier, Object value, TagDefinition definition) {
            if (!(value instanceof Integer)) {
                throw new IllegalArgumentException("Value must be an Integer for IntSchema");
            }
            return new Tag<>(identifier, (Integer) value, definition);
        }

        public static final MapCodec<IntSchema> MAP_CODEC = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        Codec.INT.optionalFieldOf("min").forGetter(IntSchema::min),
                        Codec.INT.optionalFieldOf("max").forGetter(IntSchema::max)
                ).apply(inst, IntSchema::new)
        );
    }

    record FloatSchema(Optional<Float> min, Optional<Float> max) implements TagValueSchema<Float> {
        @Override public String type() { return "float"; }

        @Override
        public Float parse(String raw) throws CommandSyntaxException {
            try {
                float val = Float.parseFloat(raw);
                if (min.isPresent() && val < min.get())
                    throw new CommandSyntaxException(null, Text.literal("Too small: " + val));
                if (max.isPresent() && val > max.get())
                    throw new CommandSyntaxException(null, Text.literal("Too large: " + val));
                return val;
            } catch (NumberFormatException e) {
                throw new CommandSyntaxException(null, Text.literal("Invalid float: " + raw));
            }
        }

        @Override
        public List<String> suggest(String prefix) {
            return List.of();
        }

        @Override
        public Tag<Float> createTag(Identifier identifier, Object value, TagDefinition definition) {
            if (!(value instanceof Float)) {
                throw new IllegalArgumentException("Value must be a Float for FloatSchema");
            }
            return new Tag<>(identifier, (Float) value, definition);
        }

        public static final MapCodec<FloatSchema> MAP_CODEC = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        Codec.FLOAT.optionalFieldOf("min").forGetter(FloatSchema::min),
                        Codec.FLOAT.optionalFieldOf("max").forGetter(FloatSchema::max)
                ).apply(inst, FloatSchema::new)
        );
    }

    record EnumSchema(List<String> validValues) implements TagValueSchema<String> {
        @Override public String type() { return "enum"; }

        @Override
        public String parse(String raw) throws CommandSyntaxException {
            if (!validValues.contains(raw))
                throw new CommandSyntaxException(null, Text.literal("Must be one of " + validValues));
            return raw;
        }

        @Override
        public List<String> suggest(String prefix) {
            return validValues.stream()
                    .filter(v -> v.startsWith(prefix))
                    .toList();
        }

        @Override
        public Tag<String> createTag(Identifier identifier, Object value, TagDefinition definition) {
            if (!(value instanceof String)) {
                throw new IllegalArgumentException("Value must be a String for EnumSchema");
            }
            return new Tag<>(identifier, (String) value, definition);
        }

        public static final MapCodec<EnumSchema> MAP_CODEC = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        Codec.list(Codec.STRING)
                                .fieldOf("valid_values")
                                .forGetter(EnumSchema::validValues)
                ).apply(inst, EnumSchema::new)
        );
    }
}