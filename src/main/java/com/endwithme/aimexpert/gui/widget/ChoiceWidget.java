package com.endwithme.aimexpert.gui.widget;

import com.endwithme.aimexpert.gui.GuiTheme;
import com.endwithme.aimexpert.i18n.Lang;
import com.endwithme.aimexpert.render.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ChoiceWidget extends Widget
{
    private static final int DEFAULT_SEGMENT_WIDTH = 36;

    private final String[] options;
    private final Settings.IntGet getter;
    private final Settings.IntSet setter;
    private final int segmentWidth;

    public ChoiceWidget(String label, String[] options, Settings.IntGet getter, Settings.IntSet setter)
    {
        this(label, options, getter, setter, DEFAULT_SEGMENT_WIDTH);
    }

    public ChoiceWidget(String label, String[] options, Settings.IntGet getter, Settings.IntSet setter, int segmentWidth)
    {
        super(label);
        this.options = options;
        this.getter = getter;
        this.setter = setter;
        this.segmentWidth = Math.max(20, segmentWidth);
    }

    @Override
    public int getHeight()
    {
        return 16;
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        int selected = this.getter.get();
        font.drawString(this.text(), this.x + 2, this.y + (this.getHeight() - font.FONT_HEIGHT) / 2 + 1, GuiTheme.TEXT, false);

        int total = this.options.length * this.segmentWidth;
        int startX = this.x + this.width - total;
        for (int i = 0; i < this.options.length; i++)
        {
            int boxX = startX + i * this.segmentWidth;
            boolean active = i == selected;
            boolean hovered = this.isInside(mouseX, mouseY, boxX, this.y + 2, this.segmentWidth - 2, this.getHeight() - 4);
            int background = active ? 0xD9E6E6E6 : (hovered ? GuiTheme.BUTTON_HOVER : GuiTheme.BUTTON);
            RenderUtils.roundedRect(boxX, this.y + 2, this.segmentWidth - 2, this.getHeight() - 4, 3.0F, background);
            String text = font.trimStringToWidth(Lang.get(this.options[i]), this.segmentWidth - 8);
            font.drawString(text, boxX + (this.segmentWidth - 2 - font.getStringWidth(text)) / 2, this.y + (this.getHeight() - font.FONT_HEIGHT) / 2 + 1, active ? 0xFF151515 : GuiTheme.TEXT_DIM, false);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (button != 0 || !this.isHovered(mouseX, mouseY))
        {
            return false;
        }

        int total = this.options.length * this.segmentWidth;
        int startX = this.x + this.width - total;
        for (int i = 0; i < this.options.length; i++)
        {
            if (this.isInside(mouseX, mouseY, startX + i * this.segmentWidth, this.y + 2, this.segmentWidth - 2, this.getHeight() - 4))
            {
                this.setter.set(i);
                return true;
            }
        }
        return true;
    }
}
