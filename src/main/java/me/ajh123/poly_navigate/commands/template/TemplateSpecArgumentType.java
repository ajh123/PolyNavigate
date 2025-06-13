package me.ajh123.poly_navigate.commands.template;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.ajh123.poly_navigate.map_object.*;
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
    private static final DynamicCommandExceptionType MISSING_REQUIRED_TAG =
            new DynamicCommandExceptionType(tag -> Text.literal("Missing required tag: " + tag));

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
        List<Tag<?>> values = new ArrayList<>();
        if (reader.canRead() && reader.peek() == '[') {
            reader.skip(); // consume '['
            while (true) {
                // parse tagId
                int keyStart = reader.getCursor();
                Identifier tagId = Identifier.fromCommandInput(reader);

                // must be allowed
                boolean inTemplate = tmpl.requiredTags().contains(tagId) || tmpl.optionalTags().contains(tagId);
                // ...and also actually registered in the global tag registry:
                boolean registered = MapDataRegistry.getMapObjectTags().containsKey(tagId);

                if (!inTemplate || !registered) {
                    throw UNKNOWN_TAG.createWithContext(reader, tagId.toString());
                }

                if (reader.canRead() && reader.peek() == '=') {
                    reader.skip();
                } else {
                    throw BAD_SYNTAX.createWithContext(reader, reader.getString().substring(keyStart));
                }

                // parse value
                TagDefinition def = MapDataRegistry.getMapObjectTags().get(tagId);
                TagValueSchema<?> schema = def.schema();

                // read value token (quoted or unquoted)
                String rawValue;
                if (reader.canRead() && reader.peek() == '"') {
                    reader.skip(); // opening quote
                    int start = reader.getCursor();
                    while (reader.canRead() && reader.peek() != '"') {
                        reader.skip();
                    }
                    if (!reader.canRead()) {
                        throw BAD_SYNTAX.createWithContext(reader, reader.getString().substring(start));
                    }
                    rawValue = reader.getString().substring(start, reader.getCursor());
                    reader.skip(); // closing quote
                } else {
                    int start = reader.getCursor();
                    while (reader.canRead() && reader.peek() != ',' && reader.peek() != ']') {
                        reader.skip();
                    }
                    rawValue = reader.getString().substring(start, reader.getCursor());
                }

                Object parsedValue;
                try {
                    parsedValue = schema.parse(rawValue);
                } catch (CommandSyntaxException e) {
                    throw BAD_VALUE.createWithContext(reader, e.getMessage());
                }

                values.add(schema.createTag(tagId, parsedValue, def));

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

        // 3) ensure all required tags are present
        for (Identifier requiredTag : tmpl.requiredTags()) {
            if (values.stream().noneMatch(tag -> tag.identifier().equals(requiredTag))) {
                throw MISSING_REQUIRED_TAG.createWithContext(reader, requiredTag.toString());
            }
        }

        return new TemplateSpec(tmplId, values);
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
                    for (Identifier tagId : tmpl.requiredTags()) {
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
                TagValueSchema<?> schema = def.schema();
                List<String> suggestions = schema.suggest(prefix);
                for (String s : suggestions) {
                    valueBuilder.suggest(s);
                }

                // Suggest separators if the value looks complete
                if (prefix.startsWith("\"") && prefix.endsWith("\"") && prefix.length() > 1) {
                    SuggestionsBuilder sepBuilder = builder.createOffset(builder.getStart() + builder.getRemaining().length());
                    sepBuilder.suggest(",");
                    sepBuilder.suggest("]");
                    return sepBuilder.buildFuture();
                } else if (!suggestions.isEmpty() && suggestions.contains(prefix)) {
                    // Unquoted suggestions (like "true", "123", etc.) → also suggest end tokens
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