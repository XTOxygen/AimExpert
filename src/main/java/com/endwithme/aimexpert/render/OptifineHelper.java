package com.endwithme.aimexpert.render;

import java.lang.reflect.Method;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Optional Optifine integration. Everything is resolved reflectively so the mod never
 * depends on Optifine being installed and stays usable when it is absent.
 */
@SideOnly(Side.CLIENT)
public final class OptifineHelper
{
    private static boolean resolved = false;
    private static Method shadersMethod;

    private OptifineHelper()
    {
    }

    public static boolean isShadersEnabled()
    {
        if (!resolved)
        {
            resolved = true;
            shadersMethod = findMethod();
        }

        if (shadersMethod == null)
        {
            return false;
        }

        try
        {
            Object result = shadersMethod.invoke(null);
            return result instanceof Boolean && ((Boolean)result).booleanValue();
        }
        catch (Throwable t)
        {
            shadersMethod = null;
            return false;
        }
    }

    private static Method findMethod()
    {
        String[][] candidates = new String[][] {
            {"net.optifine.Config", "isShaders"},
            {"Config", "isShaders"},
            {"net.optifine.shaders.Shaders", "isShaderPackLoaded"},
            {"shadersmod.client.Shaders", "isShaderPackLoaded"}
        };

        for (String[] candidate : candidates)
        {
            try
            {
                Class<?> type = Class.forName(candidate[0]);
                Method method = type.getMethod(candidate[1]);
                if (method.getReturnType() == Boolean.TYPE)
                {
                    return method;
                }
            }
            catch (Throwable t)
            {
                // Optifine is not present under this name, try the next one.
            }
        }
        return null;
    }
}
