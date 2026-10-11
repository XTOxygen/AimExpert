package com.endwithme.aimexpert.event;

import com.endwithme.aimexpert.config.AimSettings;
import com.endwithme.aimexpert.crosshair.CrosshairState;
import com.endwithme.aimexpert.gui.AimExpertGui;
import com.endwithme.aimexpert.proxy.ClientProxy;
import com.endwithme.aimexpert.render.BlurRenderer;
import com.endwithme.aimexpert.render.CrosshairRenderer;
import com.endwithme.aimexpert.share.ShareManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEvents
{
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ShareManager.tick(mc);

        if (ClientProxy.keyOpenGui != null && ClientProxy.keyOpenGui.isPressed() && mc.currentScreen == null)
        {
            mc.displayGuiScreen(new AimExpertGui());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event)
    {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
        {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        if (AimSettings.hideCrosshair)
        {
            event.setCanceled(true);
            return;
        }

        if (!AimSettings.useCustomCrosshair)
        {
            return;
        }

        if (mc.thePlayer == null || mc.gameSettings.hideGUI || mc.gameSettings.showDebugInfo)
        {
            return;
        }

        event.setCanceled(true);
        CrosshairState.update(mc, event.partialTicks);
        ScaledResolution resolution = event.resolution;
        CrosshairRenderer.render(CrosshairState.getType(), resolution.getScaledWidth() / 2.0F, resolution.getScaledHeight() / 2.0F);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (AimSettings.blurEnabled && mc.currentScreen instanceof AimExpertGui)
        {
            BlurRenderer.capture();
        }
        else
        {
            BlurRenderer.reset();
        }
    }
}
