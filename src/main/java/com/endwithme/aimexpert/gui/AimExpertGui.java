package com.endwithme.aimexpert.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import com.endwithme.aimexpert.Reference;
import com.endwithme.aimexpert.config.AimSettings;
import com.endwithme.aimexpert.gui.widget.TextFieldWidget;
import com.endwithme.aimexpert.gui.widget.Widget;
import com.endwithme.aimexpert.i18n.Lang;
import com.endwithme.aimexpert.proxy.ClientProxy;
import com.endwithme.aimexpert.render.BlurRenderer;
import com.endwithme.aimexpert.render.RenderUtils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AimExpertGui extends GuiScreen
{
    private static final int HEADER_HEIGHT = 28;
    private static final int CARD_GAP = 6;
    private static final int CLOSE_BUTTON_SIZE = 11;
    private static final long OPEN_DURATION = 260L;
    private static final long CLOSE_DURATION = 220L;
    private static final long OPEN_STAGGER = 65L;
    private static final long CLOSE_STAGGER = 45L;

    private final List<Card> cards = new ArrayList<Card>();
    private double scroll;
    private double targetScroll;
    private long openTime;
    private long closeTime;
    private boolean closing;
    private float closeProgress;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int contentTop;
    private int contentViewHeight;
    private int contentHeight;
    private int maxScroll;
    private Widget focusedWidget;
    private Widget hoveredWidget;

    @Override
    public void initGui()
    {
        if (this.cards.isEmpty())
        {
            this.cards.addAll(CardBuilder.build());
            this.openTime = System.currentTimeMillis();
        }

        this.panelWidth = Math.max(160, Math.min(268, this.width - 20));
        this.panelHeight = Math.max(120, Math.min(250, this.height - 16));
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;
        this.contentTop = this.panelY + HEADER_HEIGHT;
        this.contentViewHeight = Math.max(20, this.panelHeight - HEADER_HEIGHT - 8);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        long now = System.currentTimeMillis();

        if (this.closing)
        {
            this.closeProgress = Math.min(1.0F, (now - this.closeTime) / (float)closeDuration());
            if (this.closeProgress >= 1.0F)
            {
                this.mc.displayGuiScreen((GuiScreen)null);
                return;
            }
        }

        BlurRenderer.composite();
        RenderUtils.rect(0.0F, 0.0F, this.width, this.height, GuiTheme.OVERLAY);
        RenderUtils.roundedRect(this.panelX, this.panelY, this.panelWidth, this.panelHeight, 6.0F, GuiTheme.PANEL_TOP, GuiTheme.PANEL_BOTTOM);
        RenderUtils.roundedOutline(this.panelX, this.panelY, this.panelWidth, this.panelHeight, 6.0F, 1.0F, GuiTheme.PANEL_BORDER);

        this.layoutCards(now);

        RenderUtils.startScissor(this.panelX + 1, this.contentTop, this.panelWidth - 2, this.contentViewHeight);
        this.hoveredWidget = null;

        for (Card card : this.cards)
        {
            if (card.getY() + card.getHeight() < this.contentTop || card.getY() > this.contentTop + this.contentViewHeight)
            {
                continue;
            }

            card.draw(mouseX, mouseY);
            if (this.hoveredWidget == null)
            {
                this.hoveredWidget = card.findHovered(mouseX, mouseY);
            }
        }

        RenderUtils.endScissor();

        this.drawHeader(mouseX, mouseY);
        this.drawScrollbar();
        this.drawFooter();
    }

    private void layoutCards(long now)
    {
        int cardWidth = this.panelWidth - 12;
        int total = 0;

        for (Card card : this.cards)
        {
            card.measure(cardWidth);
            total += card.getHeight() + CARD_GAP;
        }

        this.contentHeight = Math.max(0, total - CARD_GAP);
        this.maxScroll = Math.max(0, this.contentHeight - this.contentViewHeight);
        this.targetScroll = Math.max(0.0D, Math.min(this.targetScroll, this.maxScroll));

        if (AimSettings.animationsEnabled)
        {
            this.scroll += (this.targetScroll - this.scroll) * 0.35D;
            if (Math.abs(this.targetScroll - this.scroll) < 0.4D)
            {
                this.scroll = this.targetScroll;
            }
        }
        else
        {
            this.scroll = this.targetScroll;
        }

        int baseY = this.contentTop - (int)Math.round(this.scroll);

        for (int index = 0; index < this.cards.size(); index++)
        {
            Card card = this.cards.get(index);
            float eased = this.easingFor(index, now);
            int upDistance = Math.max(0, baseY - this.contentTop) + card.getHeight() + 14;
            int offset = this.closing ? -(int)(eased * upDistance) : -(int)((1.0F - eased) * upDistance);
            card.layout(this.panelX + 6, baseY + offset);
            baseY += card.getHeight() + CARD_GAP;
        }
    }

    private float easingFor(int index, long now)
    {
        if (this.closing)
        {
            if (!AimSettings.animationsEnabled)
            {
                return 1.0F;
            }

            long start = this.closeTime + (this.cards.size() - 1 - index) * CLOSE_STAGGER;
            float progress = Math.max(0.0F, Math.min(1.0F, (now - start) / (float)closeDuration()));
            return progress * progress;
        }

        if (!AimSettings.animationsEnabled)
        {
            return 1.0F;
        }

        long start = this.openTime + index * OPEN_STAGGER;
        float progress = Math.max(0.0F, Math.min(1.0F, (now - start) / (float)OPEN_DURATION));
        float inverted = 1.0F - progress;
        return 1.0F - inverted * inverted * inverted;
    }

    private void drawHeader(int mouseX, int mouseY)
    {
        FontRenderer font = this.fontRendererObj;
        font.drawString(Reference.NAME, this.panelX + 12, this.panelY + 8, GuiTheme.TITLE, true);
        font.drawString("v" + Reference.VERSION, this.panelX + 16 + font.getStringWidth(Reference.NAME), this.panelY + 8, GuiTheme.TEXT_DIM, false);

        int buttonX = this.closeButtonX();
        int buttonY = this.closeButtonY();
        boolean hovered = this.isInside(mouseX, mouseY, buttonX - 2, buttonY - 2, CLOSE_BUTTON_SIZE + 4, CLOSE_BUTTON_SIZE + 4);
        RenderUtils.roundedRect(buttonX, buttonY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE, 3.0F, hovered ? 0x80FFFFFF : 0x33FFFFFF);
        font.drawString("x", buttonX + 3, buttonY + 2, hovered ? 0xFF151515 : GuiTheme.TEXT, false);

        RenderUtils.rect(this.panelX, this.panelY + HEADER_HEIGHT - 1, this.panelWidth, 1, 0x1AFFFFFF);
    }

    private void drawScrollbar()
    {
        if (this.maxScroll <= 0)
        {
            return;
        }

        int trackX = this.panelX + this.panelWidth - 5;
        int trackY = this.contentTop + 2;
        int trackHeight = this.contentViewHeight - 4;
        RenderUtils.roundedRect(trackX, trackY, 2.0F, trackHeight, 1.0F, GuiTheme.SCROLL_TRACK);

        float visible = this.contentViewHeight / (float)Math.max(1, this.contentHeight);
        int barHeight = Math.max(12, (int)(trackHeight * visible));
        int barY = trackY + (int)((trackHeight - barHeight) * (this.scroll / Math.max(1.0D, this.maxScroll)));
        RenderUtils.roundedRect(trackX, barY, 2.0F, barHeight, 1.0F, GuiTheme.SCROLL_BAR);
    }

    private void drawFooter()
    {
        FontRenderer font = this.fontRendererObj;
        String hint = Lang.get("gui.hint.close");
        font.drawString(hint, (this.width - font.getStringWidth(hint)) / 2, this.panelY + this.panelHeight + 5, GuiTheme.TEXT_DIM, false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException
    {
        if (this.closing)
        {
            return;
        }

        if (this.isInside(mouseX, mouseY, this.closeButtonX() - 2, this.closeButtonY() - 2, CLOSE_BUTTON_SIZE + 4, CLOSE_BUTTON_SIZE + 4))
        {
            this.startClosing();
            return;
        }

        if (mouseX < this.panelX || mouseX > this.panelX + this.panelWidth || mouseY < this.panelY || mouseY > this.panelY + this.panelHeight)
        {
            this.startClosing();
            return;
        }

        if (mouseY < this.contentTop || mouseY > this.contentTop + this.contentViewHeight)
        {
            return;
        }

        for (Card card : this.cards)
        {
            if (card.mouseClicked(mouseX, mouseY, button))
            {
                this.setFocus(card.findHovered(mouseX, mouseY));
                return;
            }
        }

        this.setFocus(null);
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        for (Card card : this.cards)
        {
            card.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        for (Card card : this.cards)
        {
            card.mouseDragged(mouseX, mouseY, clickedMouseButton);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        int wheel = Mouse.getEventDWheel();
        if (wheel == 0 || this.closing)
        {
            return;
        }

        int amount = wheel / 120;

        if (this.hoveredWidget != null)
        {
            this.hoveredWidget.mouseScrolled(amount);
            this.hoveredWidget = null;
            return;
        }

        this.targetScroll = Math.max(0.0D, Math.min(this.targetScroll - amount * 22.0D, this.maxScroll));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1 || ClientProxy.keyOpenGui != null && keyCode == ClientProxy.keyOpenGui.getKeyCode())
        {
            this.startClosing();
            return;
        }

        if (this.focusedWidget != null && this.focusedWidget.keyTyped(typedChar, keyCode))
        {
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        for (Card card : this.cards)
        {
            card.update();
        }
    }

    @Override
    public void onGuiClosed()
    {
        for (Card card : this.cards)
        {
            card.onClosed();
        }

        AimSettings.save();
        BlurRenderer.reset();
        this.setFocus(null);
    }

    private void startClosing()
    {
        if (this.closing)
        {
            return;
        }

        this.closing = true;
        this.closeTime = System.currentTimeMillis();
        this.setFocus(null);
    }

    private long closeDuration()
    {
        return AimSettings.animationsEnabled ? CLOSE_DURATION : 1L;
    }

    private void setFocus(Widget widget)
    {
        Widget target = widget instanceof TextFieldWidget ? widget : null;
        if (target == this.focusedWidget)
        {
            return;
        }

        if (this.focusedWidget instanceof TextFieldWidget)
        {
            ((TextFieldWidget)this.focusedWidget).setFocused(false);
        }

        this.focusedWidget = target;
    }

    private int closeButtonX()
    {
        return this.panelX + this.panelWidth - CLOSE_BUTTON_SIZE - 8;
    }

    private int closeButtonY()
    {
        return this.panelY + 8;
    }

    private boolean isInside(int mouseX, int mouseY, int boxX, int boxY, int boxWidth, int boxHeight)
    {
        return mouseX >= boxX && mouseX <= boxX + boxWidth && mouseY >= boxY && mouseY <= boxY + boxHeight;
    }
}
