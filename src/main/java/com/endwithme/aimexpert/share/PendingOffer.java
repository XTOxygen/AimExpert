package com.endwithme.aimexpert.share;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class PendingOffer
{
    private final int id;
    private final String sender;
    private final String templateName;
    private final String json;
    private final long time;

    public PendingOffer(int id, String sender, String templateName, String json, long time)
    {
        this.id = id;
        this.sender = sender;
        this.templateName = templateName;
        this.json = json;
        this.time = time;
    }

    public int getId()
    {
        return this.id;
    }

    public String getSender()
    {
        return this.sender;
    }

    public String getTemplateName()
    {
        return this.templateName;
    }

    public String getJson()
    {
        return this.json;
    }

    public long getTime()
    {
        return this.time;
    }
}
