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
    private static final float OUTLINE = 1.0F;

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

        float dot = dotSize();
        int columns = pattern.getColumns();
        int rows = pattern.getRowCount();
        float originX = Math.round(centerX - columns * dot / 2.0F);
        float originY = Math.round(centerY - rows * dot / 2.0F);

        int alpha = (int)(CrosshairState.getAlpha() * 255.0F);
        if (alpha <= 0)
        {
            return;
        }

        boolean outline = AimSettings.crosshairOutline;
        boolean gradient = AimSettings.gradientEnabled && !outline;
        int color = alpha << 24 | AimSettings.getColor(type.name());
        float phase = gradient ? gradientPhase() : 0.0F;

        begin();

        if (outline)
        {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer renderer = tessellator.getWorldRenderer();
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            drawSolid(renderer, pattern, originX, originY, dot, alpha << 24, OUTLINE);
            tessellator.draw();
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        if (gradient)
        {
            drawGradient(renderer, pattern, originX, originY, dot, alpha, phase);
        }
        else
        {
            drawSolid(renderer, pattern, originX, originY, dot, color, 0.0F);
        }
        tessellator.draw();

        end();
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
        int alphaByte = (int)(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
        if (alphaByte <= 0)
        {
            return;
        }

        boolean outline = AimSettings.crosshairOutline;
        boolean gradient = AimSettings.gradientEnabled && !outline;
        int color = alphaByte << 24 | AimSettings.getColor(type.name());

        begin();

        if (outline)
        {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer renderer = tessellator.getWorldRenderer();
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            drawSolid(renderer, pattern, originX, originY, dot, alphaByte << 24, OUTLINE);
            tessellator.draw();
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        if (gradient)
        {
            // Static phase in the preview so the chosen direction stays readable.
            drawGradient(renderer, pattern, originX, originY, dot, alphaByte, 0.0F);
        }
        else
        {
            drawSolid(renderer, pattern, originX, originY, dot, color, 0.0F);
        }
        tessellator.draw();

        end();
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

    private static float dotSize()
    {
        return Math.max(1.0F, (AimSettings.crosshairSize / 100.0F) * CrosshairState.getScale());
    }

    private static void begin()
    {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void end()
    {
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawSolid(WorldRenderer renderer, CrosshairPattern pattern, float originX, float originY, float dot, int color, float expand)
    {
        int columns = pattern.getColumns();
        int rows = pattern.getRowCount();

        for (int row = 0; row < rows; row++)
        {
            for (int column = 0; column < columns; column++)
            {
                if (!pattern.isDot(column, row))
                {
                    continue;
                }

                float x = originX + column * dot - expand;
                float y = originY + row * dot - expand;
                float size = dot + expand * 2.0F;
                addQuad(renderer, x, y, x + size, y + size, color);
            }
        }
    }

    private static void drawGradient(WorldRenderer renderer, CrosshairPattern pattern, float originX, float originY, float dot, int alpha, float phase)
    {
        int columns = pattern.getColumns();
        int rows = pattern.getRowCount();
        float width = columns * dot;
        float height = rows * dot;

        for (int row = 0; row < rows; row++)
        {
            for (int column = 0; column < columns; column++)
            {
                if (!pattern.isDot(column, row))
                {
                    continue;
                }

                float x0 = originX + column * dot;
                float y0 = originY + row * dot;
                float x1 = x0 + dot;
                float y1 = y0 + dot;
                int topLeft = gradientColor(originX, originY, width, height, x0, y0, phase, alpha);
                int topRight = gradientColor(originX, originY, width, height, x1, y0, phase, alpha);
                int bottomRight = gradientColor(originX, originY, width, height, x1, y1, phase, alpha);
                int bottomLeft = gradientColor(originX, originY, width, height, x0, y1, phase, alpha);
                addGradientQuad(renderer, x0, y0, x1, y1, topLeft, topRight, bottomRight, bottomLeft);
            }
        }
    }

    private static int gradientColor(float left, float top, float width, float height, float x, float y, float phase, int alpha)
    {
        float factor;

        switch (AimSettings.gradientDirection)
        {
            case AimSettings.GRADIENT_RIGHT_TO_LEFT:
                factor = 1.0F - axisFactor(x, left, width);
                break;
            case AimSettings.GRADIENT_TOP_TO_BOTTOM:
                factor = axisFactor(y, top, height);
                break;
            case AimSettings.GRADIENT_BOTTOM_TO_TOP:
                factor = 1.0F - axisFactor(y, top, height);
                break;
            case AimSettings.GRADIENT_CENTER_OUT:
                factor = radialFactor(left, top, width, height, x, y);
                break;
            case AimSettings.GRADIENT_EDGE_IN:
                factor = 1.0F - radialFactor(left, top, width, height, x, y);
                break;
            default:
                factor = axisFactor(x, left, width);
                break;
        }

        // Subtracting the phase makes the moving band travel from the A side to the B side,
        // so "left to right", "top to bottom" and "center out" also read correctly while animated.
        factor -= phase;
        factor -= (float)Math.floor(factor);
        factor = Math.max(0.0F, Math.min(1.0F, factor));

        int from = AimSettings.gradientColorA;
        int to = AimSettings.gradientColorB;
        int red = lerpChannel(from >> 16 & 255, to >> 16 & 255, factor);
        int green = lerpChannel(from >> 8 & 255, to >> 8 & 255, factor);
        int blue = lerpChannel(from & 255, to & 255, factor);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static float axisFactor(float value, float start, float length)
    {
        if (length <= 0.0F)
        {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, (value - start) / length));
    }

    private static float radialFactor(float left, float top, float width, float height, float x, float y)
    {
        float centerX = left + width / 2.0F;
        float centerY = top + height / 2.0F;
        float dx = x - centerX;
        float dy = y - centerY;
        float max = (float)Math.sqrt(width * width + height * height) / 2.0F;
        if (max <= 0.0F)
        {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, (float)Math.sqrt(dx * dx + dy * dy) / max));
    }

    private static float gradientPhase()
    {
        int speed = AimSettings.gradientSpeed;
        if (speed <= 0)
        {
            return 0.0F;
        }

        float seconds = (System.currentTimeMillis() % 600000L) / 1000.0F;
        float phase = seconds * (speed / 100.0F);
        return phase - (float)Math.floor(phase);
    }

    private static int lerpChannel(int from, int to, float amount)
    {
        return (int)(from + (to - from) * amount);
    }

    private static void addQuad(WorldRenderer renderer, float x0, float y0, float x1, float y1, int color)
    {
        renderer.pos(x0, y1, 0.0D).color(color >>> 16 & 255, color >>> 8 & 255, color & 255, color >>> 24).endVertex();
        renderer.pos(x1, y1, 0.0D).color(color >>> 16 & 255, color >>> 8 & 255, color & 255, color >>> 24).endVertex();
        renderer.pos(x1, y0, 0.0D).color(color >>> 16 & 255, color >>> 8 & 255, color & 255, color >>> 24).endVertex();
        renderer.pos(x0, y0, 0.0D).color(color >>> 16 & 255, color >>> 8 & 255, color & 255, color >>> 24).endVertex();
    }

    private static void addGradientQuad(WorldRenderer renderer, float x0, float y0, float x1, float y1,
            int topLeft, int topRight, int bottomRight, int bottomLeft)
    {
        vertex(renderer, x0, y1, bottomLeft);
        vertex(renderer, x1, y1, bottomRight);
        vertex(renderer, x1, y0, topRight);
        vertex(renderer, x0, y0, topLeft);
    }

    private static void vertex(WorldRenderer renderer, float x, float y, int color)
    {
        renderer.pos(x, y, 0.0D).color(color >>> 16 & 255, color >>> 8 & 255, color & 255, color >>> 24).endVertex();
    }
}
