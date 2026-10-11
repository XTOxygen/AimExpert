package com.endwithme.aimexpert.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.endwithme.aimexpert.Reference;
import com.endwithme.aimexpert.config.AimSettings;
import com.endwithme.aimexpert.crosshair.CrosshairState;
import com.endwithme.aimexpert.crosshair.CrosshairType;
import com.endwithme.aimexpert.crosshair.TemplateManager;
import com.endwithme.aimexpert.gui.widget.ButtonWidget;
import com.endwithme.aimexpert.gui.widget.ChoiceWidget;
import com.endwithme.aimexpert.gui.widget.CrosshairRowWidget;
import com.endwithme.aimexpert.gui.widget.LabelWidget;
import com.endwithme.aimexpert.gui.widget.ListWidget;
import com.endwithme.aimexpert.gui.widget.PatternBoardWidget;
import com.endwithme.aimexpert.gui.widget.RowWidget;
import com.endwithme.aimexpert.gui.widget.SliderWidget;
import com.endwithme.aimexpert.gui.widget.TextFieldWidget;
import com.endwithme.aimexpert.gui.widget.ToggleWidget;
import com.endwithme.aimexpert.i18n.Lang;
import com.endwithme.aimexpert.proxy.ClientProxy;
import com.endwithme.aimexpert.share.PendingOffer;
import com.endwithme.aimexpert.share.ShareManager;
import com.endwithme.aimexpert.util.ChatHelper;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Builds the settings cards, one card per category. */
@SideOnly(Side.CLIENT)
public final class CardBuilder
{
    private CardBuilder()
    {
    }

    public static List<Card> build()
    {
        List<Card> cards = new ArrayList<Card>();
        cards.add(buildGeneral());
        cards.add(buildCrosshair());
        cards.add(buildDrawing());
        cards.add(buildDynamic());
        cards.add(buildTemplate());
        cards.add(buildShare());
        return cards;
    }

    private static Card buildDrawing()
    {
        Card card = new Card("card.draw");

        final PatternBoardWidget board = new PatternBoardWidget("draw.board");
        final CrosshairType[] selected = new CrosshairType[] {CrosshairType.NORMAL};

        card.add(new ChoiceWidget("draw.type", new String[] {"draw.type.normal", "draw.type.friendly", "draw.type.hostile", "draw.type.fov", "draw.type.attack"},
            () -> selected[0].ordinal(),
            index ->
            {
                selected[0] = CrosshairType.values()[index];
                board.setType(selected[0]);
            }));

        card.add(new SliderWidget("draw.size",
            () -> board.getSize(),
            value -> board.setSize(value), 5, 21, ""));

        card.add(new ToggleWidget("draw.mirror",
            () -> board.isMirror(),
            value -> board.setMirror(value)));

        card.add(board);

        card.add(new LabelWidget("draw.hint"));

        card.add(new RowWidget(
            new ButtonWidget("draw.clear", () -> board.clear()),
            new ButtonWidget("draw.invert", () -> board.invert())));

        card.add(new RowWidget(
            new ButtonWidget("draw.copy", () ->
            {
                board.copyShape();
                ChatHelper.send(Lang.get("draw.copied"));
            }),
            new ButtonWidget("draw.paste", () ->
            {
                if (!board.pasteShape())
                {
                    ChatHelper.send(Lang.get("draw.nothing"));
                }
            })));

        final TextFieldWidget nameField = new TextFieldWidget("draw.name", AimSettings.templateName, 24,
            value -> AimSettings.templateName = value);
        card.add(nameField);

        card.add(new ButtonWidget("draw.save", () ->
        {
            if (!board.hasDots())
            {
                ChatHelper.send(Lang.get("draw.empty"));
                return;
            }

            String saved = TemplateManager.saveDrawing(nameField.getText());
            nameField.setText(saved);
            ChatHelper.send(Lang.get("draw.saved", saved));
        }, true));

        return card;
    }

