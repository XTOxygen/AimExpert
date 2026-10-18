package com.endwithme.aimexpert.proxy;

import com.endwithme.aimexpert.net.PacketHelloAck;
import com.endwithme.aimexpert.net.PacketShareOffer;
import com.endwithme.aimexpert.net.PacketShareResult;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Common side proxy. Client bound packets are dispatched through this class so that no
 * client only type is ever referenced from the shared network handlers.
 */
public class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {
    }

    public void init(FMLInitializationEvent event)
    {
    }

    public void onClientHello(EntityPlayerMP player)
    {
    }

    public void onHelloAck(PacketHelloAck message)
    {
    }

    public void onShareOffer(PacketShareOffer message)
    {
    }

    public void onShareResult(PacketShareResult message)
    {
    }

    public void openGui()
    {
    }
}
