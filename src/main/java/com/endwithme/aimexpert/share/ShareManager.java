package com.endwithme.aimexpert.share;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.endwithme.aimexpert.Reference;
import com.endwithme.aimexpert.config.AimSettings;
import com.endwithme.aimexpert.crosshair.CrosshairTemplate;
import com.endwithme.aimexpert.crosshair.TemplateManager;
import com.endwithme.aimexpert.i18n.Lang;
import com.endwithme.aimexpert.net.PacketHandler;
import com.endwithme.aimexpert.net.PacketHello;
import com.endwithme.aimexpert.net.PacketShareOffer;
import com.endwithme.aimexpert.net.PacketShareRequest;
import com.endwithme.aimexpert.net.PacketShareResponse;
import com.endwithme.aimexpert.net.PacketShareResult;
import com.endwithme.aimexpert.util.ChatHelper;
import com.endwithme.aimexpert.util.ClipboardHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ShareManager
{
    private static final long HELLO_TIMEOUT = 4000L;
    private static final long HELLO_DELAY = 700L;
    private static final int MAX_HELLO_ATTEMPTS = 2;
    private static final long OFFER_LIFETIME = 300000L;

    private static final Map<Integer, PendingOffer> PENDING = new LinkedHashMap<Integer, PendingOffer>();

    private static boolean helloAcked = false;
    private static boolean helloPending = false;
    private static int helloAttempts = 0;
    private static long helloSentAt = 0L;
    private static long joinedAt = 0L;

    private ShareManager()
    {
    }

    public static void reset()
    {
        PENDING.clear();
        helloAcked = false;
        helloPending = false;
        helloAttempts = 0;
        helloSentAt = 0L;
        joinedAt = 0L;
    }

    public static void tick(Minecraft mc)
    {
        if (mc.thePlayer == null)
        {
            if (joinedAt != 0L || !PENDING.isEmpty())
            {
                reset();
            }
            return;
        }

        long now = System.currentTimeMillis();
        if (joinedAt == 0L)
        {
            joinedAt = now;
        }

        if (!helloAcked && !helloPending && helloAttempts < MAX_HELLO_ATTEMPTS && now - joinedAt > HELLO_DELAY)
        {
            helloPending = true;
            helloAttempts++;
            helloSentAt = now;
            PacketHandler.NETWORK.sendToServer(new PacketHello(Reference.VERSION));
        }

        if (helloPending && now - helloSentAt > HELLO_TIMEOUT)
        {
            helloPending = false;
        }

        Iterator<Map.Entry<Integer, PendingOffer>> iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext())
        {
            if (now - iterator.next().getValue().getTime() > OFFER_LIFETIME)
            {
                iterator.remove();
            }
        }
    }

    public static void setServerSupported(boolean supported)
    {
        helloAcked = supported;
        helloPending = false;
    }

    public static boolean isServerReady()
    {
        return helloAcked;
    }

    public static boolean isHelloPending()
    {
        return helloPending;
    }

    public static List<PendingOffer> getPendingOffers()
    {
        return new ArrayList<PendingOffer>(PENDING.values());
    }

    public static int getPendingCount()
    {
        return PENDING.size();
    }

    public static void addOffer(PacketShareOffer message)
    {
        if (message == null || message.json == null || message.json.isEmpty())
        {
            return;
        }

        PENDING.put(Integer.valueOf(message.offerId), new PendingOffer(message.offerId, message.senderName, message.templateName, message.json, System.currentTimeMillis()));

        IChatComponent name = new ChatComponentText(message.senderName);
        name.getChatStyle().setColor(EnumChatFormatting.AQUA);

        IChatComponent root = new ChatComponentText("");
        root.appendSibling(name);
        root.appendSibling(new ChatComponentText(Lang.get("share.offerText")));

        IChatComponent accept = new ChatComponentText(" [" + Lang.get("share.accept") + "]");
        accept.getChatStyle().setColor(EnumChatFormatting.GREEN);
        accept.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/aimexpert accept " + message.offerId));
        accept.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(Lang.get("share.acceptHover"))));

        IChatComponent decline = new ChatComponentText(" [" + Lang.get("share.decline") + "]");
        decline.getChatStyle().setColor(EnumChatFormatting.RED);
        decline.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/aimexpert decline " + message.offerId));
        decline.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(Lang.get("share.declineHover"))));

        root.appendSibling(accept);
        root.appendSibling(decline);
        ChatHelper.send(root);
    }

    public static void accept(int offerId)
    {
        PendingOffer offer = PENDING.remove(Integer.valueOf(offerId));
        if (offer == null)
        {
            ChatHelper.send(Lang.get("share.gone"));
            return;
        }

        CrosshairTemplate template = TemplateManager.importJson(offer.getJson());
        if (template == null)
        {
            ChatHelper.send(Lang.get("template.invalid"));
        }
        else
        {
            ChatHelper.send(Lang.get("share.accepted") + ": " + template.getName());
        }

        PacketHandler.NETWORK.sendToServer(new PacketShareResponse(offerId, true));
    }

    public static void decline(int offerId)
    {
        PendingOffer offer = PENDING.remove(Integer.valueOf(offerId));
        if (offer == null)
        {
            ChatHelper.send(Lang.get("share.gone"));
            return;
        }

        ChatHelper.send(Lang.get("share.declined") + ": " + offer.getTemplateName());
        PacketHandler.NETWORK.sendToServer(new PacketShareResponse(offerId, false));
    }

    public static void acceptLatest()
    {
        PendingOffer offer = getOldest();
        if (offer == null)
        {
            ChatHelper.send(Lang.get("share.gone"));
            return;
        }
        accept(offer.getId());
    }

    public static void declineLatest()
    {
        PendingOffer offer = getOldest();
        if (offer == null)
        {
            ChatHelper.send(Lang.get("share.gone"));
            return;
        }
        decline(offer.getId());
    }

    public static PendingOffer getOldest()
    {
        Iterator<PendingOffer> iterator = PENDING.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    public static String formatOffer(PendingOffer offer)
    {
        if (offer == null)
        {
            return "";
        }
        return offer.getSender() + " - " + offer.getTemplateName();
    }

    public static boolean share(String templateName, String recipient)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null)
        {
            ChatHelper.send(Lang.get("command.noServer"));
            return false;
        }

        CrosshairTemplate template = TemplateManager.get(templateName);
        if (template == null)
        {
            ChatHelper.send(Lang.get("command.unknownTemplate", templateName));
            return false;
        }

        if (!helloAcked)
        {
            ChatHelper.send(Lang.get(helloPending ? "share.wait" : "share.unsupported"));
            return false;
        }

        String json = buildShareJson(templateName);
        if (json == null)
        {
            return false;
        }

        AimSettings.lastRecipient = recipient == null ? "" : recipient;
        AimSettings.save();
        PacketHandler.NETWORK.sendToServer(new PacketShareRequest(templateName, json, recipient == null ? "" : recipient));
        return true;
    }

    /** Builds the JSON payload of a template, keeping the live size and opacity for the active one. */
    public static String buildShareJson(String templateName)
    {
        CrosshairTemplate template = TemplateManager.get(templateName);
        if (template == null)
        {
            return null;
        }

        CrosshairTemplate copy = template.copy(templateName);
        if (templateName.equals(TemplateManager.getActiveName()))
        {
            copy.setSize(AimSettings.crosshairSize);
            copy.setOpacity(AimSettings.crosshairOpacity);
        }
        return TemplateManager.toJson(copy);
    }

    public static void copyJsonToClipboard(String templateName)
    {
        String json = buildShareJson(templateName);
        if (json == null)
        {
            ChatHelper.send(Lang.get("command.unknownTemplate", templateName));
            return;
        }

        try
        {
            ClipboardHelper.write(json);
            ChatHelper.send(Lang.get("template.copied"));
        }
        catch (Throwable t)
        {
            ChatHelper.send(Lang.get("template.copyFailed"));
        }
    }

    public static void importJsonFromClipboard()
    {
        String json = ClipboardHelper.read();

        if (json == null || json.trim().isEmpty())
        {
            ChatHelper.send(Lang.get("template.clipboardEmpty"));
            return;
        }

        CrosshairTemplate template = TemplateManager.importJson(json);
        if (template == null)
        {
            ChatHelper.send(Lang.get("template.invalid"));
            return;
        }

        ChatHelper.send(Lang.get("template.imported", template.getName()));
    }

    public static void onResult(PacketShareResult message)
    {
        if (message == null)
        {
            return;
        }

        switch (message.result)
        {
            case PacketShareResult.SINGLE:
                ChatHelper.send(Lang.get("share.sent", message.templateName, message.name));
                break;
            case PacketShareResult.ALL:
                ChatHelper.send(Lang.get("share.sentAll", message.templateName, Integer.toString(message.count)));
                break;
            case PacketShareResult.NO_RECIPIENT:
                ChatHelper.send(Lang.get("share.noRecipient"));
                break;
            case PacketShareResult.ACCEPTED:
                ChatHelper.send(Lang.get("share.acceptedBy", message.name, message.templateName));
                break;
            case PacketShareResult.DECLINED:
                ChatHelper.send(Lang.get("share.declinedBy", message.name, message.templateName));
                break;
            default:
                break;
        }
    }
}

