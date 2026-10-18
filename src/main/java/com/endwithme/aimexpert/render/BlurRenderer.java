package com.endwithme.aimexpert.render;

import java.nio.IntBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.endwithme.aimexpert.config.AimSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Gaussian like background blur built from vanilla framebuffers only, so no shader pack,
 * no Optifine and no OpenGL 3 requirement is needed. The world image is captured while the
 * world framebuffer is still valid and blurred in a separate small chain of framebuffers.
 */
@SideOnly(Side.CLIENT)
public final class BlurRenderer
{
    private static final int MAX_LEVELS = 6;
    private static final int GL_FRAMEBUFFER_BINDING = 0x8CA6;
    private static final Logger LOGGER = LogManager.getLogger("AimExpert");

    private static final Framebuffer[] LEVELS = new Framebuffer[MAX_LEVELS];
    private static int levelCount = 0;
    private static int levelWidth = -1;
    private static int levelHeight = -1;
    private static boolean captured = false;
    private static boolean failed = false;
    private static boolean flipVertical = true;

    private BlurRenderer()
    {
    }

    public static boolean isFailed()
    {
        return failed;
    }

    public static void reset()
    {
        captured = false;
    }

    /** Captures and downsamples the world image. Must run while the world framebuffer holds the frame. */
    public static void capture()
    {
        captured = false;
        if (failed || !AimSettings.blurEnabled)
        {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        Framebuffer source = mc.getFramebuffer();
        if (source == null || !OpenGlHelper.isFramebufferEnabled() || source.framebufferTexture <= 0 || mc.displayWidth <= 0 || mc.displayHeight <= 0)
        {
            return;
        }

        int levels = AimSettings.clamp(AimSettings.blurStrength, 1, MAX_LEVELS - 1) + 1;
        boolean flip = !OptifineHelper.isShadersEnabled();
        State state = null;

        try
        {
            state = new State();
            ensureLevels(mc.displayWidth, mc.displayHeight);
            prepare();

            drawInto(LEVELS[0], source.framebufferTexture);
            for (int i = 1; i < levels; i++)
            {
                drawInto(LEVELS[i], LEVELS[i - 1].framebufferTexture);
            }

            levelCount = levels;
            flipVertical = flip;
            captured = true;
        }
        catch (Throwable t)
        {
            failed = true;
            captured = false;
            LOGGER.warn("Background blur capture failed, falling back to a plain overlay", t);
        }
        finally
        {
            if (state != null)
            {
                state.restore();
            }
        }
    }

    /** Draws the blurred image over the current framebuffer. */
    public static void composite()
    {
        if (!captured || failed || levelCount <= 0)
        {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        State state = null;

        try
        {
            state = new State();
            prepare();

            for (int i = levelCount - 1; i > 0; i--)
            {
                drawInto(LEVELS[i - 1], LEVELS[i].framebufferTexture);
            }

            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, state.framebuffer);
            GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
            drawQuad(LEVELS[0].framebufferTexture);
        }
        catch (Throwable t)
        {
            failed = true;
            captured = false;
            LOGGER.warn("Background blur composite failed, falling back to a plain overlay", t);
        }
        finally
        {
            if (state != null)
            {
                state.restore();
            }
        }
    }

    private static void prepare()
    {
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, 1.0D, 1.0D, 0.0D, -1000.0D, 1000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawInto(Framebuffer target, int texture)
    {
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, target.framebufferObject);
        GlStateManager.viewport(0, 0, target.framebufferWidth, target.framebufferHeight);
        drawQuad(texture);
    }

    private static void drawQuad(int texture)
    {
        float v0 = flipVertical ? 1.0F : 0.0F;
        float v1 = flipVertical ? 0.0F : 1.0F;

        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        renderer.pos(0.0D, 1.0D, 0.0D).tex(0.0F, v1).endVertex();
        renderer.pos(1.0D, 1.0D, 0.0D).tex(1.0F, v1).endVertex();
        renderer.pos(1.0D, 0.0D, 0.0D).tex(1.0F, v0).endVertex();
        renderer.pos(0.0D, 0.0D, 0.0D).tex(0.0F, v0).endVertex();
        tessellator.draw();
    }

    private static void ensureLevels(int width, int height)
    {
        if (LEVELS[0] != null && width == levelWidth && height == levelHeight)
        {
            return;
        }

        for (int i = 0; i < MAX_LEVELS; i++)
        {
            int levelW = Math.max(1, width >> i + 1);
            int levelH = Math.max(1, height >> i + 1);

            if (LEVELS[i] == null)
            {
                LEVELS[i] = new Framebuffer(levelW, levelH, false);
            }
            else
            {
                LEVELS[i].createBindFramebuffer(levelW, levelH);
            }
            LEVELS[i].setFramebufferFilter(GL11.GL_LINEAR);
        }

        levelWidth = width;
        levelHeight = height;
    }

    private static final class State
    {
        private final int framebuffer = GL11.glGetInteger(GL_FRAMEBUFFER_BINDING);
        private final boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        private final boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        private final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        private final boolean alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        private final boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        private final boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        private final boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        private final int viewportX;
        private final int viewportY;
        private final int viewportWidth;
        private final int viewportHeight;

        private State()
        {
            IntBuffer viewport = BufferUtils.createIntBuffer(16);
            GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
            this.viewportX = viewport.get(0);
            this.viewportY = viewport.get(1);
            this.viewportWidth = viewport.get(2);
            this.viewportHeight = viewport.get(3);
        }

        private void restore()
        {
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, this.framebuffer);
            GlStateManager.viewport(this.viewportX, this.viewportY, this.viewportWidth, this.viewportHeight);

            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.popMatrix();

            GlStateManager.bindTexture(0);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (this.depth)
            {
                GlStateManager.enableDepth();
            }
            else
            {
                GlStateManager.disableDepth();
            }
            GlStateManager.depthMask(this.depthMask);

            if (this.blend)
            {
                GlStateManager.enableBlend();
            }
            else
            {
                GlStateManager.disableBlend();
            }

            if (this.alpha)
            {
                GlStateManager.enableAlpha();
            }
            else
            {
                GlStateManager.disableAlpha();
            }

            if (this.texture)
            {
                GlStateManager.enableTexture2D();
            }
            else
            {
                GlStateManager.disableTexture2D();
            }

            if (this.cull)
            {
                GlStateManager.enableCull();
            }
            else
            {
                GlStateManager.disableCull();
            }

            if (this.lighting)
            {
                GlStateManager.enableLighting();
            }
            else
            {
                GlStateManager.disableLighting();
            }

            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        }
    }
}
