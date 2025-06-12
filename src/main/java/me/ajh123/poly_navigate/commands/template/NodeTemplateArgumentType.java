package me.ajh123.poly_navigate.commands.template;

import me.ajh123.poly_navigate.map_object.MapObjectType;

public class NodeTemplateArgumentType extends TemplateSpecArgumentType {
    public NodeTemplateArgumentType() {
        super(MapObjectType.NODE);
    }

    public static NodeTemplateArgumentType spec() {
        return new NodeTemplateArgumentType();
    }
}
