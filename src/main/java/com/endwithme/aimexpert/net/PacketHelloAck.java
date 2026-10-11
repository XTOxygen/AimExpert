package com.endwithme.aimexpert.net;

import com.endwithme.aimexpert.AimExpert;
import com.endwithme.aimexpert.Reference;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Confirms that the server understands AimExpert sharing. */
public class PacketHelloAck implements IMessage
{
    public String version = Reference.VERSION;

    public PacketHelloAck()
    {
    }

    public PacketHelloAck(String version)
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

    public static class Handler implements IMessageHandler<PacketHelloAck, IMessage>
    {
        @Override
        public IMessage onMessage(PacketHelloAck message, MessageContext ctx)
        {
            AimExpert.proxy.onHelloAck(message);
            return null;
        }
    }
}
