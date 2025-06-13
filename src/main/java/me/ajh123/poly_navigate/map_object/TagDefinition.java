package me.ajh123.poly_navigate.map_object;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record TagDefinition(
        String description,
        String osmName,
        TagSchema schema
) {
    public static final Codec<TagDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("description").forGetter(TagDefinition::description),
                    Codec.STRING.fieldOf("osm_name").forGetter(TagDefinition::osmName),
                    TagSchema.CODEC.fieldOf("schema").forGetter(TagDefinition::schema)
            ).apply(instance, TagDefinition::new)
    );

    public record TagSchema(
            String type,
            List<String> validValues,
            Optional<Integer> min,
            Optional<Integer> max
    ) {
        public static final Codec<TagSchema> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("type").forGetter(TagSchema::type),
                        Codec.list(Codec.STRING).optionalFieldOf("valid_values", List.of()).forGetter(TagSchema::validValues),
                        Codec.INT.optionalFieldOf("min").forGetter(TagSchema::min),
                        Codec.INT.optionalFieldOf("max").forGetter(TagSchema::max)
                ).apply(instance, TagSchema::new)
        );
    }
}
