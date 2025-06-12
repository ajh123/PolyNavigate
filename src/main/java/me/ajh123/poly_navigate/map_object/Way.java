package me.ajh123.poly_navigate.map_object;

import java.util.Map;

public record Way(long id, Map<String, String> tags) {
    public Way(long id) {
        this(id, Map.of());
    }

    public Way(long id, Map<String, String> tags) {
        this.id = id;
        this.tags = tags;
    }
}
