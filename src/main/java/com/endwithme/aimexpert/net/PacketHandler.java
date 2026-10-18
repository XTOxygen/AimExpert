package com.endwithme.aimexpert.net;

import com.endwithme.aimexpert.Reference;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketHandler
{
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.CHANNEL);

    private PacketHandler()
    {
    }

    public static void init()
    {
        NETWORK.registerMessage(PacketHello.Handler.class, PacketHello.class, 0, Side.SERVER);
        NETWORK.registerMessage(PacketHelloAck.Handler.class, PacketHelloAck.class, 1, Side.CLIENT);
        NETWORK.registerMessage(PacketShareRequest.Handler.class, PacketShareRequest.class, 2, Side.SERVER);
        NETWORK.registerMessage(PacketShareOffer.Handler.class, PacketShareOffer.class, 3, Side.CLIENT);
        NETWORK.registerMessage(PacketShareResponse.Handler.class, PacketShareResponse.class, 4, Side.SERVER);
        NETWORK.registerMessage(PacketShareResult.Handler.class, PacketShareResult.class, 5, Side.CLIENT);
    }
}
