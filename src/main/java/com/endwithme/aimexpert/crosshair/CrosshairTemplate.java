package com.endwithme.aimexpert.crosshair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.endwithme.aimexpert.config.AimSettings;

/**
 * A shareable crosshair template. Templates only describe the shapes plus size and opacity,
 * the per type colors stay local to every user.
 */
public class CrosshairTemplate
{
    public static final int FORMAT_VERSION = 1;

    private String name = "Local";
    private int formatVersion = FORMAT_VERSION;
    private int size = 100;
    private int opacity = 100;
    private Map<String, List<String>> patterns = new LinkedHashMap<String, List<String>>();

    public CrosshairTemplate()
    {
    }

    public CrosshairTemplate(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getFormatVersion()
    {
        return this.formatVersion;
    }

    public int getSize()
    {
        return this.size;
    }

    public void setSize(int size)
    {
        this.size = AimSettings.clamp(size, 30, 400);
    }

    public int getOpacity()
    {
        return this.opacity;
    }

    public void setOpacity(int opacity)
    {
        this.opacity = AimSettings.clamp(opacity, 10, 100);
    }

    public Map<String, List<String>> getPatterns()
    {
        return this.patterns;
    }

    public CrosshairPattern getPattern(CrosshairType type)
    {
        List<String> rows = this.patterns.get(type.name());
        if (rows == null || rows.isEmpty())
        {
            return DefaultPatterns.get(type);
        }
        return CrosshairPattern.of(rows);
    }

    public void setPattern(CrosshairType type, CrosshairPattern pattern)
    {
        this.patterns.put(type.name(), pattern.asList());
    }

    public void fillMissingPatterns()
    {
        for (CrosshairType type : CrosshairType.values())
        {
            if (!this.patterns.containsKey(type.name()))
            {
                this.patterns.put(type.name(), DefaultPatterns.get(type).asList());
            }
        }
    }

    public CrosshairTemplate copy(String newName)
    {
        CrosshairTemplate template = new CrosshairTemplate(newName);
        template.size = this.size;
        template.opacity = this.opacity;
        for (CrosshairType type : CrosshairType.values())
        {
            template.patterns.put(type.name(), new ArrayList<String>(this.getPattern(type).asList()));
        }
        return template;
    }
}
