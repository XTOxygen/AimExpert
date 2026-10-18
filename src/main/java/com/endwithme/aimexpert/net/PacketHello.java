package com.endwithme.aimexpert.net;

import com.endwithme.aimexpert.AimExpert;
import com.endwithme.aimexpert.Reference;
import com.endwithme.aimexpert.share.ServerUserRegistry;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Announces this client to a server that runs AimExpert. */
public class PacketHello implements IMessage
{
    public String version = Reference.VERSION;

    public PacketHello()
    {
    }

    public PacketHello(String version)
    {
        this.version = version;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.version = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.version == null ? "" : this.version);
    }

    public static class Handler implements IMessageHandler<PacketHello, IMessage>
    {
        @Override
        public IMessage onMessage(PacketHello message, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null)
            {
                return null;
            }

            ServerUserRegistry.add(player);
            AimExpert.proxy.onClientHello(player);
            return new PacketHelloAck(Reference.VERSION);
        }
    }
}
