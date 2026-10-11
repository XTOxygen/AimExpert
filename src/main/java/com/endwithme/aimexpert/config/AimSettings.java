package com.endwithme.aimexpert.config;

import java.io.File;
import java.util.Locale;

import com.endwithme.aimexpert.i18n.Lang;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class AimSettings
{
    public static final String CAT_GENERAL = "general";
    public static final String CAT_CROSSHAIR = "crosshair";
    public static final String CAT_DYNAMIC = "dynamic";
    public static final String CAT_TEMPLATE = "template";
    public static final String CAT_SHARE = "share";

    private static Configuration config;

    public static String language = Lang.EN_US;
    public static boolean blurEnabled = true;
    public static int blurStrength = 3;
    public static boolean animationsEnabled = true;

    public static boolean useCustomCrosshair = true;
    public static boolean hideCrosshair = false;
    public static boolean showNormal = true;
    public static boolean showFriendly = true;
    public static boolean showHostile = true;
    public static boolean showFov = true;
    public static boolean showAttack = true;
    public static String playerCrosshair = "HOSTILE";
    public static int colorNormal = 0xFFFFFF;
    public static int colorFriendly = 0x55FF55;
    public static int colorHostile = 0xFF5555;
    public static int colorFov = 0x55FFFF;
    public static int colorAttack = 0xFFAA00;
    public static int crosshairSize = 100;
    public static int crosshairOpacity = 100;
    public static int crosshairRange = 8;

    public static boolean dynamicEnabled = true;
    public static boolean dynamicFov = true;
    public static boolean dynamicAttack = true;
    public static int dynamicAmount = 150;
    public static int dynamicSpeed = 35;
    public static boolean switchAnimation = true;
    public static int switchDuration = 220;
    public static boolean switchSmallToLarge = true;

    public static String templateName = "Local";
    public static String lastRecipient = "";

    private AimSettings()
    {
    }

    public static void load(File file)
    {
        config = new Configuration(file);
        config.load();

        language = config.get(CAT_GENERAL, "language", Lang.EN_US).getString();
        blurEnabled = config.get(CAT_GENERAL, "blurEnabled", true).getBoolean();
        blurStrength = clamp(config.get(CAT_GENERAL, "blurStrength", 3).getInt(), 1, 6);
        animationsEnabled = config.get(CAT_GENERAL, "animationsEnabled", true).getBoolean();

        useCustomCrosshair = config.get(CAT_CROSSHAIR, "useCustomCrosshair", true).getBoolean();
        hideCrosshair = config.get(CAT_CROSSHAIR, "hideCrosshair", false).getBoolean();
        showNormal = config.get(CAT_CROSSHAIR, "showNormal", true).getBoolean();
        showFriendly = config.get(CAT_CROSSHAIR, "showFriendly", true).getBoolean();
        showHostile = config.get(CAT_CROSSHAIR, "showHostile", true).getBoolean();
        showFov = config.get(CAT_CROSSHAIR, "showFov", true).getBoolean();
        showAttack = config.get(CAT_CROSSHAIR, "showAttack", true).getBoolean();
        playerCrosshair = config.get(CAT_CROSSHAIR, "playerCrosshair", "HOSTILE").getString();
        colorNormal = parseColor(config.get(CAT_CROSSHAIR, "colorNormal", "FFFFFF").getString(), 0xFFFFFF);
        colorFriendly = parseColor(config.get(CAT_CROSSHAIR, "colorFriendly", "55FF55").getString(), 0x55FF55);
        colorHostile = parseColor(config.get(CAT_CROSSHAIR, "colorHostile", "FF5555").getString(), 0xFF5555);
        colorFov = parseColor(config.get(CAT_CROSSHAIR, "colorFov", "55FFFF").getString(), 0x55FFFF);
        colorAttack = parseColor(config.get(CAT_CROSSHAIR, "colorAttack", "FFAA00").getString(), 0xFFAA00);
        crosshairSize = clamp(config.get(CAT_CROSSHAIR, "crosshairSize", 100).getInt(), 30, 400);
        crosshairOpacity = clamp(config.get(CAT_CROSSHAIR, "crosshairOpacity", 100).getInt(), 10, 100);
        crosshairRange = clamp(config.get(CAT_CROSSHAIR, "crosshairRange", 8).getInt(), 3, 32);

        dynamicEnabled = config.get(CAT_DYNAMIC, "dynamicEnabled", true).getBoolean();
        dynamicFov = config.get(CAT_DYNAMIC, "dynamicFov", true).getBoolean();
        dynamicAttack = config.get(CAT_DYNAMIC, "dynamicAttack", true).getBoolean();
        dynamicAmount = clamp(config.get(CAT_DYNAMIC, "dynamicAmount", 150).getInt(), 100, 300);
        dynamicSpeed = clamp(config.get(CAT_DYNAMIC, "dynamicSpeed", 35).getInt(), 5, 100);
        switchAnimation = config.get(CAT_DYNAMIC, "switchAnimation", true).getBoolean();
        switchDuration = clamp(config.get(CAT_DYNAMIC, "switchDuration", 220).getInt(), 60, 800);
        switchSmallToLarge = config.get(CAT_DYNAMIC, "switchSmallToLarge", true).getBoolean();

        templateName = config.get(CAT_TEMPLATE, "templateName", "Local").getString();
        lastRecipient = config.get(CAT_SHARE, "lastRecipient", "").getString();

        Lang.setLanguage(language);

        if (config.hasChanged())
        {
            config.save();
        }
    }

    public static void save()
    {
        if (config == null)
        {
            return;
        }

        set(CAT_GENERAL, "language", language);
        set(CAT_GENERAL, "blurEnabled", blurEnabled);
        set(CAT_GENERAL, "blurStrength", blurStrength);
        set(CAT_GENERAL, "animationsEnabled", animationsEnabled);

        set(CAT_CROSSHAIR, "useCustomCrosshair", useCustomCrosshair);
        set(CAT_CROSSHAIR, "hideCrosshair", hideCrosshair);
        set(CAT_CROSSHAIR, "showNormal", showNormal);
        set(CAT_CROSSHAIR, "showFriendly", showFriendly);
        set(CAT_CROSSHAIR, "showHostile", showHostile);
        set(CAT_CROSSHAIR, "showFov", showFov);
        set(CAT_CROSSHAIR, "showAttack", showAttack);
        set(CAT_CROSSHAIR, "playerCrosshair", playerCrosshair);
        set(CAT_CROSSHAIR, "colorNormal", toHex(colorNormal));
        set(CAT_CROSSHAIR, "colorFriendly", toHex(colorFriendly));
        set(CAT_CROSSHAIR, "colorHostile", toHex(colorHostile));
        set(CAT_CROSSHAIR, "colorFov", toHex(colorFov));
        set(CAT_CROSSHAIR, "colorAttack", toHex(colorAttack));
        set(CAT_CROSSHAIR, "crosshairSize", crosshairSize);
        set(CAT_CROSSHAIR, "crosshairOpacity", crosshairOpacity);
        set(CAT_CROSSHAIR, "crosshairRange", crosshairRange);

        set(CAT_DYNAMIC, "dynamicEnabled", dynamicEnabled);
        set(CAT_DYNAMIC, "dynamicFov", dynamicFov);
        set(CAT_DYNAMIC, "dynamicAttack", dynamicAttack);
        set(CAT_DYNAMIC, "dynamicAmount", dynamicAmount);
        set(CAT_DYNAMIC, "dynamicSpeed", dynamicSpeed);
        set(CAT_DYNAMIC, "switchAnimation", switchAnimation);
        set(CAT_DYNAMIC, "switchDuration", switchDuration);
        set(CAT_DYNAMIC, "switchSmallToLarge", switchSmallToLarge);

        set(CAT_TEMPLATE, "templateName", templateName);
        set(CAT_SHARE, "lastRecipient", lastRecipient);

        config.save();
    }

    public static int getColor(String type)
    {
        if ("FRIENDLY".equals(type))
        {
            return colorFriendly;
        }
        if ("HOSTILE".equals(type))
        {
            return colorHostile;
        }
        if ("FOV".equals(type))
        {
            return colorFov;
        }
        if ("ATTACK".equals(type))
        {
            return colorAttack;
        }
        return colorNormal;
    }

    public static boolean isPlayerHostile()
    {
        return "HOSTILE".equals(playerCrosshair);
    }

    public static boolean isPlayerFriendly()
    {
        return "FRIENDLY".equals(playerCrosshair);
    }

    public static boolean isPlayerIgnored()
    {
        return "IGNORE".equals(playerCrosshair);
    }

    public static void setPlayerMode(int index)
    {
        playerCrosshair = index == 1 ? "FRIENDLY" : (index == 2 ? "IGNORE" : "HOSTILE");
    }

    public static int getPlayerMode()
    {
        return isPlayerFriendly() ? 1 : (isPlayerIgnored() ? 2 : 0);
    }

    public static void setColor(String type, int rgb)
    {
        rgb &= 0xFFFFFF;
        if ("FRIENDLY".equals(type))
        {
            colorFriendly = rgb;
        }
        else if ("HOSTILE".equals(type))
        {
            colorHostile = rgb;
        }
        else if ("FOV".equals(type))
        {
            colorFov = rgb;
        }
        else if ("ATTACK".equals(type))
        {
            colorAttack = rgb;
        }
        else
        {
            colorNormal = rgb;
        }
    }

    public static boolean isEnabled(String type)
    {
        if ("FRIENDLY".equals(type))
        {
            return showFriendly;
        }
        if ("HOSTILE".equals(type))
        {
            return showHostile;
        }
        if ("FOV".equals(type))
        {
            return showFov;
        }
        if ("ATTACK".equals(type))
        {
            return showAttack;
        }
        return showNormal;
    }

    public static void setEnabled(String type, boolean value)
    {
        if ("FRIENDLY".equals(type))
        {
            showFriendly = value;
        }
        else if ("HOSTILE".equals(type))
        {
            showHostile = value;
        }
        else if ("FOV".equals(type))
        {
            showFov = value;
        }
        else if ("ATTACK".equals(type))
        {
            showAttack = value;
        }
        else
        {
            showNormal = value;
        }
    }

    private static void set(String category, String key, String value)
    {
        Property property = config.get(category, key, value);
        if (!value.equals(property.getString()))
        {
            property.set(value);
        }
    }

    private static void set(String category, String key, boolean value)
    {
        Property property = config.get(category, key, value);
        if (property.getBoolean() != value)
        {
            property.set(value);
        }
    }

    private static void set(String category, String key, int value)
    {
        Property property = config.get(category, key, value);
        if (property.getInt() != value)
        {
            property.set(value);
        }
    }

    public static String toHex(int rgb)
    {
        return String.format(Locale.ROOT, "%06X", rgb & 0xFFFFFF);
    }

    public static int parseColor(String value, int fallback)
    {
        try
        {
            return Integer.parseInt(value.trim(), 16) & 0xFFFFFF;
        }
        catch (Exception e)
        {
            return fallback;
        }
    }

    public static int clamp(int value, int min, int max)
    {
        return value < min ? min : (value > max ? max : value);
    }

    public static float clamp(float value, float min, float max)
    {
        return value < min ? min : (value > max ? max : value);
    }
}
