package com.endwithme.aimexpert.gui.widget;

import com.endwithme.aimexpert.gui.GuiTheme;
import com.endwithme.aimexpert.render.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SliderWidget extends Widget
{
    private final Settings.IntGet getter;
    private final Settings.IntSet setter;
    private final int min;
    private final int max;
    private final String suffix;
    private boolean dragging;

    public SliderWidget(String label, Settings.IntGet getter, Settings.IntSet setter, int min, int max, String suffix)
    {
        super(label);
        this.getter = getter;
        this.setter = setter;
        this.min = min;
        this.max = max;
        this.suffix = suffix == null ? "" : suffix;
    }

    @Override
    public int getHeight()
    {
        return 20;
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        int value = this.getter.get();
        font.drawString(this.text(), this.x + 2, this.y + 2, GuiTheme.TEXT, false);
        String text = value + this.suffix;
        font.drawString(text, this.x + this.width - font.getStringWidth(text) - 2, this.y + 2, GuiTheme.TEXT_DIM, false);

        int trackY = this.y + 15;
        RenderUtils.roundedRect(this.x + 2, trackY, this.width - 4, 3, 1.5F, GuiTheme.TRACK);

        float ratio = this.max <= this.min ? 0.0F : (value - this.min) / (float)(this.max - this.min);
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));
        int fillWidth = (int)((this.width - 4) * ratio);
        if (fillWidth > 0)
        {
            RenderUtils.roundedRect(this.x + 2, trackY, fillWidth, 3, 1.5F, GuiTheme.TRACK_FILL);
        }

        int knobX = this.x + 2 + fillWidth - 2;
        boolean active = this.dragging || this.isHovered(mouseX, mouseY);
        RenderUtils.roundedRect(knobX, trackY - 2, 4, 7, 2.0F, active ? GuiTheme.KNOB : GuiTheme.TRACK_FILL);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (button == 0 && this.isHovered(mouseX, mouseY))
        {
            this.dragging = true;
            this.updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int button)
    {
        if (this.dragging)
        {
            this.updateValue(mouseX);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        this.dragging = false;
    }

    @Override
    public void mouseScrolled(int amount)
    {
    }

    private void updateValue(int mouseX)
    {
        float ratio = (mouseX - (this.x + 2)) / (float)Math.max(1, this.width - 4);
        ratio = Math.max(0.0F, Math.min(1.0F, ratio));
        int value = Math.round(this.min + ratio * (this.max - this.min));
        this.setter.set(value);
    }
}
