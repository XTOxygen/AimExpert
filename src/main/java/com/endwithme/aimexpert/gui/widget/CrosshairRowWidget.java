package com.endwithme.aimexpert.gui.widget;

import com.endwithme.aimexpert.config.AimSettings;
import com.endwithme.aimexpert.crosshair.CrosshairType;
import com.endwithme.aimexpert.gui.GuiTheme;
import com.endwithme.aimexpert.render.CrosshairRenderer;
import com.endwithme.aimexpert.render.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CrosshairRowWidget extends Widget
{
    private static final int SWITCH_WIDTH = 22;
    private static final int SWITCH_HEIGHT = 11;
    private static final int SWATCH_SIZE = 12;
    private static final int CHANNEL_HEIGHT = 11;

    private final CrosshairType type;
    private final Settings.BoolGet enabledGet;
    private final Settings.BoolSet enabledSet;
    private final Settings.IntGet colorGet;
    private final Settings.IntSet colorSet;
    private boolean expanded;
    private int draggingChannel = -1;

    public CrosshairRowWidget(CrosshairType type, Settings.BoolGet enabledGet, Settings.BoolSet enabledSet, Settings.IntGet colorGet, Settings.IntSet colorSet)
    {
        super(type.getLangKey());
        this.type = type;
        this.enabledGet = enabledGet;
        this.enabledSet = enabledSet;
        this.colorGet = colorGet;
        this.colorSet = colorSet;
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
        boolean enabled = this.enabledGet.get();
        int color = this.colorGet.get();

        CrosshairRenderer.renderPreview(this.type, this.x + 12, this.y + 9, 1.0F, enabled ? 0.9F : 0.3F);

        int titleY = this.y + (18 - font.FONT_HEIGHT) / 2 + 1;
        font.drawString(this.text(), this.x + 24, titleY, enabled ? GuiTheme.TEXT : GuiTheme.TEXT_DISABLED, false);

        int swatchX = this.x + this.width - SWATCH_SIZE - 30;
        int swatchY = this.y + (18 - SWATCH_SIZE) / 2;
        RenderUtils.roundedRect(swatchX, swatchY, SWATCH_SIZE, SWATCH_SIZE, 3.0F, 0xFF000000 | color);
        RenderUtils.roundedOutline(swatchX, swatchY, SWATCH_SIZE, SWATCH_SIZE, 3.0F, 1.0F, this.expanded ? 0xCCFFFFFF : 0x66FFFFFF);

        int switchX = this.x + this.width - SWITCH_WIDTH;
        int switchY = this.y + (18 - SWITCH_HEIGHT) / 2;
        RenderUtils.roundedRect(switchX, switchY, SWITCH_WIDTH, SWITCH_HEIGHT, SWITCH_HEIGHT / 2.0F, enabled ? GuiTheme.TOGGLE_ON : GuiTheme.TOGGLE_OFF);
        int knobSize = SWITCH_HEIGHT - 2;
        int knobX = enabled ? switchX + SWITCH_WIDTH - knobSize - 1 : switchX + 1;
        RenderUtils.roundedRect(knobX, switchY + 1, knobSize, knobSize, knobSize / 2.0F, enabled ? GuiTheme.TOGGLE_ON_KNOB : GuiTheme.TOGGLE_OFF_KNOB);

        if (this.expanded)
        {
            String[] names = new String[] {"R", "G", "B"};
            for (int channel = 0; channel < 3; channel++)
            {
                int channelY = this.y + 19 + channel * CHANNEL_HEIGHT;
                int value = color >> (16 - channel * 8) & 255;
                font.drawString(names[channel], this.x + 24, channelY + 2, GuiTheme.TEXT_DIM, false);

                int trackX = this.x + 34;
                int trackWidth = this.x + this.width - trackX;
                RenderUtils.roundedRect(trackX, channelY + 4, trackWidth, 3, 1.5F, GuiTheme.TRACK);
                int fillWidth = (int)(trackWidth * (value / 255.0F));
                if (fillWidth > 0)
                {
                    RenderUtils.roundedRect(trackX, channelY + 4, fillWidth, 3, 1.5F, channelColor(channel));
                }
                RenderUtils.roundedRect(trackX + fillWidth - 2, channelY + 2, 4, 7, 2.0F, GuiTheme.KNOB);
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (button != 0 || !this.isHovered(mouseX, mouseY))
        {
            return false;
        }

        int switchX = this.x + this.width - SWITCH_WIDTH;
        int switchY = this.y + (18 - SWITCH_HEIGHT) / 2;
        if (this.isInside(mouseX, mouseY, switchX, switchY, SWITCH_WIDTH, SWITCH_HEIGHT))
        {
            this.enabledSet.set(!this.enabledGet.get());
            return true;
        }

        int swatchX = this.x + this.width - SWATCH_SIZE - 30;
        int swatchY = this.y + (18 - SWATCH_SIZE) / 2;
        if (this.isInside(mouseX, mouseY, swatchX, swatchY, SWATCH_SIZE, SWATCH_SIZE))
        {
            this.expanded = !this.expanded;
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
        int trackX = this.x + 34;
        int trackWidth = Math.max(1, this.x + this.width - trackX);
        float ratio = (mouseX - trackX) / (float)trackWidth;
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));
        int value = Math.round(ratio * 255.0F);
        int shift = 16 - channel * 8;
        int color = this.colorGet.get();
        color = color & ~(255 << shift) | value << shift;
        this.colorSet.set(color & 0xFFFFFF);
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

    public static CrosshairRowWidget create(final CrosshairType type)
    {
        final String name = type.name();
        return new CrosshairRowWidget(type,
            new Settings.BoolGet()
            {
                @Override
                public boolean get()
                {
                    return AimSettings.isEnabled(name);
                }
            },
            new Settings.BoolSet()
            {
                @Override
                public void set(boolean value)
                {
                    AimSettings.setEnabled(name, value);
                }
            },
            new Settings.IntGet()
            {
                @Override
                public int get()
                {
                    return AimSettings.getColor(name);
                }
            },
            new Settings.IntSet()
            {
                @Override
                public void set(int value)
                {
                    AimSettings.setColor(name, value);
                }
            });
    }
}
