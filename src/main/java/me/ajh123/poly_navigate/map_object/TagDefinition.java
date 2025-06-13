package me.ajh123.poly_navigate.map_object;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TagDefinition(
        String description,
        String osmName,
        TagValueSchema<?> schema
) {
    public static final Codec<TagDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("description").forGetter(TagDefinition::description),
                    Codec.STRING.fieldOf("osm_name").forGetter(TagDefinition::osmName),
                    TagValueSchema.CODEC.fieldOf("schema").forGetter(TagDefinition::schema)
            ).apply(instance, TagDefinition::new)
    );
}
