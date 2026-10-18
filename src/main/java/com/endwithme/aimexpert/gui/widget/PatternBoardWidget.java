package com.endwithme.aimexpert.gui.widget;

import com.endwithme.aimexpert.config.AimSettings;
import com.endwithme.aimexpert.crosshair.CrosshairPattern;
import com.endwithme.aimexpert.crosshair.CrosshairType;
import com.endwithme.aimexpert.crosshair.TemplateManager;
import com.endwithme.aimexpert.gui.GuiTheme;
import com.endwithme.aimexpert.render.RenderUtils;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Pixel painting board: the player draws the dots of a crosshair pattern directly.
 * Left click paints, right click erases, changes are written into the active template immediately.
 */
@SideOnly(Side.CLIENT)
public class PatternBoardWidget extends Widget
{
    private static final int MAX_CELL = 18;
    private static final int MAX_BOARD = 200;
    private static final int MIN_CELL = 6;

    private static boolean[][] copied;

    private CrosshairType type = CrosshairType.NORMAL;
    private int size = 9;
    private final boolean[][] cells = new boolean[CrosshairPattern.MAX_SIZE][CrosshairPattern.MAX_SIZE];
    private boolean mirror = true;
    private boolean painting;
    private boolean erasing;
    private int hoverColumn = -1;
    private int hoverRow = -1;

    public PatternBoardWidget(String label)
    {
        super(label);
        this.loadFromTemplate();
    }

    public CrosshairType getType()
    {
        return this.type;
    }

    public void setType(CrosshairType type)
    {
        if (type != null && type != this.type)
        {
            this.type = type;
            this.loadFromTemplate();
        }
    }

    public int getSize()
    {
        return this.size;
    }

    public void setSize(int newSize)
    {
        newSize = AimSettings.clamp(newSize, 1, CrosshairPattern.MAX_SIZE);
        if (newSize == this.size)
        {
            return;
        }

        boolean[][] previous = this.copyCells();
        int previousSize = this.size;
        this.clearCells();
        int shift = (previousSize - newSize) / 2;

        for (int row = 0; row < newSize; row++)
        {
            for (int column = 0; column < newSize; column++)
            {
                int sourceRow = row + shift;
                int sourceColumn = column + shift;
                if (sourceRow >= 0 && sourceRow < previousSize && sourceColumn >= 0 && sourceColumn < previousSize)
                {
                    this.cells[row][column] = previous[sourceRow][sourceColumn];
                }
            }
        }

        this.size = newSize;
        this.applyToTemplate();
    }

    public boolean isMirror()
    {
        return this.mirror;
    }

    public void setMirror(boolean mirror)
    {
        this.mirror = mirror;
    }

    public void clear()
    {
        this.clearCells();
        this.applyToTemplate();
    }

    public void invert()
    {
        for (int row = 0; row < this.size; row++)
        {
            for (int column = 0; column < this.size; column++)
            {
                this.cells[row][column] = !this.cells[row][column];
            }
        }
        this.applyToTemplate();
    }

    public void copyShape()
    {
        copied = this.copyCells();
    }

    public boolean pasteShape()
    {
        if (copied == null)
        {
            return false;
        }

        this.clearCells();
        for (int row = 0; row < CrosshairPattern.MAX_SIZE && row < this.size; row++)
        {
            for (int column = 0; column < CrosshairPattern.MAX_SIZE && column < this.size; column++)
            {
                this.cells[row][column] = copied[row][column];
            }
        }
        this.applyToTemplate();
        return true;
    }

