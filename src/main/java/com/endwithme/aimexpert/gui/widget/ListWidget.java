package com.endwithme.aimexpert.gui.widget;

import java.util.ArrayList;
import java.util.List;

import com.endwithme.aimexpert.gui.GuiTheme;
import com.endwithme.aimexpert.render.RenderUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ListWidget extends Widget
{
    public interface ItemSource
    {
        List<String> getItems();
    }

    public interface ItemClick
    {
        void onClick(String item, int button);
    }

    private static final int ROW_HEIGHT = 11;

    private final ItemSource source;
    private final ItemClick click;
    private final Settings.TextGet selected;
    private final int visibleRows;
    private int scroll;

    public ListWidget(String label, ItemSource source, ItemClick click, Settings.TextGet selected, int visibleRows)
    {
        super(label);
        this.source = source;
        this.click = click;
        this.selected = selected;
        this.visibleRows = Math.max(1, visibleRows);
    }

    @Override
    public int getHeight()
    {
        return this.visibleRows * ROW_HEIGHT + 6;
    }

    private List<String> items()
    {
        List<String> list = this.source.getItems();
        return list == null ? new ArrayList<String>() : list;
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        RenderUtils.roundedRect(this.x + 2, this.y + 1, this.width - 4, this.getHeight() - 2, 3.0F, GuiTheme.LIST_BACKGROUND);

        List<String> items = this.items();
        int maxScroll = Math.max(0, items.size() - this.visibleRows);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));
        String selectedItem = this.selected == null ? null : this.selected.get();

        for (int row = 0; row < this.visibleRows; row++)
        {
            int index = row + this.scroll;
            if (index >= items.size())
            {
                break;
            }

            String item = items.get(index);
            int rowY = this.y + 3 + row * ROW_HEIGHT;
            boolean hovered = this.isInside(mouseX, mouseY, this.x + 3, rowY, this.width - 8, ROW_HEIGHT);
            boolean isSelected = selectedItem != null && selectedItem.equals(item);

            if (isSelected)
            {
                RenderUtils.rect(this.x + 3, rowY, this.width - 8, ROW_HEIGHT, GuiTheme.LIST_SELECTED);
            }
            else if (hovered)
            {
                RenderUtils.rect(this.x + 3, rowY, this.width - 8, ROW_HEIGHT, GuiTheme.LIST_HOVER);
            }

            font.drawString(font.trimStringToWidth(item, this.width - 14), this.x + 6, rowY + 2, isSelected ? GuiTheme.TITLE : GuiTheme.TEXT, false);
        }

        if (maxScroll > 0)
        {
            int trackHeight = this.getHeight() - 6;
            int barHeight = Math.max(6, trackHeight * this.visibleRows / Math.max(1, items.size()));
            int barY = this.y + 3 + (trackHeight - barHeight) * this.scroll / maxScroll;
            RenderUtils.roundedRect(this.x + this.width - 5, barY, 2, barHeight, 1.0F, GuiTheme.SCROLL_BAR);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        if (!this.isHovered(mouseX, mouseY))
        {
            return false;
        }

        List<String> items = this.items();
        for (int row = 0; row < this.visibleRows; row++)
        {
            int index = row + this.scroll;
            if (index >= items.size())
            {
                break;
            }

            int rowY = this.y + 3 + row * ROW_HEIGHT;
            if (this.isInside(mouseX, mouseY, this.x + 3, rowY, this.width - 8, ROW_HEIGHT))
            {
                this.click.onClick(items.get(index), button);
                return true;
            }
        }
        return true;
    }

    @Override
    public void mouseScrolled(int amount)
    {
        int maxScroll = Math.max(0, this.items().size() - this.visibleRows);
        this.scroll = Math.max(0, Math.min(this.scroll - amount, maxScroll));
    }
}
