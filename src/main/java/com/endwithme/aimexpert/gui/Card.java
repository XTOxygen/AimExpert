package com.endwithme.aimexpert.gui;

import java.util.ArrayList;
import java.util.List;

import com.endwithme.aimexpert.gui.widget.Widget;
import com.endwithme.aimexpert.i18n.Lang;
import com.endwithme.aimexpert.render.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Card
{
    private static final int TITLE_HEIGHT = 20;
    private static final int PADDING = 6;

    private final String titleKey;
    private final List<Widget> widgets = new ArrayList<Widget>();
    private int x;
    private int y;
    private int width;
    private int height;

    public Card(String titleKey)
    {
        this.titleKey = titleKey;
    }

    public Card add(Widget widget)
    {
        this.widgets.add(widget);
        return this;
    }

    public List<Widget> getWidgets()
    {
        return this.widgets;
    }

    public String getTitle()
    {
        return Lang.get(this.titleKey);
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

    public int getHeight()
    {
        return this.height;
    }

    public int getContentTop()
    {
        return this.y + TITLE_HEIGHT;
    }

    /** Computes the card height for a given width without touching the absolute position. */
    public void measure(int width)
    {
        this.width = width;
        int cursor = TITLE_HEIGHT;
        for (Widget widget : this.widgets)
        {
            widget.setBounds(0, cursor, width - PADDING * 2);
            cursor += widget.getHeight() + 2;
        }
        this.height = cursor + PADDING;
    }

    public void layout(int x, int y)
    {
        this.x = x;
        this.y = y;
        int cursor = y + TITLE_HEIGHT;
        for (Widget widget : this.widgets)
        {
            widget.setBounds(x + PADDING, cursor, this.width - PADDING * 2);
            cursor += widget.getHeight() + 2;
        }
    }

    public void draw(int mouseX, int mouseY)
    {
        boolean hovered = mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
        RenderUtils.roundedRect(this.x, this.y, this.width, this.height, 5.0F, hovered ? GuiTheme.CARD_TOP_HOVER : GuiTheme.CARD_TOP, hovered ? GuiTheme.CARD_BOTTOM_HOVER : GuiTheme.CARD_BOTTOM);
        RenderUtils.roundedOutline(this.x, this.y, this.width, this.height, 5.0F, 1.0F, hovered ? GuiTheme.CARD_BORDER_HOVER : GuiTheme.CARD_BORDER);

        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        font.drawString(font.trimStringToWidth(this.getTitle(), this.width - PADDING * 2), this.x + PADDING, this.y + 6, GuiTheme.TITLE, false);
        RenderUtils.rect(this.x + PADDING, this.y + TITLE_HEIGHT - 5, this.width - PADDING * 2, 1, 0x1AFFFFFF);

        for (Widget widget : this.widgets)
        {
            widget.draw(mouseX, mouseY);
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (mouseX < this.x || mouseX > this.x + this.width || mouseY < this.y || mouseY > this.y + this.height)
        {
            return false;
        }

        for (Widget widget : this.widgets)
        {
            if (widget.mouseClicked(mouseX, mouseY, button))
            {
                return true;
            }
        }
        return true;
    }

    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        for (Widget widget : this.widgets)
        {
            widget.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void mouseDragged(int mouseX, int mouseY, int button)
    {
        for (Widget widget : this.widgets)
        {
            widget.mouseDragged(mouseX, mouseY, button);
        }
    }

    public void mouseScrolled(int amount)
    {
        for (Widget widget : this.widgets)
        {
            widget.mouseScrolled(amount);
        }
    }

    public void update()
    {
        for (Widget widget : this.widgets)
        {
            widget.update();
        }
    }

    public void onClosed()
    {
        for (Widget widget : this.widgets)
        {
            widget.onClosed();
        }
    }

    public Widget findHovered(int mouseX, int mouseY)
    {
        for (Widget widget : this.widgets)
        {
            if (widget.contains(mouseX, mouseY))
            {
                return widget;
            }
        }
        return null;
    }
}