    public boolean hasDots()
    {
        for (int row = 0; row < this.size; row++)
        {
            for (int column = 0; column < this.size; column++)
            {
                if (this.cells[row][column])
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getHeight()
    {
        return this.size * this.cellSize() + 8;
    }

    private int cellSize()
    {
        int cell = Math.min(MAX_CELL, MAX_BOARD / Math.max(1, this.size));
        return Math.max(MIN_CELL, cell);
    }

    private int boardX()
    {
        int boardWidth = this.size * this.cellSize();
        return this.x + (this.width - boardWidth) / 2;
    }

    private int boardY()
    {
        return this.y + 4;
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        int cell = this.cellSize();
        int boardX = this.boardX();
        int boardY = this.boardY();
        int boardSize = this.size * cell;

        this.hoverColumn = -1;
        this.hoverRow = -1;
        if (this.isInside(mouseX, mouseY, boardX, boardY, boardSize, boardSize))
        {
            this.hoverColumn = (mouseX - boardX) / cell;
            this.hoverRow = (mouseY - boardY) / cell;
        }

        RenderUtils.roundedRect(boardX - 2, boardY - 2, boardSize + 4, boardSize + 4, 3.0F, 0xB3101010);
        RenderUtils.roundedOutline(boardX - 2, boardY - 2, boardSize + 4, boardSize + 4, 3.0F, 1.0F, 0x33FFFFFF);

        int color = AimSettings.getColor(this.type.name());

        for (int row = 0; row < this.size; row++)
        {
            for (int column = 0; column < this.size; column++)
            {
                int cellX = boardX + column * cell;
                int cellY = boardY + row * cell;
                RenderUtils.rect(cellX, cellY, cell - 1, cell - 1, 0x1AFFFFFF);

                if (this.cells[row][column])
                {
                    RenderUtils.rect(cellX, cellY, cell - 1, cell - 1, 0xFF000000 | color);
                }
                else if (column == this.hoverColumn && row == this.hoverRow)
                {
                    RenderUtils.rect(cellX, cellY, cell - 1, cell - 1, 0x40FFFFFF);
                }
            }
        }

        int center = this.size / 2 * cell;
        RenderUtils.rect(boardX, boardY + center, boardSize, 1, 0x40FFFFFF);
        RenderUtils.rect(boardX + center, boardY, 1, boardSize, 0x40FFFFFF);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        int cell = this.cellSize();
        int boardX = this.boardX();
        int boardY = this.boardY();
        int boardSize = this.size * cell;

        if (!this.isInside(mouseX, mouseY, boardX, boardY, boardSize, boardSize))
        {
            return false;
        }

        if (button == 0)
        {
            this.painting = true;
            this.erasing = false;
            this.paint(mouseX, mouseY, true);
        }
        else if (button == 1)
        {
            this.painting = false;
            this.erasing = true;
            this.paint(mouseX, mouseY, false);
        }
        return true;
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int button)
    {
        if (this.painting)
        {
            this.paint(mouseX, mouseY, true);
        }
        else if (this.erasing)
        {
            this.paint(mouseX, mouseY, false);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        this.painting = false;
        this.erasing = false;
    }

    private void paint(int mouseX, int mouseY, boolean value)
    {
        int cell = this.cellSize();
        int column = (mouseX - this.boardX()) / cell;
        int row = (mouseY - this.boardY()) / cell;
        if (column < 0 || row < 0 || column >= this.size || row >= this.size)
        {
            return;
        }

        this.cells[row][column] = value;

        if (this.mirror)
        {
            int mirroredColumn = this.size - 1 - column;
            int mirroredRow = this.size - 1 - row;
            this.cells[row][mirroredColumn] = value;
            this.cells[mirroredRow][column] = value;
            this.cells[mirroredRow][mirroredColumn] = value;
        }

        this.applyToTemplate();
    }

    private void clearCells()
    {
        for (int row = 0; row < CrosshairPattern.MAX_SIZE; row++)
        {
            for (int column = 0; column < CrosshairPattern.MAX_SIZE; column++)
            {
                this.cells[row][column] = false;
            }
        }
    }

    private boolean[][] copyCells()
    {
        boolean[][] copy = new boolean[CrosshairPattern.MAX_SIZE][CrosshairPattern.MAX_SIZE];
        for (int row = 0; row < CrosshairPattern.MAX_SIZE; row++)
        {
            System.arraycopy(this.cells[row], 0, copy[row], 0, CrosshairPattern.MAX_SIZE);
        }
        return copy;
    }

    private void loadFromTemplate()
    {
        CrosshairPattern pattern = TemplateManager.getActive().getPattern(this.type);
        this.clearCells();

        int columns = Math.min(CrosshairPattern.MAX_SIZE, Math.max(1, pattern.getColumns()));
        int rows = Math.min(CrosshairPattern.MAX_SIZE, Math.max(1, pattern.getRowCount()));

        for (int row = 0; row < rows; row++)
        {
            for (int column = 0; column < columns; column++)
            {
                this.cells[row][column] = pattern.isDot(column, row);
            }
        }

        this.size = Math.max(columns, rows);
    }

    private void applyToTemplate()
    {
        String[] rows = new String[this.size];
        for (int row = 0; row < this.size; row++)
        {
            StringBuilder builder = new StringBuilder();
            for (int column = 0; column < this.size; column++)
            {
                builder.append(this.cells[row][column] ? CrosshairPattern.DOT : '.');
            }
            rows[row] = builder.toString();
        }
        TemplateManager.getActive().setPattern(this.type, new CrosshairPattern(rows));
    }
}
