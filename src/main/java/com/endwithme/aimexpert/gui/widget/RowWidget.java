package com.endwithme.aimexpert.gui.widget;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Lays two widgets out next to each other in one row. */
@SideOnly(Side.CLIENT)
public class RowWidget extends Widget
{
    private static final int GAP = 4;

    private final Widget left;
    private final Widget right;

    public RowWidget(Widget left, Widget right)
    {
        super("");
        this.left = left;
        this.right = right;
    }

    @Override
    public int getHeight()
    {
        return Math.max(this.left.getHeight(), this.right.getHeight());
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        int half = (this.width - GAP) / 2;
        this.left.setBounds(this.x, this.y, half);
        this.right.setBounds(this.x + half + GAP, this.y, this.width - half - GAP);
        this.left.draw(mouseX, mouseY);
        this.right.draw(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        boolean handledLeft = this.left.mouseClicked(mouseX, mouseY, button);
        boolean handledRight = this.right.mouseClicked(mouseX, mouseY, button);
        return handledLeft || handledRight;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        this.left.mouseReleased(mouseX, mouseY, button);
        this.right.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int button)
    {
        this.left.mouseDragged(mouseX, mouseY, button);
        this.right.mouseDragged(mouseX, mouseY, button);
    }

    @Override
    public void mouseScrolled(int amount)
    {
        this.left.mouseScrolled(amount);
        this.right.mouseScrolled(amount);
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode)
    {
        return this.left.keyTyped(typedChar, keyCode) || this.right.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean isFocused()
    {
        return this.left.isFocused() || this.right.isFocused();
    }

    @Override
    public void update()
    {
        this.left.update();
        this.right.update();
    }

    @Override
    public void onClosed()
    {
        this.left.onClosed();
        this.right.onClosed();
    }

    @Override
    public boolean contains(int mouseX, int mouseY)
    {
        return this.left.contains(mouseX, mouseY) || this.right.contains(mouseX, mouseY);
    }
}
