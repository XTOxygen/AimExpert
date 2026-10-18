package com.endwithme.aimexpert.event;

import com.endwithme.aimexpert.share.ServerUserRegistry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class CommonEvents
{
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
            ServerUserRegistry.remove((EntityPlayerMP)event.player);
        }
    }
}
