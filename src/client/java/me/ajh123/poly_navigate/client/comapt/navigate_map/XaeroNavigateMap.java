package me.ajh123.poly_navigate.client.comapt.navigate_map;

import me.ajh123.poly_navigate.client.mixin.compat.XaeroFullscreenMapAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import xaero.map.WorldMap;
import xaero.map.gui.GuiMap;

public class XaeroNavigateMap {
    // Correct: use the public static factory method
    private static final Identifier ICON = Identifier.of(
            "poly_navigate",
            "textures/gui/waypoint_icon.png"
    );
    private static final int ICON_SIZE = 16;

    public static void onRender(DrawContext graphics,
                                GuiMap screen,
                                int scaledMouseX,
                                int scaledMouseY,
                                float partialTicks) {
        MinecraftClient mc = MinecraftClient.getInstance();
        XaeroFullscreenMapAccessor acc = (XaeroFullscreenMapAccessor) screen;
        Window window = mc.getWindow();
        MatrixStack ms = graphics.getMatrices();

        ms.push();

        // 1) undo Xaero's built‑in scale
        ms.scale((float)(1.0D / acc.getScale()),
                (float)(1.0D / acc.getScreenScale()),
                1.0F);
        // 2) center the map on screen
        ms.translate(window.getWidth()  / 2.0,
                window.getHeight() / 2.0,
                0);
        // 3) re‑apply Xaero’s zoom
        ms.scale((float) acc.getScale(),
                (float) acc.getScale(),
                1.0F);
        // 4) apply GUI‑scale tweak + user waypoint‑size setting
        double minGui = 4.0D;
        double guiFactor = acc.getScreenScale() > minGui
                ? acc.getScreenScale() / minGui
                : 1.0D;
        double mapScale = acc.getScale()
                * guiFactor
                * WorldMap.settings.worldmapWaypointsScale;
        ms.scale((float) mapScale,
                (float) mapScale,
                1.0F);

        // 5) draw at world (0,0) on the map (replace with your coords)
        renderPoint(graphics, acc, window.getWidth(), window.getHeight(),
                /* worldX= */ 0.0, /* worldZ= */ 0.0);

        ms.pop();
    }

    private static void renderPoint(DrawContext graphics,
                                    XaeroFullscreenMapAccessor acc,
                                    int windowW,
                                    int windowH,
                                    double worldX,
                                    double worldZ) {
        // world→map‑pixel delta (all scaling is already applied)
        double dx = worldX - acc.getCameraX();
        double dz = worldZ - acc.getCameraZ();

        // map is centered at (windowW/2, windowH/2)
        int centerX = windowW  / 2;
        int centerY = windowH  / 2;

        // compute final on‑screen pixel (center icon)
        int px = (int)Math.round(centerX + dx - ICON_SIZE / 2.0);
        int py = (int)Math.round(centerY + dz - ICON_SIZE / 2.0);

        // blit your 16×16 icon
        graphics.drawTexture(
                ICON,
                px, py,              // on‑screen x, y
                0, 0,                // u, v in the texture
                ICON_SIZE, ICON_SIZE // width, height
        );
    }
}
