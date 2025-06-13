package me.ajh123.poly_navigate.commands.template;

import net.minecraft.util.Identifier;
import java.util.Map;

public record TemplateSpec(
        Identifier templateId,
        Map<Identifier, String> tagValues
) {}