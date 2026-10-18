package com.endwithme.aimexpert.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ChatHelper
{
    private static final String PREFIX = EnumChatFormatting.DARK_GRAY + "[" + EnumChatFormatting.AQUA + "AimExpert" + EnumChatFormatting.DARK_GRAY + "] " + EnumChatFormatting.RESET;

    private ChatHelper()
    {
    }

    public static void send(String text)
    {
        send(new ChatComponentText(text == null ? "" : text));
    }

    public static void send(IChatComponent component)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.ingameGUI == null || component == null)
        {
            return;
        }

        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(PREFIX).appendSibling(component));
    }
}
