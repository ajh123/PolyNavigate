package me.ajh123.poly_navigate.commands.template;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.ajh123.poly_navigate.map_object.MapDataRegistry;
import me.ajh123.poly_navigate.map_object.MapObjectTemplate;
import me.ajh123.poly_navigate.map_object.MapObjectType;
import me.ajh123.poly_navigate.map_object.TagDefinition;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class TemplateSpecArgumentType implements ArgumentType<TemplateSpec> {
    private static final DynamicCommandExceptionType UNKNOWN_TEMPLATE =
            new DynamicCommandExceptionType(id -> Text.literal("Unknown template: " + id));
    private static final DynamicCommandExceptionType BAD_SYNTAX =
            new DynamicCommandExceptionType(ctx -> Text.literal("Invalid syntax near '" + ctx + "'"));
    private static final DynamicCommandExceptionType UNKNOWN_TAG =
            new DynamicCommandExceptionType(tag -> Text.literal("Unknown tag: " + tag));
    private static final DynamicCommandExceptionType BAD_VALUE =
            new DynamicCommandExceptionType(msg -> Text.literal("Invalid value: " + msg));

    private final MapObjectType type;

    protected TemplateSpecArgumentType(MapObjectType type) {
        this.type = type;
    }

    @Override
    public TemplateSpec parse(StringReader reader) throws CommandSyntaxException {
        // 1) parse template ID
        Identifier tmplId = Identifier.fromCommandInput(reader);
        Map<Identifier, MapObjectTemplate> all = (type == MapObjectType.NODE) ?  MapDataRegistry.getNodeTemplates() : MapDataRegistry.getWayTemplates();
        if (!all.containsKey(tmplId)) {
            throw UNKNOWN_TEMPLATE.createWithContext(reader, tmplId.toString());
        }
        MapObjectTemplate tmpl = all.get(tmplId);

        // 2) if next is '[', parse tag assignments
        Map<Identifier, String> values = new LinkedHashMap<>();
        if (reader.canRead() && reader.peek() == '[') {
            reader.skip(); // consume '['
            while (true) {
                // parse tagId
                int keyStart = reader.getCursor();
                Identifier tagId = Identifier.fromCommandInput(reader);

                // must be allowed
                boolean inTemplate = tmpl.tags().contains(tagId) || tmpl.optionalTags().contains(tagId);
                // ...and also actually registered in the global tag registry:
                boolean registered  = MapDataRegistry.getMapObjectTags().containsKey(tagId);

                if (!inTemplate || !registered) {
                    throw UNKNOWN_TAG.createWithContext(reader, tagId.toString());
                }

                if (reader.canRead() && reader.peek() == '=') {
                    reader.skip();
                } else {
                    throw BAD_SYNTAX.createWithContext(reader, reader.getString().substring(keyStart));
                }

                // parse value
                Object parsedValue;
                if (reader.canRead() && reader.peek() == '"') {
                    // quoted string
                    reader.skip();
                    int valStart = reader.getCursor();
                    while (reader.canRead() && reader.peek() != '"') {
                        reader.skip();
                    }
                    if (!reader.canRead()) {
                        throw BAD_SYNTAX.createWithContext(reader, reader.getString().substring(valStart));
                    }
                    String raw = reader.getString().substring(valStart, reader.getCursor());
                    reader.skip(); // closing quote
                    parsedValue = raw;
                } else {
                    // unquoted: read until ',' or ']'
                    int valStart = reader.getCursor();
                    while (reader.canRead() && reader.peek() != ',' && reader.peek() != ']') {
                        reader.skip();
                    }
                    String raw = reader.getString().substring(valStart, reader.getCursor());
                    // try bool
                    if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) {
                        parsedValue = Boolean.parseBoolean(raw);
                    }
                    // try integer
                    else if (raw.matches("-?\\d+")) {
                        parsedValue = Integer.parseInt(raw);
                    }
                    // try float
                    else if (raw.matches("-?\\d*\\.\\d+")) {
                        parsedValue = Float.parseFloat(raw);
                    }
                    else {
                        parsedValue = raw;
                    }
                }

                // validate against TagDefinition
                TagDefinition def = MapDataRegistry.getMapObjectTags().get(tagId);
                switch (def.type()) {
                    case "int" -> {
                        if (!(parsedValue instanceof Integer)) {
                            throw BAD_VALUE.createWithContext(reader, raw("expect int", parsedValue));
                        }
                        int i = (Integer)parsedValue;
                        try {
                            def.min().ifPresent(min -> {
                                if (i < min) try {
                                    throw BAD_VALUE.create(min);
                                } catch (CommandSyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            def.max().ifPresent(max -> {
                                if (i > max) try {
                                    throw BAD_VALUE.create(max);
                                } catch (CommandSyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (RuntimeException e) {
                            try {
                                throw e.getCause();
                            } catch (Throwable ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                    case "float" -> {
                        if (!(parsedValue instanceof Float)) {
                            throw BAD_VALUE.createWithContext(reader, raw("expect float", parsedValue));
                        }
                        float i = (Float)parsedValue;
                        try {
                            def.min().ifPresent(min -> {
                                if (i < min) try {
                                    throw BAD_VALUE.create(min);
                                } catch (CommandSyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            def.max().ifPresent(max -> {
                                if (i > max) try {
                                    throw BAD_VALUE.create(max);
                                } catch (CommandSyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (RuntimeException e) {
                            try {
                                throw e.getCause();
                            } catch (Throwable ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                    case "bool" -> {
                        if (!(parsedValue instanceof Boolean)) {
                            throw BAD_VALUE.createWithContext(reader, raw("expect bool", parsedValue));
                        }
                    }
                    default -> {
                        // string: optionally check validValues()
                        if (!def.validValues().isEmpty()
                                && !def.validValues().contains(parsedValue)) {
                            throw BAD_VALUE.createWithContext(reader, raw("expect one of " + def.validValues(), parsedValue));
                        }
                    }
                }

                values.put(tagId, String.valueOf(parsedValue));

                // if comma, continue; if ']', break
                if (reader.canRead() && reader.peek() == ',') {
                    reader.skip();
                    continue;
                }
                if (reader.canRead() && reader.peek() == ']') {
                    reader.skip();
                    break;
                }
                throw BAD_SYNTAX.createWithContext(reader, reader.getRemaining());
            }
        }

        return new TemplateSpec(tmplId, values);
    }

    private static String raw(String expect, Object got) {
        return expect + ", got " + got;
    }

    /** Suggest both template IDs (before the '[') and tags/values inside the brackets. */
    public static SuggestionProvider<ServerCommandSource> suggestTemplatesAndTags(MapObjectType type) {
        return (ctx, builder) -> {
            Map<Identifier, MapObjectTemplate> all = (type == MapObjectType.NODE) ?  MapDataRegistry.getNodeTemplates() : MapDataRegistry.getWayTemplates();

            String full = builder.getRemaining();
            int bracketIdx = full.indexOf('[');

            // 1) before '[' → suggest template IDs
            if (bracketIdx < 0) {
                for (Identifier id : all.keySet()) {
                    String s = id.toString();
                    if (s.startsWith(full)) builder.suggest(s);
                }
                return builder.buildFuture();
            }

            // isolate what's inside the brackets
            String insideAll = full.substring(bracketIdx + 1);
            String[] segments     = insideAll.split(",", -1);
            String lastSeg        = segments[segments.length - 1];

            // compute where the last segment starts in the overall input
            int segStartOffset = builder.getStart() + bracketIdx + 1
                    + Arrays.stream(segments, 0, segments.length - 1)
                    .mapToInt(String::length)
                    .sum()
                    + (segments.length - 1);

            int eqIdx = lastSeg.indexOf('=');

            // 2a) Tag‑key suggestions (no '=' in this segment)
            if (eqIdx < 0) {
                SuggestionsBuilder keyBuilder = builder.createOffset(segStartOffset);

                String tmplPart = full.substring(0, bracketIdx);
                Identifier tmplId = Identifier.tryParse(tmplPart);
                MapObjectTemplate tmpl = tmplId == null
                        ? null
                        : all.get(tmplId);
                var globalTags = MapDataRegistry.getMapObjectTags();

                if (tmpl != null) {
                    for (Identifier tagId : tmpl.tags()) {
                        if (!globalTags.containsKey(tagId)) continue;
                        String token = tagId + "=";
                        if (!insideAll.contains(token)) keyBuilder.suggest(token);
                    }
                    for (Identifier tagId : tmpl.optionalTags()) {
                        if (!globalTags.containsKey(tagId)) continue;
                        String token = tagId + "=";
                        if (!insideAll.contains(token)) keyBuilder.suggest(token);
                    }
                }
                return keyBuilder.buildFuture();
            }

            // 2b) Tag‑value suggestions
            // offset just after the '=' of the last segment
            int valueOffset = segStartOffset + eqIdx + 1;
            SuggestionsBuilder valueBuilder = builder.createOffset(valueOffset);

            String keyPart = lastSeg.substring(0, eqIdx);
            String prefix  = lastSeg.substring(eqIdx + 1);
            Identifier tagId = Identifier.tryParse(keyPart);
            TagDefinition def = tagId == null
                    ? null
                    : MapDataRegistry.getMapObjectTags().get(tagId);

            if (def != null) {
                // Suggest matching values normally (replace prefix)
                for (String v : def.validValues()) {
                    if (prefix.isEmpty() || v.startsWith(prefix)) {
                        valueBuilder.suggest(v);
                    }
                }

                // If exact match on a value, suggest separators
                if (def.validValues().contains(prefix)) {
                    // Use a builder offset at the *end* of the current input for separators,
                    // so the separator inserts after the value instead of replacing it.
                    SuggestionsBuilder sepBuilder = builder.createOffset(builder.getStart() + builder.getRemaining().length());

                    sepBuilder.suggest(",");
                    sepBuilder.suggest("]");
                    return sepBuilder.buildFuture();
                }
            }

            return valueBuilder.buildFuture();
        };
    }
}