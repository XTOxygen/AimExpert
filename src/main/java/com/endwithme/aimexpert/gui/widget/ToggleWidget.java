package com.endwithme.aimexpert.gui.widget;

import com.endwithme.aimexpert.gui.GuiTheme;
import com.endwithme.aimexpert.render.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ToggleWidget extends Widget
{
    private static final int SWITCH_WIDTH = 22;
    private static final int SWITCH_HEIGHT = 11;

    private final Settings.BoolGet getter;
    private final Settings.BoolSet setter;

    public ToggleWidget(String label, Settings.BoolGet getter, Settings.BoolSet setter)
    {
        super(label);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int getHeight()
    {
        return 16;
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        boolean value = this.getter.get();
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        int textY = this.y + (this.getHeight() - font.FONT_HEIGHT) / 2 + 1;
        font.drawString(this.text(), this.x + 2, textY, value ? GuiTheme.TEXT : GuiTheme.TEXT_DIM, false);

        int switchX = this.x + this.width - SWITCH_WIDTH;
        int switchY = this.y + (this.getHeight() - SWITCH_HEIGHT) / 2;
        RenderUtils.roundedRect(switchX, switchY, SWITCH_WIDTH, SWITCH_HEIGHT, SWITCH_HEIGHT / 2.0F, value ? GuiTheme.TOGGLE_ON : GuiTheme.TOGGLE_OFF);

        int knobSize = SWITCH_HEIGHT - 2;
        int knobX = value ? switchX + SWITCH_WIDTH - knobSize - 1 : switchX + 1;
        RenderUtils.roundedRect(knobX, switchY + 1, knobSize, knobSize, knobSize / 2.0F, value ? GuiTheme.TOGGLE_ON_KNOB : GuiTheme.TOGGLE_OFF_KNOB);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (button == 0 && this.isHovered(mouseX, mouseY))
        {
            this.setter.set(!this.getter.get());
            return true;
        }
        return false;
    }
}
