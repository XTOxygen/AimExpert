package com.endwithme.aimexpert.gui.widget;

import com.endwithme.aimexpert.gui.GuiTheme;
import com.endwithme.aimexpert.render.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** A label with a color swatch that opens three RGB sliders when clicked. */
@SideOnly(Side.CLIENT)
public class ColorRowWidget extends Widget
{
    private static final int SWATCH_SIZE = 12;
    private static final int CHANNEL_HEIGHT = 11;

    private final Settings.IntGet getter;
    private final Settings.IntSet setter;
    private boolean expanded;
    private int draggingChannel = -1;

    public ColorRowWidget(String label, Settings.IntGet getter, Settings.IntSet setter)
    {
        super(label);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int getHeight()
    {
        return this.expanded ? 18 + CHANNEL_HEIGHT * 3 + 2 : 18;
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        int color = this.getter.get();
        int titleY = this.y + (18 - font.FONT_HEIGHT) / 2 + 1;
        font.drawString(this.text(), this.x + 2, titleY, GuiTheme.TEXT, false);

        int swatchX = this.x + this.width - SWATCH_SIZE - 2;
        int swatchY = this.y + (18 - SWATCH_SIZE) / 2;
        RenderUtils.roundedRect(swatchX, swatchY, SWATCH_SIZE, SWATCH_SIZE, 3.0F, 0xFF000000 | color);
        RenderUtils.roundedOutline(swatchX, swatchY, SWATCH_SIZE, SWATCH_SIZE, 3.0F, 1.0F, this.expanded ? 0xCCFFFFFF : 0x66FFFFFF);

        if (!this.expanded)
        {
            return;
        }

        String[] names = new String[] {"R", "G", "B"};
        for (int channel = 0; channel < 3; channel++)
        {
            int channelY = this.y + 19 + channel * CHANNEL_HEIGHT;
            int value = color >> (16 - channel * 8) & 255;
            font.drawString(names[channel], this.x + 4, channelY + 2, GuiTheme.TEXT_DIM, false);

            int trackX = this.x + 16;
            int trackWidth = Math.max(1, this.x + this.width - trackX - 4);
            RenderUtils.roundedRect(trackX, channelY + 4, trackWidth, 3, 1.5F, GuiTheme.TRACK);
            int fillWidth = (int)(trackWidth * (value / 255.0F));
            if (fillWidth > 0)
            {
                RenderUtils.roundedRect(trackX, channelY + 4, fillWidth, 3, 1.5F, channelColor(channel));
            }
            RenderUtils.roundedRect(trackX + fillWidth - 2, channelY + 2, 4, 7, 2.0F, GuiTheme.KNOB);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (button != 0 || !this.isHovered(mouseX, mouseY))
        {
            return false;
        }

        int swatchX = this.x + this.width - SWATCH_SIZE - 2;
        int swatchY = this.y + (18 - SWATCH_SIZE) / 2;
        if (this.isInside(mouseX, mouseY, swatchX, swatchY, SWATCH_SIZE, SWATCH_SIZE))
        {
            this.expanded = !this.expanded;
            this.draggingChannel = -1;
            return true;
        }

        if (this.expanded && mouseY >= this.y + 19)
        {
            int channel = (mouseY - (this.y + 19)) / CHANNEL_HEIGHT;
            if (channel >= 0 && channel < 3)
            {
                this.draggingChannel = channel;
                this.updateChannel(channel, mouseX);
                return true;
            }
        }

        return true;
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int button)
    {
        if (this.draggingChannel >= 0)
        {
            this.updateChannel(this.draggingChannel, mouseX);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        this.draggingChannel = -1;
    }

    private void updateChannel(int channel, int mouseX)
    {
        int trackX = this.x + 16;
        int trackWidth = Math.max(1, this.x + this.width - trackX - 4);
        float ratio = (mouseX - trackX) / (float)trackWidth;
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));
        int value = Math.round(ratio * 255.0F);
        int shift = 16 - channel * 8;
        int color = this.getter.get();
        color = color & ~(255 << shift) | value << shift;
        this.setter.set(color & 0xFFFFFF);
    }

    private static int channelColor(int channel)
    {
        if (channel == 0)
        {
            return 0xFFE06060;
        }
        if (channel == 1)
        {
            return 0xFF60E060;
        }
        return 0xFF6080E0;
    }
}
