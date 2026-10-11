package com.endwithme.aimexpert.crosshair;

import com.endwithme.aimexpert.i18n.Lang;

public enum CrosshairType
{
    NORMAL("crosshair.type.normal"),
    FRIENDLY("crosshair.type.friendly"),
    HOSTILE("crosshair.type.hostile"),
    FOV("crosshair.type.fov"),
    ATTACK("crosshair.type.attack");

    private final String langKey;

    CrosshairType(String langKey)
    {
        this.langKey = langKey;
    }

    public String getDisplayName()
    {
        return Lang.get(this.langKey);
    }

    public String getLangKey()
    {
        return this.langKey;
    }

    public static CrosshairType byName(String name, CrosshairType fallback)
    {
        if (name != null)
        {
            for (CrosshairType type : values())
            {
                if (type.name().equalsIgnoreCase(name))
                {
                    return type;
                }
            }
        }
        return fallback;
    }
}
