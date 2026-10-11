package com.endwithme.aimexpert.share;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.endwithme.aimexpert.crosshair.CrosshairTemplate;
import com.endwithme.aimexpert.crosshair.TemplateManager;
import com.endwithme.aimexpert.net.PacketHandler;
import com.endwithme.aimexpert.net.PacketShareOffer;
import com.endwithme.aimexpert.net.PacketShareRequest;
import com.endwithme.aimexpert.net.PacketShareResponse;
import com.endwithme.aimexpert.net.PacketShareResult;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/** Server side relay of crosshair templates between players. */
public final class ShareService
{
    private static final int MAX_JSON_LENGTH = 32768;
    private static final long OFFER_LIFETIME = 600000L;
    private static final Map<Integer, Offer> OFFERS = new ConcurrentHashMap<Integer, Offer>();
    private static final AtomicInteger NEXT_OFFER_ID = new AtomicInteger(1);

    private ShareService()
    {
    }

    public static void handleRequest(EntityPlayerMP sender, PacketShareRequest message)
    {
        if (sender == null || message == null)
        {
            return;
        }

        String json = message.json;
        if (json == null || json.isEmpty() || json.length() > MAX_JSON_LENGTH)
        {
            return;
        }

        CrosshairTemplate template = TemplateManager.fromJson(json);
        if (template == null)
        {
            return;
        }

        String name = message.templateName == null || message.templateName.isEmpty() ? template.getName() : message.templateName;
        String recipient = message.recipient == null ? "" : message.recipient.trim();
        MinecraftServer server = sender.mcServer;
        List<EntityPlayerMP> targets = new ArrayList<EntityPlayerMP>();

        if (!recipient.isEmpty())
        {
            EntityPlayerMP player = ServerUserRegistry.findByName(server, recipient);
            if (player != null && player != sender && ServerUserRegistry.isRegistered(player))
            {
                targets.add(player);
            }
        }
        else
        {
            targets.addAll(ServerUserRegistry.getRegisteredPlayers(server, sender.getUniqueID()));
        }

        if (targets.isEmpty())
        {
            send(sender, new PacketShareResult(PacketShareResult.NO_RECIPIENT, name, recipient, 0));
            return;
        }

        int offerId = NEXT_OFFER_ID.getAndIncrement();
        OFFERS.put(Integer.valueOf(offerId), new Offer(sender.getUniqueID(), name, System.currentTimeMillis()));

        for (EntityPlayerMP target : targets)
        {
            PacketHandler.NETWORK.sendTo(new PacketShareOffer(offerId, sender.getName(), name, json), target);
        }

        if (targets.size() == 1)
        {
            send(sender, new PacketShareResult(PacketShareResult.SINGLE, name, targets.get(0).getName(), 1));
        }
        else
        {
            send(sender, new PacketShareResult(PacketShareResult.ALL, name, "", targets.size()));
        }

        cleanup();
    }

    public static void handleResponse(EntityPlayerMP player, PacketShareResponse message)
    {
        if (player == null || message == null)
        {
            return;
        }

        Offer offer = OFFERS.remove(Integer.valueOf(message.offerId));
        if (offer == null)
        {
            return;
        }

        EntityPlayerMP sender = ServerUserRegistry.findByUUID(player.mcServer, offer.sender);
        if (sender != null && sender != player)
        {
            byte result = message.accepted ? PacketShareResult.ACCEPTED : PacketShareResult.DECLINED;
            send(sender, new PacketShareResult(result, offer.templateName, player.getName(), 1));
        }
    }

    private static void send(EntityPlayerMP player, PacketShareResult message)
    {
        if (player != null)
        {
            PacketHandler.NETWORK.sendTo(message, player);
        }
    }

    private static void cleanup()
    {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, Offer>> iterator = OFFERS.entrySet().iterator();
        while (iterator.hasNext())
        {
            if (now - iterator.next().getValue().time > OFFER_LIFETIME)
            {
                iterator.remove();
            }
        }
    }

    private static final class Offer
    {
        private final UUID sender;
        private final String templateName;
        private final long time;

        private Offer(UUID sender, String templateName, long time)
        {
            this.sender = sender;
            this.templateName = templateName;
            this.time = time;
        }
    }
}
