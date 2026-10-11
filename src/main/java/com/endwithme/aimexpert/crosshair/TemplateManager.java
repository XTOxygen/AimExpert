package com.endwithme.aimexpert.crosshair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.endwithme.aimexpert.config.AimSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class TemplateManager
{
    public static final String LOCAL = "Local";
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final Map<String, CrosshairTemplate> TEMPLATES = new LinkedHashMap<String, CrosshairTemplate>();
    private static File directory;
    private static CrosshairTemplate active = new CrosshairTemplate(LOCAL);

    private TemplateManager()
    {
    }

    public static void init(File configDirectory)
    {
        directory = new File(configDirectory, "aimexpert/templates");
        if (!directory.exists())
        {
            directory.mkdirs();
        }

        TEMPLATES.clear();
        TEMPLATES.put(LOCAL, buildLocal());
        loadFiles();

        CrosshairTemplate template = TEMPLATES.get(AimSettings.templateName);
        if (template == null)
        {
            template = TEMPLATES.get(LOCAL);
            AimSettings.templateName = LOCAL;
        }
        active = template.copy(template.getName());
    }

    public static CrosshairTemplate getActive()
    {
        return active;
    }

    public static String getActiveName()
    {
        return active.getName();
    }

    public static List<String> getNames()
    {
        return new ArrayList<String>(TEMPLATES.keySet());
    }

    public static boolean exists(String name)
    {
        return TEMPLATES.containsKey(name);
    }

    public static boolean isLocal(String name)
    {
        return LOCAL.equals(name);
    }

    public static CrosshairTemplate get(String name)
    {
        return TEMPLATES.get(name);
    }

    /** Applies the shapes, size and opacity of a template to the live settings. */
    public static boolean apply(String name)
    {
        CrosshairTemplate template = TEMPLATES.get(name);
        if (template == null)
        {
            return false;
        }

        active = template.copy(template.getName());
        AimSettings.templateName = template.getName();
        AimSettings.crosshairSize = template.getSize();
        AimSettings.crosshairOpacity = template.getOpacity();
        AimSettings.save();
        return true;
    }

    /**
     * Saves the drawn patterns under a usable name. The built in name and an empty name are turned
     * into a fresh unique name so a drawing session never fails on the built in template.
     */
    public static String saveDrawing(String preferredName)
    {
        String name = sanitizeName(preferredName);
        if (name.isEmpty() || LOCAL.equals(name))
        {
            name = uniqueName(LOCAL + "-1");
        }

        CrosshairTemplate template = buildFromCurrent(name);
        TEMPLATES.put(name, template);
        active = template.copy(name);
        AimSettings.templateName = name;
        AimSettings.crosshairSize = template.getSize();
        AimSettings.crosshairOpacity = template.getOpacity();
        AimSettings.save();
        writeFile(template);
        return name;
    }

    /** Stores the current crosshair state under the given name. */
    public static boolean save(String name)
    {        String clean = sanitizeName(name);
        if (clean.isEmpty() || LOCAL.equals(clean))
        {
            return false;
        }

        CrosshairTemplate template = buildFromCurrent(clean);
        TEMPLATES.put(clean, template);
        active = template.copy(clean);
        AimSettings.templateName = clean;
        AimSettings.save();
        writeFile(template);
        return true;
    }

    public static boolean delete(String name)
    {
        if (name == null || LOCAL.equals(name) || !TEMPLATES.containsKey(name))
        {
            return false;
        }

        TEMPLATES.remove(name);
        File file = fileFor(name);
        if (file != null && file.isFile())
        {
            file.delete();
        }

        if (name.equals(active.getName()))
        {
            apply(LOCAL);
        }
        return true;
    }

    public static CrosshairTemplate buildFromCurrent(String name)
    {
        CrosshairTemplate template = new CrosshairTemplate(name);
        template.setSize(AimSettings.crosshairSize);
        template.setOpacity(AimSettings.crosshairOpacity);
        for (CrosshairType type : CrosshairType.values())
        {
            template.setPattern(type, active.getPattern(type));
        }
        return template;
    }

    public static String toJson(CrosshairTemplate template)
    {
        return GSON.toJson(template);
    }

    public static CrosshairTemplate fromJson(String json)
    {
        if (json == null || json.trim().isEmpty())
        {
            return null;
        }

        try
        {
            CrosshairTemplate template = GSON.fromJson(json, CrosshairTemplate.class);
            if (template == null)
            {
                return null;
            }
            template.fillMissingPatterns();
            return template;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /** Imports a template from JSON text, storing it as a new template. */
    public static CrosshairTemplate importJson(String json)
    {
        CrosshairTemplate template = fromJson(json);
        if (template == null)
        {
            return null;
        }

        String name = sanitizeName(template.getName());
        if (name.isEmpty() || LOCAL.equals(name))
        {
            name = uniqueName(LOCAL + "-1");
        }
        else if (TEMPLATES.containsKey(name))
        {
            name = uniqueName(name);
        }

        template.setName(name);
        template.fillMissingPatterns();
        TEMPLATES.put(name, template);
        active = template.copy(name);
        AimSettings.templateName = name;
        AimSettings.crosshairSize = template.getSize();
        AimSettings.crosshairOpacity = template.getOpacity();
        AimSettings.save();
        writeFile(template);
        return template;
    }

    private static String uniqueName(String base)
    {
        String name = base;
        int index = 2;
        while (TEMPLATES.containsKey(name))
        {
            name = base + "-" + index;
            index++;
        }
        return name;
    }

    public static String sanitizeName(String name)
    {
        if (name == null)
        {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        String trimmed = name.trim();
        for (int i = 0; i < trimmed.length() && builder.length() < 24; i++)
        {
            char c = trimmed.charAt(i);
            if (c < ' ' || c == 0x7F)
            {
                continue;
            }
            builder.append(c);
        }
        return builder.toString().trim();
    }

    private static CrosshairTemplate buildLocal()
    {
        CrosshairTemplate template = new CrosshairTemplate(LOCAL);
        template.setSize(100);
        template.setOpacity(100);
        for (CrosshairType type : CrosshairType.values())
        {
            template.setPattern(type, DefaultPatterns.get(type));
        }
        return template;
    }

    private static void loadFiles()
    {
        File[] files = directory.listFiles();
        if (files == null)
        {
            return;
        }

        for (File file : files)
        {
            if (!file.isFile() || !file.getName().toLowerCase().endsWith(".json"))
            {
                continue;
            }

            CrosshairTemplate template = readFile(file);
            if (template == null)
            {
                continue;
            }

            String name = sanitizeName(template.getName());
            if (name.isEmpty() || LOCAL.equals(name))
            {
                continue;
            }

            template.setName(name);
            template.fillMissingPatterns();
            TEMPLATES.put(name, template);
        }
    }

    private static CrosshairTemplate readFile(File file)
    {
        Reader reader = null;
        try
        {
            reader = new InputStreamReader(new FileInputStream(file), UTF8);
            return GSON.fromJson(reader, CrosshairTemplate.class);
        }
        catch (Exception e)
        {
            return null;
        }
        finally
        {
            close(reader);
        }
    }

    private static void writeFile(CrosshairTemplate template)
    {
        File file = fileFor(template.getName());
        if (file == null)
        {
            return;
        }

        Writer writer = null;
        try
        {
            writer = new OutputStreamWriter(new FileOutputStream(file), UTF8);
            writer.write(GSON.toJson(template));
            writer.flush();
        }
        catch (Exception e)
        {
            // Saving failures are not fatal, the template stays available in memory.
        }
        finally
        {
            close(writer);
        }
    }

    private static File fileFor(String name)
    {
        if (directory == null || name == null || name.isEmpty())
        {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++)
        {
            char c = name.charAt(i);
            builder.append(Character.isLetterOrDigit(c) || c == '_' || c == '-' ? c : '_');
        }
        return new File(directory, builder.toString() + "_" + Integer.toHexString(name.hashCode()) + ".json");
    }

    private static void close(java.io.Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (Exception e)
            {
                // ignored
            }
        }
    }
}
