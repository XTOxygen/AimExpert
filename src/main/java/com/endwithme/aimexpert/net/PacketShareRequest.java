package com.endwithme.aimexpert.net;

import com.endwithme.aimexpert.share.ShareService;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Sent by a client that wants to share a crosshair template with one or all players. */
public class PacketShareRequest implements IMessage
{
    public String templateName = "";
    public String json = "";
    public String recipient = "";

    public PacketShareRequest()
    {
    }

    public PacketShareRequest(String templateName, String json, String recipient)
    {
        this.templateName = templateName;
        this.json = json;
        this.recipient = recipient == null ? "" : recipient;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.templateName = ByteBufUtils.readUTF8String(buf);
        this.json = ByteBufUtils.readUTF8String(buf);
        this.recipient = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.templateName == null ? "" : this.templateName);
        ByteBufUtils.writeUTF8String(buf, this.json == null ? "" : this.json);
        ByteBufUtils.writeUTF8String(buf, this.recipient == null ? "" : this.recipient);
    }

    public static class Handler implements IMessageHandler<PacketShareRequest, IMessage>
    {
        @Override
        public IMessage onMessage(PacketShareRequest message, MessageContext ctx)
        {
            EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
            if (sender != null)
            {
                ShareService.handleRequest(sender, message);
            }
            return null;
        }
    }
}
