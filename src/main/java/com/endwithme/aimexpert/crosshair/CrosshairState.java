package com.endwithme.aimexpert.crosshair;

import java.util.ArrayList;
import java.util.List;

import com.endwithme.aimexpert.config.AimSettings;
import com.google.common.base.Predicates;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Runtime crosshair state: which shape is active, the smooth dynamic spread and the switch animation.
 */
@SideOnly(Side.CLIENT)
public final class CrosshairState
{
    private static CrosshairType current = CrosshairType.NORMAL;
    private static long switchStart = System.nanoTime();
    private static long lastFrame = 0L;
    private static float dynamicScale = 1.0F;
    private static boolean attacking = false;
    private static boolean moving = false;

    private static boolean wasSwinging = false;
    private static float lastSwingProgress = 0.0F;
    private static long lastClickAt = 0L;
    private static float clickInterval = 0.32F;
    private static float breathPhase = 0.0F;
    private static float breathEnvelope = 0.0F;

    private CrosshairState()
    {
    }

    public static void update(Minecraft mc, float partialTicks)
    {
        long now = System.nanoTime();
        float delta = lastFrame == 0L ? 0.016F : Math.min(0.25F, (now - lastFrame) / 1.0E9F);
        lastFrame = now;

        attacking = isAttacking(mc);
        moving = isMoving(mc);
        updateBreathing(mc, System.currentTimeMillis(), delta);

        CrosshairType resolved = resolve(mc, partialTicks);
        if (resolved != current)
        {
            current = resolved;
            switchStart = now;
        }

        float target = 1.0F;
        if (AimSettings.dynamicEnabled)
        {
            float amount = (AimSettings.dynamicAmount / 100.0F) - 1.0F;
            if (attacking && AimSettings.dynamicAttack)
            {
                target += amount;
            }
            else if (moving && AimSettings.dynamicFov)
            {
                target += amount * 0.6F;
            }
        }

        float speed = 4.0F + (AimSettings.dynamicSpeed / 100.0F) * 14.0F;
        float factor = 1.0F - (float)Math.exp(-speed * delta);
        dynamicScale += (target - dynamicScale) * factor;
    }

    public static CrosshairType getType()
    {
        return current;
    }

    public static boolean isAttacking()
    {
        return attacking;
    }

    public static boolean isMoving()
    {
        return moving;
    }

    public static float getSwitchProgress()
    {
        if (!AimSettings.switchAnimation)
        {
            return 1.0F;
        }

        float duration = AimSettings.switchDuration / 1000.0F;
        float elapsed = (System.nanoTime() - switchStart) / 1.0E9F;
        float progress = duration <= 0.0F ? 1.0F : elapsed / duration;
        return progress >= 1.0F ? 1.0F : (progress <= 0.0F ? 0.0F : progress);
    }

    public static float getSwitchScale()
    {
        float progress = getSwitchProgress();
        if (progress >= 1.0F)
        {
            return 1.0F;
        }

        float eased = 1.0F - (1.0F - progress) * (1.0F - progress) * (1.0F - progress);
        float from = AimSettings.switchSmallToLarge ? 0.35F : 1.85F;
        return from + (1.0F - from) * eased;
    }

    public static float getScale()
    {
        float scale = getSwitchScale() * dynamicScale;
        if (current == CrosshairType.ATTACK && AimSettings.attackBreathing)
        {
            scale *= getBreathingScale();
        }
        return AimSettings.clamp(scale, 0.1F, 4.0F);
    }

    /**
     * The attack crosshair breathes with the click rate: every burst starts big, shrinks and grows
     * back. The breathing speed follows the interval between two clicks, so fast clicking gives a
     * quick pulse and slow clicking a calm one.
     */
    private static void updateBreathing(Minecraft mc, long now, float delta)
    {
        boolean swinging = mc.thePlayer != null && mc.thePlayer.isSwingInProgress;
        float progress = mc.thePlayer == null ? 0.0F : mc.thePlayer.swingProgress;
        boolean clicked = false;

        if (mc.thePlayer != null)
        {
            boolean restarted = swinging && (!wasSwinging || progress + 0.25F < lastSwingProgress);
            if (restarted && now - lastClickAt > 50L)
            {
                clicked = true;
            }
        }

        if (clicked)
        {
            if (lastClickAt != 0L)
            {
                float interval = (now - lastClickAt) / 1000.0F;
                clickInterval = AimSettings.clamp(clickInterval * 0.55F + interval * 0.45F, 0.08F, 1.2F);
            }

            lastClickAt = now;
            if (breathEnvelope < 0.15F)
            {
                breathPhase = 0.0F;
            }
        }

        wasSwinging = swinging;
        lastSwingProgress = progress;

        boolean active = AimSettings.attackBreathing && AimSettings.dynamicEnabled && AimSettings.dynamicAttack && attacking;
        float target = active ? 1.0F : 0.0F;
        float ease = 1.0F - (float)Math.exp(-9.0F * delta);
        breathEnvelope += (target - breathEnvelope) * ease;
        if (breathEnvelope < 0.002F)
        {
            breathEnvelope = 0.0F;
        }

        if (breathEnvelope > 0.0F)
        {
            // One breath per click interval, capped so very fast combo clicking stays readable.
            float hertz = AimSettings.clamp(1.0F / Math.max(0.08F, clickInterval), 0.6F, 3.0F);
            breathPhase += delta * hertz;
            breathPhase -= (float)Math.floor(breathPhase);
        }
    }

