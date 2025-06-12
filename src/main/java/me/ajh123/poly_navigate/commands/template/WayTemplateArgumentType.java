package me.ajh123.poly_navigate.commands.template;

import me.ajh123.poly_navigate.map_object.MapObjectType;

public class WayTemplateArgumentType extends TemplateSpecArgumentType {
    public WayTemplateArgumentType() {
        super(MapObjectType.WAY);
    }

    public static WayTemplateArgumentType spec() {
        return new WayTemplateArgumentType();
    }
}
