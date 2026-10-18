package com.endwithme.aimexpert.gui.widget;

import com.endwithme.aimexpert.gui.GuiTheme;
import com.endwithme.aimexpert.render.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextFieldWidget extends Widget
{
    private static final int FIELD_WIDTH = 96;

    private final GuiTextField field;
    private final Settings.TextSet setter;

    public TextFieldWidget(String label, String initial, int maxLength, Settings.TextSet setter)
    {
        super(label);
        this.setter = setter;
        this.field = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 0, 0, FIELD_WIDTH, 10);
        this.field.setMaxStringLength(maxLength);
        this.field.setEnableBackgroundDrawing(false);
        this.field.setText(initial == null ? "" : initial);
        this.field.setTextColor(0xFFFFFF);
        this.field.setDisabledTextColour(0xFF808080);
        this.field.setCanLoseFocus(true);
    }

    @Override
    public int getHeight()
    {
        return 17;
    }

    public String getText()
    {
        return this.field.getText();
    }

    public void setText(String text)
    {
        this.field.setText(text == null ? "" : text);
        this.setter.set(this.field.getText());
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        font.drawString(this.text(), this.x + 2, this.y + (this.getHeight() - font.FONT_HEIGHT) / 2 + 1, GuiTheme.TEXT, false);

        int boxX = this.x + this.width - FIELD_WIDTH - 2;
        int boxY = this.y + 2;
        RenderUtils.roundedRect(boxX, boxY, FIELD_WIDTH, this.getHeight() - 4, 3.0F, GuiTheme.FIELD);
        RenderUtils.roundedOutline(boxX, boxY, FIELD_WIDTH, this.getHeight() - 4, 3.0F, 1.0F, this.field.isFocused() ? GuiTheme.FIELD_FOCUS : GuiTheme.FIELD_BORDER);

        this.field.xPosition = boxX + 4;
        this.field.yPosition = boxY + (this.getHeight() - 4 - 8) / 2;
        this.field.width = FIELD_WIDTH - 8;
        this.field.height = 10;
        this.field.drawTextBox();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (!this.isHovered(mouseX, mouseY))
        {
            return false;
        }

        this.setFocused(true);
        this.field.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    public void setFocused(boolean focused)
    {
        this.field.setFocused(focused);
    }

    @Override
    public boolean isFocused()
    {
        return this.field.isFocused();
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode)
    {
        if (!this.field.isFocused())
        {
            return false;
        }

        if (this.field.textboxKeyTyped(typedChar, keyCode))
        {
            this.setter.set(this.field.getText());
            return true;
        }
        return false;
    }

    @Override
    public void update()
    {
        this.field.updateCursorCounter();
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button)
    {
    }
}
