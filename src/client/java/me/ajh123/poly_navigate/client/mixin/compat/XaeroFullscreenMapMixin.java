package me.ajh123.poly_navigate.client.mixin.compat;

import me.ajh123.poly_navigate.PolyNavigate;
import net.minecraft.client.gui.DrawContext;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xaero.map.gui.GuiMap;

import me.ajh123.poly_navigate.client.comapt.navigate_map.XaeroNavigateMap;


@Mixin(GuiMap.class)
public class XaeroFullscreenMapMixin {
    @Unique
    boolean poly_navigate$failedToRenderNavigateMap = true;

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"), require = 0)
    @SuppressWarnings("DataFlowIssue") // we are allowed to cast to GuiMap during runtime as the mixin is removed and injected into the class
    public void create$xaeroMapFullscreenRender(DrawContext graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        try {
            if(!poly_navigate$failedToRenderNavigateMap)
                XaeroNavigateMap.onRender(graphics, (GuiMap) (Object) this, mouseX, mouseY, partialTicks);
        } catch (Exception e) {
            PolyNavigate.LOGGER.severe("Failed to render Xaero's World Map Poly Navigate Map integration: \n %s".formatted(e.toString()));
            poly_navigate$failedToRenderNavigateMap = true;
        }
    }
}
