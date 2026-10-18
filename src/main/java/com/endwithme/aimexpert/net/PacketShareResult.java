package com.endwithme.aimexpert.net;

import com.endwithme.aimexpert.AimExpert;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Result of a share request, shown to the sender in his own language. */
public class PacketShareResult implements IMessage
{
    public static final byte SINGLE = 0;
    public static final byte ALL = 1;
    public static final byte NO_RECIPIENT = 2;
    public static final byte ACCEPTED = 3;
    public static final byte DECLINED = 4;

    public byte result;
    public String templateName = "";
    public String name = "";
    public int count;

    public PacketShareResult()
    {
    }

    public PacketShareResult(byte result, String templateName, String name, int count)
    {
        this.result = result;
        this.templateName = templateName;
        this.name = name;
        this.count = count;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.result = buf.readByte();
        this.templateName = ByteBufUtils.readUTF8String(buf);
        this.name = ByteBufUtils.readUTF8String(buf);
        this.count = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(this.result);
        ByteBufUtils.writeUTF8String(buf, this.templateName == null ? "" : this.templateName);
        ByteBufUtils.writeUTF8String(buf, this.name == null ? "" : this.name);
        buf.writeInt(this.count);
    }

    public static class Handler implements IMessageHandler<PacketShareResult, IMessage>
    {
        @Override
        public IMessage onMessage(PacketShareResult message, MessageContext ctx)
        {
            AimExpert.proxy.onShareResult(message);
            return null;
        }
    }
}