    private static float getBreathingScale()
    {
        float amount = AimSettings.attackBreathingAmount / 100.0F * 0.45F;
        return 1.0F + amount * (float)Math.cos(breathPhase * Math.PI * 2.0D) * breathEnvelope;
    }

    public static float getAlpha()
    {
        float opacity = AimSettings.crosshairOpacity / 100.0F;
        float fade = 0.6F + 0.4F * getSwitchProgress();
        return AimSettings.clamp(opacity * fade, 0.0F, 1.0F);
    }

    /**
     * Priority: attacking, then the targeted mob, then FOV movement, then the normal crosshair.
     * The attack crosshair always wins, the mob crosshairs win over the FOV crosshair.
     */
    private static CrosshairType resolve(Minecraft mc, float partialTicks)
    {
        List<CrosshairType> order = new ArrayList<CrosshairType>(4);

        if (AimSettings.dynamicEnabled && AimSettings.dynamicAttack && attacking)
        {
            order.add(CrosshairType.ATTACK);
        }

        CrosshairType targetType = classifyTarget(mc, partialTicks);
        if (targetType != null)
        {
            order.add(targetType);
        }

        if (AimSettings.dynamicEnabled && AimSettings.dynamicFov && moving)
        {
            order.add(CrosshairType.FOV);
        }

        order.add(CrosshairType.NORMAL);

        for (CrosshairType type : order)
        {
            if (AimSettings.isEnabled(type.name()))
            {
                return type;
            }
        }
        return order.get(0);
    }

    private static CrosshairType classifyTarget(Minecraft mc, float partialTicks)
    {
        EntityLivingBase target = getTarget(mc, partialTicks);
        if (target == null)
        {
            return null;
        }

        if (target instanceof EntityPlayer)
        {
            if (AimSettings.isPlayerIgnored())
            {
                return null;
            }
            return AimSettings.isPlayerFriendly() ? CrosshairType.FRIENDLY : CrosshairType.HOSTILE;
        }

        return target instanceof IMob ? CrosshairType.HOSTILE : CrosshairType.FRIENDLY;
    }

    /**
     * Aim based entity lookup with its own range. The vanilla pick stops at roughly three
     * blocks, which would keep the mob crosshairs from appearing while simply aiming at a mob.
     */
    private static EntityLivingBase getTarget(Minecraft mc, float partialTicks)
    {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.theWorld == null)
        {
            return null;
        }

        double range = AimSettings.crosshairRange;
        Vec3 eyes = player.getPositionEyes(partialTicks);
        Vec3 look = player.getLook(partialTicks);
        Vec3 end = eyes.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);

        MovingObjectPosition blockHit = mc.theWorld.rayTraceBlocks(eyes, end, false, true, false);
        double limit = blockHit == null ? range : eyes.distanceTo(blockHit.hitVec);

        AxisAlignedBB search = player.getEntityBoundingBox()
            .addCoord(look.xCoord * range, look.yCoord * range, look.zCoord * range)
            .expand(1.0D, 1.0D, 1.0D);
        List<Entity> candidates = mc.theWorld.getEntitiesInAABBexcluding(player, search, Predicates.alwaysTrue());

        EntityLivingBase best = null;
        double bestDistance = limit;

        for (Entity entity : candidates)
        {
            if (entity == player || !(entity instanceof EntityLivingBase) || !entity.canBeCollidedWith())
            {
                continue;
            }

            float border = entity.getCollisionBorderSize();
            AxisAlignedBB box = entity.getEntityBoundingBox().expand((double)border, (double)border, (double)border);
            MovingObjectPosition hit = box.calculateIntercept(eyes, end);

            double distance;
            if (box.isVecInside(eyes))
            {
                distance = 0.0D;
            }
            else if (hit != null)
            {
                distance = eyes.distanceTo(hit.hitVec);
            }
            else
            {
                continue;
            }

            if (distance <= bestDistance)
            {
                bestDistance = distance;
                best = (EntityLivingBase)entity;
            }
        }

        return best;
    }

    private static boolean isAttacking(Minecraft mc)
    {
        if (mc.thePlayer == null)
        {
            return false;
        }
        return mc.thePlayer.isSwingInProgress || mc.thePlayer.swingProgress > 0.0F || mc.gameSettings.keyBindAttack.isKeyDown();
    }

    private static boolean isMoving(Minecraft mc)
    {
        if (mc.thePlayer == null)
        {
            return false;
        }

        double dx = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double dz = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        return mc.thePlayer.isSprinting() || dx * dx + dz * dz > 0.0036D;
    }
}
