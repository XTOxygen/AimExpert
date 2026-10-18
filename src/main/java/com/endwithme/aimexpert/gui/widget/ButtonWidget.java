package com.endwithme.aimexpert.gui.widget;

import com.endwithme.aimexpert.gui.GuiTheme;
import com.endwithme.aimexpert.render.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ButtonWidget extends Widget
{
    private final Runnable action;
    private final boolean accent;

    public ButtonWidget(String label, Runnable action)
    {
        this(label, action, false);
    }

    public ButtonWidget(String label, Runnable action, boolean accent)
    {
        super(label);
        this.action = action;
        this.accent = accent;
    }

    @Override
    public int getHeight()
    {
        return 16;
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        boolean hovered = this.isHovered(mouseX, mouseY);
        int background = hovered ? GuiTheme.BUTTON_HOVER : GuiTheme.BUTTON;
        int textColor = GuiTheme.BUTTON_TEXT;

        if (this.accent)
        {
            background = hovered ? 0xE6FFFFFF : 0xCCE0E0E0;
            textColor = 0xFF151515;
        }

        RenderUtils.roundedRect(this.x + 2, this.y + 1, this.width - 4, this.getHeight() - 2, 3.0F, background);
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        String text = font.trimStringToWidth(this.text(), this.width - 10);
        font.drawString(text, this.x + (this.width - font.getStringWidth(text)) / 2, this.y + (this.getHeight() - font.FONT_HEIGHT) / 2 + 1, textColor, false);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (button == 0 && this.isHovered(mouseX, mouseY))
        {
            this.action.run();
            return true;
        }
        return false;
    }
}
