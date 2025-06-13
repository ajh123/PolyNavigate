package me.ajh123.poly_navigate.map_object;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public record Node(long id, BlockPos pos, List<Tag<?>> tags) {
    public Node(long id, BlockPos pos) {
        this(id, pos, List.of());
    }

    public Node(long id, BlockPos pos, List<Tag<?>> tags) {
        this.id = id;
        this.pos = pos;
        this.tags = tags;
    }
}
