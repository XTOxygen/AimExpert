package com.endwithme.aimexpert.proxy;

import org.lwjgl.input.Keyboard;

import com.endwithme.aimexpert.command.CommandAimExpert;
import com.endwithme.aimexpert.command.CommandShareAim;
import com.endwithme.aimexpert.event.ClientEvents;
import com.endwithme.aimexpert.gui.AimExpertGui;
import com.endwithme.aimexpert.net.PacketHelloAck;
import com.endwithme.aimexpert.net.PacketShareOffer;
import com.endwithme.aimexpert.net.PacketShareResult;
import com.endwithme.aimexpert.share.ShareManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    public static KeyBinding keyOpenGui;

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        keyOpenGui = new KeyBinding("key.aimexpert.gui", Keyboard.KEY_M, "key.categories.aimexpert");
        ClientRegistry.registerKeyBinding(keyOpenGui);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        ClientCommandHandler.instance.registerCommand(new CommandShareAim());
        ClientCommandHandler.instance.registerCommand(new CommandAimExpert());
    }

    @Override
    public void onHelloAck(final PacketHelloAck message)
    {
        Minecraft.getMinecraft().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                ShareManager.setServerSupported(true);
            }
        });
    }

    @Override
    public void onShareOffer(final PacketShareOffer message)
    {
        Minecraft.getMinecraft().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                ShareManager.addOffer(message);
            }
        });
    }

    @Override
    public void onShareResult(final PacketShareResult message)
    {
        Minecraft.getMinecraft().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                ShareManager.onResult(message);
            }
        });
    }

    @Override
    public void openGui()
    {
        Minecraft.getMinecraft().displayGuiScreen(new AimExpertGui());
    }
}
