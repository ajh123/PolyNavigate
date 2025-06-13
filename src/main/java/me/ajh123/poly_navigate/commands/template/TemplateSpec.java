package me.ajh123.poly_navigate.commands.template;

import me.ajh123.poly_navigate.map_object.Tag;
import net.minecraft.util.Identifier;

import java.util.List;

public record TemplateSpec(
        Identifier templateId,
        List<Tag<?>> tagValues
) {}