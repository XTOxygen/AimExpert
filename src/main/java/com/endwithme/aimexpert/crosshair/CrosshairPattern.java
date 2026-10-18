package com.endwithme.aimexpert.crosshair;

import java.util.ArrayList;
import java.util.List;

/**
 * A crosshair shape stored as a grid of pixel dots. '#' is a dot, anything else is empty.
 * Patterns are always square and limited in size so imported templates cannot stall rendering.
 */
public class CrosshairPattern
{
    public static final int MAX_SIZE = 21;
    public static final char DOT = '#';

    private String[] rows;

    public CrosshairPattern(String[] rows)
    {
        this.rows = normalize(rows);
    }

    public static CrosshairPattern of(List<String> rows)
    {
        if (rows == null)
        {
            return new CrosshairPattern(new String[0]);
        }
        return new CrosshairPattern(rows.toArray(new String[rows.size()]));
    }

    public static CrosshairPattern empty()
    {
        return new CrosshairPattern(new String[] {"."});
    }

    public String[] getRows()
    {
        return this.rows;
    }

    public List<String> asList()
    {
        List<String> list = new ArrayList<String>();
        for (String row : this.rows)
        {
            list.add(row);
        }
        return list;
    }

    public int getColumns()
    {
        return this.rows.length == 0 ? 0 : this.rows[0].length();
    }

    public int getRowCount()
    {
        return this.rows.length;
    }

    public boolean isDot(int column, int row)
    {
        if (row < 0 || row >= this.rows.length)
        {
            return false;
        }
        String line = this.rows[row];
        return column >= 0 && column < line.length() && line.charAt(column) == DOT;
    }

    public boolean isEmpty()
    {
        for (String row : this.rows)
        {
            if (row.indexOf(DOT) >= 0)
            {
                return false;
            }
        }
        return true;
    }

    public CrosshairPattern copy()
    {
        return new CrosshairPattern(this.rows.clone());
    }

    private static String[] normalize(String[] input)
    {
        if (input == null || input.length == 0)
        {
            return new String[] {"."};
        }

        int rowCount = Math.min(input.length, MAX_SIZE);
        int width = 1;
        for (int i = 0; i < rowCount; i++)
        {
            String row = input[i] == null ? "" : input[i].trim();
            int length = Math.min(row.length(), MAX_SIZE);
            if (length > width)
            {
                width = length;
            }
        }

        String[] result = new String[rowCount];
        for (int i = 0; i < rowCount; i++)
        {
            String row = input[i] == null ? "" : input[i].trim();
            StringBuilder builder = new StringBuilder();
            for (int c = 0; c < width; c++)
            {
                builder.append(c < row.length() && row.charAt(c) == DOT ? DOT : '.');
            }
            result[i] = builder.toString();
        }
        return result;
    }
}
