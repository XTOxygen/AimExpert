package com.endwithme.aimexpert.net;

import com.endwithme.aimexpert.AimExpert;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Delivers a shared template to a player, who can accept or decline it in chat. */
public class PacketShareOffer implements IMessage
{
    public int offerId;
    public String senderName = "";
    public String templateName = "";
    public String json = "";

    public PacketShareOffer()
    {
    }

    public PacketShareOffer(int offerId, String senderName, String templateName, String json)
    {
        this.offerId = offerId;
        this.senderName = senderName;
        this.templateName = templateName;
        this.json = json;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.offerId = buf.readInt();
        this.senderName = ByteBufUtils.readUTF8String(buf);
        this.templateName = ByteBufUtils.readUTF8String(buf);
        this.json = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.offerId);
        ByteBufUtils.writeUTF8String(buf, this.senderName == null ? "" : this.senderName);
        ByteBufUtils.writeUTF8String(buf, this.templateName == null ? "" : this.templateName);
        ByteBufUtils.writeUTF8String(buf, this.json == null ? "" : this.json);
    }

    public static class Handler implements IMessageHandler<PacketShareOffer, IMessage>
    {
        @Override
        public IMessage onMessage(PacketShareOffer message, MessageContext ctx)
        {
            AimExpert.proxy.onShareOffer(message);
            return null;
        }
    }
}
