package me.ajh123.poly_navigate.map_object;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record TagDefinition(
        String description,
        String type,
        List<String> validValues,
        Optional<Integer> min,
        Optional<Integer> max
) {
    public static final Codec<TagDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("description").forGetter(TagDefinition::description),
                    Codec.STRING.fieldOf("type").forGetter(TagDefinition::type),
                    Codec.list(Codec.STRING).optionalFieldOf("valid_values", List.of()).forGetter(TagDefinition::validValues),
                    Codec.INT.optionalFieldOf("min").forGetter(TagDefinition::min),
                    Codec.INT.optionalFieldOf("max").forGetter(TagDefinition::max)
            ).apply(instance, TagDefinition::new)
    );
}