    private static Card buildGeneral()
    {
        Card card = new Card("card.general");

        card.add(new ChoiceWidget("gui.lang", new String[] {"EN", "中文"},
            () -> Lang.isChinese() ? 1 : 0,
            index ->
            {
                AimSettings.language = index == 1 ? Lang.ZH_CN : Lang.EN_US;
                Lang.setLanguage(AimSettings.language);
            }));

        card.add(new ToggleWidget("general.blur",
            () -> AimSettings.blurEnabled,
            value -> AimSettings.blurEnabled = value));

        card.add(new SliderWidget("general.blurStrength",
            () -> AimSettings.blurStrength,
            value -> AimSettings.blurStrength = value, 1, 6, ""));

        card.add(new ToggleWidget("general.animations",
            () -> AimSettings.animationsEnabled,
            value -> AimSettings.animationsEnabled = value));

        card.add(new LabelWidget(() -> Lang.get("general.keyHint", ClientProxy.keyOpenGui == null ? "M" : Keyboard.getKeyName(ClientProxy.keyOpenGui.getKeyCode())), GuiTheme.TEXT_DIM, 11));
        card.add(new LabelWidget(() -> Lang.get("general.about", Reference.VERSION, Reference.AUTHOR), GuiTheme.TEXT_DIM, 11));

        return card;
    }

    private static Card buildCrosshair()
    {
        Card card = new Card("card.crosshair");

        card.add(new ToggleWidget("crosshair.useCustom",
            () -> AimSettings.useCustomCrosshair,
            value -> AimSettings.useCustomCrosshair = value));

        card.add(new ToggleWidget("crosshair.hide",
            () -> AimSettings.hideCrosshair,
            value -> AimSettings.hideCrosshair = value));

        card.add(new SliderWidget("crosshair.size",
            () -> AimSettings.crosshairSize,
            value -> AimSettings.crosshairSize = value, 30, 400, "%"));

        card.add(new SliderWidget("crosshair.opacity",
            () -> AimSettings.crosshairOpacity,
            value -> AimSettings.crosshairOpacity = value, 10, 100, "%"));

        card.add(new SliderWidget("crosshair.range",
            () -> AimSettings.crosshairRange,
            value -> AimSettings.crosshairRange = value, 3, 32, "m"));

        card.add(new ChoiceWidget("crosshair.players", new String[] {"crosshair.playersHostile", "crosshair.playersFriendly", "crosshair.playersIgnore"},
            () -> AimSettings.getPlayerMode(),
            index -> AimSettings.setPlayerMode(index)));

        for (CrosshairType type : CrosshairType.values())
        {
            card.add(CrosshairRowWidget.create(type));
        }

        card.add(new LabelWidget("crosshair.patternHint"));
        return card;
    }

    private static Card buildDynamic()
    {
        Card card = new Card("card.dynamic");

        card.add(new ToggleWidget("dynamic.enabled",
            () -> AimSettings.dynamicEnabled,
            value -> AimSettings.dynamicEnabled = value));

        card.add(new ToggleWidget("dynamic.fov",
            () -> AimSettings.dynamicFov,
            value -> AimSettings.dynamicFov = value));

        card.add(new ToggleWidget("dynamic.attack",
            () -> AimSettings.dynamicAttack,
            value -> AimSettings.dynamicAttack = value));

        card.add(new SliderWidget("dynamic.amount",
            () -> AimSettings.dynamicAmount,
            value -> AimSettings.dynamicAmount = value, 100, 300, "%"));

        card.add(new SliderWidget("dynamic.speed",
            () -> AimSettings.dynamicSpeed,
            value -> AimSettings.dynamicSpeed = value, 5, 100, "%"));

        card.add(new ToggleWidget("dynamic.switch",
            () -> AimSettings.switchAnimation,
            value -> AimSettings.switchAnimation = value));

        card.add(new SliderWidget("dynamic.duration",
            () -> AimSettings.switchDuration,
            value -> AimSettings.switchDuration = value, 60, 800, "ms"));

        card.add(new ChoiceWidget("dynamic.direction", new String[] {"dynamic.smallToLarge", "dynamic.largeToSmall"},
            () -> AimSettings.switchSmallToLarge ? 0 : 1,
            index -> AimSettings.switchSmallToLarge = index == 0));

        card.add(new LabelWidget(() -> Lang.get("dynamic.status", CrosshairState.getType().getDisplayName(), String.format("%.2f", Float.valueOf(CrosshairState.getScale()))), GuiTheme.TEXT_DIM, 11));

        return card;
    }

