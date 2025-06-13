package me.ajh123.poly_navigate.map_object;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record MapObjectTemplate(
    MapObjectType type,
    List<Identifier> requiredTags,
    List<Identifier> optionalTags
) {
    public static final Codec<MapObjectTemplate> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            MapObjectType.CODEC.fieldOf("type").forGetter(MapObjectTemplate::type),
            Codec.list(Identifier.CODEC).fieldOf("required_tags").forGetter(MapObjectTemplate::requiredTags),
            Codec.list(Identifier.CODEC).optionalFieldOf("optional_tags", List.of()).forGetter(MapObjectTemplate::optionalTags)
        ).apply(instance, MapObjectTemplate::new)
    );
}
