package me.ajh123.poly_navigate.map_object;

import net.minecraft.util.Identifier;

public record Tag<T>(Identifier identifier, T value, TagDefinition definition) {
}
