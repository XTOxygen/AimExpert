package com.endwithme.aimexpert;

import java.io.File;

import com.endwithme.aimexpert.config.AimSettings;
import com.endwithme.aimexpert.crosshair.TemplateManager;
import com.endwithme.aimexpert.event.CommonEvents;
import com.endwithme.aimexpert.net.PacketHandler;
import com.endwithme.aimexpert.proxy.CommonProxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, acceptedMinecraftVersions = "[1.8.9]")
public class AimExpert
{
    @Mod.Instance(Reference.MODID)
    public static AimExpert instance;

    @SidedProxy(clientSide = "com.endwithme.aimexpert.proxy.ClientProxy", serverSide = "com.endwithme.aimexpert.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        File configDirectory = event.getModConfigurationDirectory();
        AimSettings.load(new File(configDirectory, Reference.NAME + ".cfg"));
        TemplateManager.init(configDirectory);
        PacketHandler.init();
        MinecraftForge.EVENT_BUS.register(new CommonEvents());
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }
}
