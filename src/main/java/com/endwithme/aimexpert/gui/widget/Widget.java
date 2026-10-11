package com.endwithme.aimexpert.gui.widget;

import com.endwithme.aimexpert.i18n.Lang;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class Widget
{
    protected String label;
    protected int x;
    protected int y;
    protected int width;

    public Widget(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return this.label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    /** Resolves the label through the mod language at draw time. */
    protected String text()
    {
        return Lang.get(this.label);
    }

    public void setBounds(int x, int y, int width)
    {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getWidth()
    {
        return this.width;
    }

    public abstract int getHeight();

    public abstract void draw(int mouseX, int mouseY);

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int button)
    {
    }

    public void mouseDragged(int mouseX, int mouseY, int button)
    {
    }

    public void mouseScrolled(int amount)
    {
    }

    public boolean keyTyped(char typedChar, int keyCode)
    {
        return false;
    }

    public boolean isFocused()
    {
        return false;
    }

    public void update()
    {
    }

    public void onClosed()
    {
    }

    public boolean contains(int mouseX, int mouseY)
    {
        return this.isHovered(mouseX, mouseY);
    }

    protected boolean isHovered(int mouseX, int mouseY)
    {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.getHeight();
    }

    protected boolean isInside(int mouseX, int mouseY, int boxX, int boxY, int boxWidth, int boxHeight)
    {
        return mouseX >= boxX && mouseX <= boxX + boxWidth && mouseY >= boxY && mouseY <= boxY + boxHeight;
    }
}
