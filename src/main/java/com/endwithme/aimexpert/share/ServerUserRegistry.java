package com.endwithme.aimexpert.share;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/** Tracks which players on a server run AimExpert. */
public final class ServerUserRegistry
{
    private static final Map<UUID, String> USERS = new ConcurrentHashMap<UUID, String>();

    private ServerUserRegistry()
    {
    }

    public static void add(EntityPlayerMP player)
    {
        if (player != null)
        {
            USERS.put(player.getUniqueID(), player.getName());
        }
    }

    public static void remove(EntityPlayerMP player)
    {
        if (player != null)
        {
            USERS.remove(player.getUniqueID());
        }
    }

    public static boolean isRegistered(EntityPlayerMP player)
    {
        return player != null && USERS.containsKey(player.getUniqueID());
    }

    public static List<EntityPlayerMP> getRegisteredPlayers(MinecraftServer server, UUID excluded)
    {
        List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>();
        if (server == null)
        {
            return players;
        }

        for (EntityPlayerMP player : server.getConfigurationManager().playerEntityList)
        {
            if (player == null || excluded != null && excluded.equals(player.getUniqueID()))
            {
                continue;
            }

            if (USERS.containsKey(player.getUniqueID()))
            {
                players.add(player);
            }
        }
        return players;
    }

    public static EntityPlayerMP findByName(MinecraftServer server, String name)
    {
        if (server == null || name == null || name.isEmpty())
        {
            return null;
        }

        for (EntityPlayerMP player : server.getConfigurationManager().playerEntityList)
        {
            if (player != null && name.equalsIgnoreCase(player.getName()))
            {
                return player;
            }
        }
        return null;
    }

    public static EntityPlayerMP findByUUID(MinecraftServer server, UUID uuid)
    {
        if (server == null || uuid == null)
        {
            return null;
        }

        for (EntityPlayerMP player : server.getConfigurationManager().playerEntityList)
        {
            if (player != null && uuid.equals(player.getUniqueID()))
            {
                return player;
            }
        }
        return null;
    }

    public static void clear()
    {
        USERS.clear();
    }
}
