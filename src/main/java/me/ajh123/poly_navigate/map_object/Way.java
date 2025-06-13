package me.ajh123.poly_navigate.map_object;

import java.util.List;

public record Way(long id, List<Tag<?>> tags) {
    public Way(long id) {
        this(id, List.of());
    }

    public Way(long id, List<Tag<?>> tags) {
        this.id = id;
        this.tags = tags;
    }
}
