package com.endwithme.aimexpert.util;

import org.lwjgl.Sys;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Clipboard access for template exchange. The native LWJGL clipboard is preferred for reading
 * because the AWT clipboard used by vanilla can return a stale value when another program
 * (a browser or a chat window) is the current clipboard owner.
 */
@SideOnly(Side.CLIENT)
public final class ClipboardHelper
{
    private ClipboardHelper()
    {
    }

    public static String read()
    {
        try
        {
            String nativeValue = Sys.getClipboard();
            if (nativeValue != null && !nativeValue.trim().isEmpty())
            {
                return nativeValue;
            }
        }
        catch (Throwable t)
        {
            // No native clipboard on this platform, fall back to the vanilla helper.
        }

        try
        {
            String vanillaValue = GuiScreen.getClipboardString();
            return vanillaValue == null ? "" : vanillaValue;
        }
        catch (Throwable t)
        {
            return "";
        }
    }

    public static void write(String text)
    {
        String value = text == null ? "" : text;

        try
        {
            GuiScreen.setClipboardString(value);
        }
        catch (Throwable t)
        {
            // Ignored, the caller reports the failure.
        }
    }
}
