package me.ajh123.poly_navigate.client.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.gen.Accessor;

import xaero.map.MapProcessor;
import xaero.map.gui.GuiMap;

@Mixin(GuiMap.class)
public interface XaeroFullscreenMapAccessor {
    @Accessor(remap = false)
    double getCameraX();

    @Accessor(remap = false)
    double getCameraZ();

    @Accessor(remap = false)
    int getRightClickX();

    @Accessor(remap = false)
    int getRightClickY();

    @Accessor(remap = false)
    int getRightClickZ();

    @Accessor(remap = false)
    double getScale();

    @Accessor(remap = false)
    double getScreenScale();

    @Accessor(remap = false)
    MapProcessor getMapProcessor();
}