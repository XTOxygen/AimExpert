package com.endwithme.aimexpert.net;

import com.endwithme.aimexpert.share.ShareService;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Client answer to a share offer. */
public class PacketShareResponse implements IMessage
{
    public int offerId;
    public boolean accepted;

    public PacketShareResponse()
    {
    }

    public PacketShareResponse(int offerId, boolean accepted)
    {
        this.offerId = offerId;
        this.accepted = accepted;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.offerId = buf.readInt();
        this.accepted = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.offerId);
        buf.writeBoolean(this.accepted);
    }

    public static class Handler implements IMessageHandler<PacketShareResponse, IMessage>
    {
        @Override
        public IMessage onMessage(PacketShareResponse message, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player != null)
            {
                ShareService.handleResponse(player, message);
            }
            return null;
        }
    }
}
