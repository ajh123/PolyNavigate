package me.ajh123.poly_navigate.map_object;

import net.minecraft.util.math.BlockPos;

import java.util.Map;

public record Node(long id, BlockPos pos, Map<String, String> tags) {
    public Node(long id, BlockPos pos) {
        this(id, pos, Map.of());
    }

    public Node(long id, BlockPos pos, Map<String, String> tags) {
        this.id = id;
        this.pos = pos;
        this.tags = tags;
    }
}
