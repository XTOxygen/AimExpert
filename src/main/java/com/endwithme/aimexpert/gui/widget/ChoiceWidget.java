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
    private static final int SEGMENT_WIDTH = 36;

    private final String[] options;
    private final Settings.IntGet getter;
    private final Settings.IntSet setter;

    public ChoiceWidget(String label, String[] options, Settings.IntGet getter, Settings.IntSet setter)
    {
        super(label);
        this.options = options;
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
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        int selected = this.getter.get();
        font.drawString(this.text(), this.x + 2, this.y + (this.getHeight() - font.FONT_HEIGHT) / 2 + 1, GuiTheme.TEXT, false);

        int total = this.options.length * SEGMENT_WIDTH;
        int startX = this.x + this.width - total;
        for (int i = 0; i < this.options.length; i++)
        {
            int boxX = startX + i * SEGMENT_WIDTH;
            boolean active = i == selected;
            boolean hovered = this.isInside(mouseX, mouseY, boxX, this.y + 2, SEGMENT_WIDTH - 2, this.getHeight() - 4);
            int background = active ? 0xD9E6E6E6 : (hovered ? GuiTheme.BUTTON_HOVER : GuiTheme.BUTTON);
            RenderUtils.roundedRect(boxX, this.y + 2, SEGMENT_WIDTH - 2, this.getHeight() - 4, 3.0F, background);
            String text = font.trimStringToWidth(Lang.get(this.options[i]), SEGMENT_WIDTH - 8);
            font.drawString(text, boxX + (SEGMENT_WIDTH - 2 - font.getStringWidth(text)) / 2, this.y + (this.getHeight() - font.FONT_HEIGHT) / 2 + 1, active ? 0xFF151515 : GuiTheme.TEXT_DIM, false);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (button != 0 || !this.isHovered(mouseX, mouseY))
        {
            return false;
        }

        int total = this.options.length * SEGMENT_WIDTH;
        int startX = this.x + this.width - total;
        for (int i = 0; i < this.options.length; i++)
        {
            if (this.isInside(mouseX, mouseY, startX + i * SEGMENT_WIDTH, this.y + 2, SEGMENT_WIDTH - 2, this.getHeight() - 4))
            {
                this.setter.set(i);
                return true;
            }
        }
        return true;
    }
}
