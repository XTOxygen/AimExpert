package com.endwithme.aimexpert.gui.widget;

import com.endwithme.aimexpert.gui.GuiTheme;
import com.endwithme.aimexpert.i18n.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LabelWidget extends Widget
{
    public interface Provider
    {
        String get();
    }

    private final Provider provider;
    private final int color;
    private final int height;

    public LabelWidget(String text)
    {
        this(text, GuiTheme.TEXT_DIM, 11);
    }

    public LabelWidget(String text, int color, int height)
    {
        super(text);
        final String value = text;
        this.provider = new Provider()
        {
            @Override
            public String get()
            {
                return Lang.get(value);
            }
        };
        this.color = color;
        this.height = height;
    }

    public LabelWidget(Provider provider, int color, int height)
    {
        super("");
        this.provider = provider;
        this.color = color;
        this.height = height;
    }

    @Override
    public int getHeight()
    {
        return this.height;
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        String text = this.provider.get();
        if (text == null || text.isEmpty())
        {
            return;
        }
        font.drawString(font.trimStringToWidth(text, this.width - 4), this.x + 2, this.y + 2, this.color, false);
    }
}
