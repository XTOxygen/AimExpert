package com.endwithme.aimexpert.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RenderUtils
{
    private RenderUtils()
    {
    }

    public static void rect(float x, float y, float width, float height, int color)
    {
        roundedRect(x, y, width, height, 0.0F, color, color);
    }

    public static void roundedRect(float x, float y, float width, float height, float radius, int color)
    {
        roundedRect(x, y, width, height, radius, color, color);
    }

    public static void roundedRect(float x, float y, float width, float height, float radius, int topColor, int bottomColor)
    {
        if (width <= 0.0F || height <= 0.0F)
        {
            return;
        }

        radius = Math.max(0.0F, Math.min(radius, Math.min(width, height) / 2.0F));
        Gradient gradient = new Gradient(y, y + height, topColor, bottomColor);

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        if (radius <= 0.05F)
        {
            quad(renderer, x, y, x + width, y + height, gradient);
        }
        else
        {
            quad(renderer, x + radius, y, x + width - radius, y + height, gradient);
            quad(renderer, x, y + radius, x + radius, y + height - radius, gradient);
            quad(renderer, x + width - radius, y + radius, x + width, y + height - radius, gradient);
            corner(renderer, x + radius, y + radius, radius, 180.0F, 270.0F, gradient);
            corner(renderer, x + width - radius, y + radius, radius, 270.0F, 360.0F, gradient);
            corner(renderer, x + width - radius, y + height - radius, radius, 0.0F, 90.0F, gradient);
            corner(renderer, x + radius, y + height - radius, radius, 90.0F, 180.0F, gradient);
        }

        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void roundedOutline(float x, float y, float width, float height, float radius, float thickness, int color)
    {
        if (width <= 0.0F || height <= 0.0F || thickness <= 0.0F)
        {
            return;
        }

        radius = Math.max(0.0F, Math.min(radius, Math.min(width, height) / 2.0F));
        thickness = Math.min(thickness, Math.min(width, height) / 2.0F);
        Gradient gradient = new Gradient(y, y + height, color, color);

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        quad(renderer, x + radius, y, x + width - radius, y + thickness, gradient);
        quad(renderer, x + radius, y + height - thickness, x + width - radius, y + height, gradient);
        quad(renderer, x, y + radius, x + thickness, y + height - radius, gradient);
        quad(renderer, x + width - thickness, y + radius, x + width, y + height - radius, gradient);
        strokeCorner(renderer, x + radius, y + radius, radius, thickness, 180.0F, 270.0F, gradient);
        strokeCorner(renderer, x + width - radius, y + radius, radius, thickness, 270.0F, 360.0F, gradient);
        strokeCorner(renderer, x + width - radius, y + height - radius, radius, thickness, 0.0F, 90.0F, gradient);
        strokeCorner(renderer, x + radius, y + height - radius, radius, thickness, 90.0F, 180.0F, gradient);

        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void texturedRect(int texture, float x, float y, float width, float height, float u0, float v0, float u1, float v1)
    {
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.bindTexture(texture);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        renderer.pos(x, y + height, 0.0D).tex(u0, v1).endVertex();
        renderer.pos(x + width, y + height, 0.0D).tex(u1, v1).endVertex();
        renderer.pos(x + width, y, 0.0D).tex(u1, v0).endVertex();
        renderer.pos(x, y, 0.0D).tex(u0, v0).endVertex();
        tessellator.draw();
    }

    public static void startScissor(int x, int y, int width, int height)
    {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc);
        int factor = resolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * factor, mc.displayHeight - (y + height) * factor, Math.max(0, width * factor), Math.max(0, height * factor));
    }

    public static void endScissor()
    {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public static int withAlpha(int color, float alpha)
    {
        int a = (int)(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
        return (a << 24) | (color & 0xFFFFFF);
    }

    public static int lerpColor(int from, int to, float amount)
    {
        amount = Math.max(0.0F, Math.min(1.0F, amount));
        int a = lerpChannel(from >>> 24, to >>> 24, amount);
        int r = lerpChannel(from >> 16 & 255, to >> 16 & 255, amount);
        int g = lerpChannel(from >> 8 & 255, to >> 8 & 255, amount);
        int b = lerpChannel(from & 255, to & 255, amount);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int multiplyAlpha(int color, float factor)
    {
        int a = (int)((color >>> 24) * Math.max(0.0F, Math.min(1.0F, factor)));
        return a << 24 | color & 0xFFFFFF;
    }

    private static int lerpChannel(int from, int to, float amount)
    {
        return (int)(from + (to - from) * amount);
    }

    private static void quad(WorldRenderer renderer, float x0, float y0, float x1, float y1, Gradient gradient)
    {
        vertex(renderer, x0, y1, gradient.at(y1));
        vertex(renderer, x1, y1, gradient.at(y1));
        vertex(renderer, x1, y0, gradient.at(y0));
        vertex(renderer, x0, y0, gradient.at(y0));
    }

    private static void corner(WorldRenderer renderer, float centerX, float centerY, float radius, float from, float to, Gradient gradient)
    {
        int segments = Math.max(2, (int)Math.ceil(radius));
        for (int i = 0; i < segments; i++)
        {
            float a0 = (float)Math.toRadians(from + (to - from) * i / segments);
            float a1 = (float)Math.toRadians(from + (to - from) * (i + 1) / segments);
            float x0 = centerX + radius * (float)Math.cos(a0);
            float y0 = centerY + radius * (float)Math.sin(a0);
            float x1 = centerX + radius * (float)Math.cos(a1);
            float y1 = centerY + radius * (float)Math.sin(a1);
            vertex(renderer, centerX, centerY, gradient.at(centerY));
            vertex(renderer, x1, y1, gradient.at(y1));
            vertex(renderer, x0, y0, gradient.at(y0));
            vertex(renderer, x0, y0, gradient.at(y0));
        }
    }

    private static void strokeCorner(WorldRenderer renderer, float centerX, float centerY, float radius, float thickness, float from, float to, Gradient gradient)
    {
        float inner = Math.max(0.0F, radius - thickness);
        int segments = Math.max(2, (int)Math.ceil(radius));
        for (int i = 0; i < segments; i++)
        {
            float a0 = (float)Math.toRadians(from + (to - from) * i / segments);
            float a1 = (float)Math.toRadians(from + (to - from) * (i + 1) / segments);
            float cos0 = (float)Math.cos(a0);
            float sin0 = (float)Math.sin(a0);
            float cos1 = (float)Math.cos(a1);
            float sin1 = (float)Math.sin(a1);
            float ix0 = centerX + inner * cos0;
            float iy0 = centerY + inner * sin0;
            float ix1 = centerX + inner * cos1;
            float iy1 = centerY + inner * sin1;
            float ox0 = centerX + radius * cos0;
            float oy0 = centerY + radius * sin0;
            float ox1 = centerX + radius * cos1;
            float oy1 = centerY + radius * sin1;
            vertex(renderer, ix0, iy0, gradient.at(iy0));
            vertex(renderer, ix1, iy1, gradient.at(iy1));
            vertex(renderer, ox1, oy1, gradient.at(oy1));
            vertex(renderer, ox0, oy0, gradient.at(oy0));
        }
    }

    private static void vertex(WorldRenderer renderer, float x, float y, int color)
    {
        renderer.pos(x, y, 0.0D).color(color >>> 16 & 255, color >>> 8 & 255, color & 255, color >>> 24).endVertex();
    }

    private static final class Gradient
    {
        private final float top;
        private final float bottom;
        private final int topColor;
        private final int bottomColor;

        private Gradient(float top, float bottom, int topColor, int bottomColor)
        {
            this.top = top;
            this.bottom = bottom;
            this.topColor = topColor;
            this.bottomColor = bottomColor;
        }

        private int at(float y)
        {
            if (this.topColor == this.bottomColor)
            {
                return this.topColor;
            }

            float span = this.bottom - this.top;
            float amount = span <= 0.0F ? 0.0F : (y - this.top) / span;
            return lerpColor(this.topColor, this.bottomColor, amount);
        }
    }
}
