package com.endwithme.aimexpert.render;

import org.lwjgl.opengl.GL11;

import com.endwithme.aimexpert.config.AimSettings;
import com.endwithme.aimexpert.crosshair.CrosshairPattern;
import com.endwithme.aimexpert.crosshair.CrosshairState;
import com.endwithme.aimexpert.crosshair.CrosshairType;
import com.endwithme.aimexpert.crosshair.TemplateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class CrosshairRenderer
{
    private CrosshairRenderer()
    {
    }

    public static void render(CrosshairType type, float centerX, float centerY)
    {
        CrosshairPattern pattern = TemplateManager.getActive().getPattern(type);
        if (pattern.isEmpty())
        {
            return;
        }

        float unit = (AimSettings.crosshairSize / 100.0F) * CrosshairState.getScale();
        float dot = Math.max(1.0F, unit);
        int columns = pattern.getColumns();
        int rows = pattern.getRowCount();
        float originX = Math.round(centerX - columns * dot / 2.0F);
        float originY = Math.round(centerY - rows * dot / 2.0F);

        int rgb = AimSettings.getColor(type.name());
        int alpha = (int)(CrosshairState.getAlpha() * 255.0F);
        if (alpha <= 0)
        {
            return;
        }
        int color = alpha << 24 | rgb;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (int row = 0; row < rows; row++)
        {
            for (int column = 0; column < columns; column++)
            {
                if (!pattern.isDot(column, row))
                {
                    continue;
                }

                float x = originX + column * dot;
                float y = originY + row * dot;
                addQuad(renderer, x, y, x + dot, y + dot, color);
            }
        }

        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** Preview used by the GUI, drawn in the current GUI coordinate space. */
    public static void renderPreview(CrosshairType type, float centerX, float centerY, float scale, float alpha)
    {
        CrosshairPattern pattern = TemplateManager.getActive().getPattern(type);
        if (pattern.isEmpty())
        {
            return;
        }

        float dot = Math.max(1.0F, scale);
        int columns = pattern.getColumns();
        int rows = pattern.getRowCount();
        float originX = Math.round(centerX - columns * dot / 2.0F);
        float originY = Math.round(centerY - rows * dot / 2.0F);
        int color = (int)(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F) << 24 | AimSettings.getColor(type.name());

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (int row = 0; row < rows; row++)
        {
            for (int column = 0; column < columns; column++)
            {
                if (!pattern.isDot(column, row))
                {
                    continue;
                }

                float x = originX + column * dot;
                float y = originY + row * dot;
                addQuad(renderer, x, y, x + dot, y + dot, color);
            }
        }

        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static int getPreviewSize(CrosshairType type)
    {
        CrosshairPattern pattern = TemplateManager.getActive().getPattern(type);
        return Math.max(pattern.getColumns(), pattern.getRowCount());
    }

    public static boolean hasCrosshair()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.gameSettings.hideGUI || mc.gameSettings.showDebugInfo)
        {
            return false;
        }

        if (AimSettings.hideCrosshair)
        {
            return false;
        }

        return AimSettings.useCustomCrosshair;
    }

    private static void addQuad(WorldRenderer renderer, float x0, float y0, float x1, float y1, int color)
    {
        renderer.pos(x0, y1, 0.0D).color(color >>> 16 & 255, color >>> 8 & 255, color & 255, color >>> 24).endVertex();
        renderer.pos(x1, y1, 0.0D).color(color >>> 16 & 255, color >>> 8 & 255, color & 255, color >>> 24).endVertex();
        renderer.pos(x1, y0, 0.0D).color(color >>> 16 & 255, color >>> 8 & 255, color & 255, color >>> 24).endVertex();
        renderer.pos(x0, y0, 0.0D).color(color >>> 16 & 255, color >>> 8 & 255, color & 255, color >>> 24).endVertex();
    }
}