    private static Card buildTemplate()
    {
        Card card = new Card("card.template");

        final TextFieldWidget nameField = new TextFieldWidget("template.name", AimSettings.templateName, 24,
            value -> AimSettings.templateName = value);
        card.add(nameField);

        card.add(new ListWidget("template.list",
            () -> TemplateManager.getNames(),
            (item, button) -> nameField.setText(item),
            () -> nameField.getText(), 4));

        card.add(new ButtonWidget("template.load", () ->
        {
            String name = nameField.getText();
            if (!TemplateManager.apply(name))
            {
                ChatHelper.send(Lang.get("template.notFound", name));
            }
            else
            {
                ChatHelper.send(Lang.get("template.loaded", name));
            }
        }));

        card.add(new ButtonWidget("template.save", () ->
        {
            String name = nameField.getText();
            if (name == null || name.trim().isEmpty())
            {
                ChatHelper.send(Lang.get("template.emptyName"));
            }
            else if (TemplateManager.isLocal(name))
            {
                ChatHelper.send(Lang.get("template.builtin"));
            }
            else if (TemplateManager.save(name))
            {
                nameField.setText(name);
                ChatHelper.send(Lang.get("template.saved", name));
            }
        }));

        card.add(new ButtonWidget("template.delete", () ->
        {
            String name = nameField.getText();
            if (TemplateManager.isLocal(name))
            {
                ChatHelper.send(Lang.get("template.deletedBuiltin"));
            }
            else if (TemplateManager.delete(name))
            {
                ChatHelper.send(Lang.get("template.deleted", name));
            }
            else
            {
                ChatHelper.send(Lang.get("template.notFound", name));
            }
        }));

        card.add(new ButtonWidget("template.export", () -> ShareManager.copyJsonToClipboard(nameField.getText())));
        card.add(new ButtonWidget("template.import", () -> ShareManager.importJsonFromClipboard()));
        card.add(new ButtonWidget("template.reset", () ->
        {
            TemplateManager.apply(TemplateManager.LOCAL);
            nameField.setText(TemplateManager.LOCAL);
        }));

        return card;
    }

    private static Card buildShare()
    {
        Card card = new Card("card.share");

        final TextFieldWidget recipientField = new TextFieldWidget("share.recipient", AimSettings.lastRecipient, 16,
            value -> AimSettings.lastRecipient = value);
        card.add(recipientField);

        card.add(new ButtonWidget("share.send", () ->
        {
            ShareManager.share(TemplateManager.getActiveName(), recipientField.getText());
        }, true));

        card.add(new ButtonWidget("share.sendAll", () ->
        {
            ShareManager.share(TemplateManager.getActiveName(), "");
        }));

        card.add(new LabelWidget("share.hint1"));
        card.add(new LabelWidget("share.hint2"));

        card.add(new LabelWidget(() ->
        {
            String state;
            if (ShareManager.isServerReady())
            {
                state = Lang.get("share.statusReady");
            }
            else if (ShareManager.isHelloPending())
            {
                state = Lang.get("share.statusPending");
            }
            else
            {
                state = Lang.get("share.statusUnsupported");
            }
            return Lang.get("share.status", state) + "   " + Lang.get("share.pending", Integer.toString(ShareManager.getPendingCount()));
        }, GuiTheme.TEXT_DIM, 11));

        card.add(new ListWidget("share.offers",
            () ->
            {
                List<String> items = new ArrayList<String>();
                for (PendingOffer offer : ShareManager.getPendingOffers())
                {
                    items.add(ShareManager.formatOffer(offer));
                }
                return items;
            },
            (item, button) ->
            {
                for (PendingOffer offer : ShareManager.getPendingOffers())
                {
                    if (ShareManager.formatOffer(offer).equals(item))
                    {
                        if (button == 1)
                        {
                            ShareManager.decline(offer.getId());
                        }
                        else
                        {
                            ShareManager.accept(offer.getId());
                        }
                        return;
                    }
                }
            },
            () -> null, 3));

        card.add(new ButtonWidget("share.accept", () -> ShareManager.acceptLatest()));
        card.add(new ButtonWidget("share.decline", () -> ShareManager.declineLatest()));

        return card;
    }
}
