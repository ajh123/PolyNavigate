package me.ajh123.poly_navigate.map_object;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;

public enum MapObjectType {
    NODE("node"),
    WAY("way");

    private final String type;

    MapObjectType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static final Codec<MapObjectType> CODEC = Codec.STRING.comapFlatMap(
            type -> {
                for (MapObjectType mapObjectType : MapObjectType.values()) {
                    if (mapObjectType.getType().equals(type)) {
                        return DataResult.success(mapObjectType);
                    }
                }
                return DataResult.error(() -> "Unknown MapObjectType: " + type + ". Valid types are: " +
                        String.join(", ", Arrays.stream(MapObjectType.values()).map(MapObjectType::getType).toList()));
            },
            MapObjectType::getType
    );
}
