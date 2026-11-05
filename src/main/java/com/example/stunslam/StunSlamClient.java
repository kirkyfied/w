package com.example.stunslam;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class StunSlamClient implements ClientModInitializer {
    public static StunSlamConfig CONFIG;

    enum State { IDLE, WAIT_AXE_HIT, WAIT_SWAP, WAIT_SMASH, WAIT_RESTORE }

    private static final KeyBinding.Category CATEGORY =
        KeyBinding.Category.create(Identifier.of("stunslam", "main"));

    private final Random rng = new Random();
    private StunSlamConfig cfg;
    private KeyBinding toggle;
    private State state = State.IDLE;
    private long nextActionAt = 0;
    private long deadline = 0;
    private int originalSlot = -1;
    private LivingEntity target;
    private boolean prevTriggerDown = false;

    @Override
    public void onInitializeClient() {
        CONFIG = StunSlamConfig.load();
        cfg = CONFIG;
        toggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.stunslam.toggle", InputUtil.Type.KEYSYM, cfg.toggleKey, CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private long now() { return System.currentTimeMillis(); }

    private long gaussDelay(int min, int max) {
        if (max <= min) return min;
        double mean = (min + max) / 2.0, sd = Math.max(1, (max - min) / 4.0);
        double v = mean + rng.nextGaussian() * sd;
        return (long) Math.max(min, Math.min(max, v));
    }

    private void dbg(MinecraftClient mc, String msg) {
        if (cfg.debug && mc.player != null) mc.player.sendMessage(Text.literal("[StunSlam] " + msg), true);
    }

    private int findSlot(MinecraftClient mc, boolean axe) {
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            var s = inv.getStack(i);
            if (axe ? s.getItem() instanceof AxeItem : s.isOf(Items.MACE)) return i;
        }
        return -1;
    }

    private void selectSlot(MinecraftClient mc, int slot) {
        if (slot >= 0 && mc.player.getInventory().getSelectedSlot() != slot)
            mc.player.getInventory().setSelectedSlot(slot);
    }

    /** Distance from the player's eyes to the target's hitbox (what reach is actually measured against). */
    private double reach(MinecraftClient mc, Entity e) {
        Vec3d eye = mc.player.getEyePos();
        Box b = e.getBoundingBox();
        double dx = Math.max(Math.max(b.minX - eye.x, 0), eye.x - b.maxX);
        double dy = Math.max(Math.max(b.minY - eye.y, 0), eye.y - b.maxY);
        double dz = Math.max(Math.max(b.minZ - eye.z, 0), eye.z - b.maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private boolean valid(MinecraftClient mc, Entity e) {
        if (!(e instanceof LivingEntity le) || e == mc.player || !le.isAlive() || e.isSpectator()) return false;
        return !cfg.playersOnly || e instanceof PlayerEntity;
    }

    private LivingEntity findTarget(MinecraftClient mc) {
        if (mc.crosshairTarget instanceof EntityHitResult hit
                && valid(mc, hit.getEntity())
                && reach(mc, hit.getEntity()) <= cfg.maxReach) {
            return (LivingEntity) hit.getEntity();
        }
        if (cfg.targetMode == StunSlamConfig.TargetMode.CROSSHAIR_ONLY) return null;

        Vec3d eye = mc.player.getEyePos();
        Vec3d look = mc.player.getRotationVec(1.0f);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (!valid(mc, e)) continue;
            double d = reach(mc, e);
            if (d > cfg.maxReach) continue;
            if (cfg.targetMode == StunSlamConfig.TargetMode.CROSSHAIR_THEN_NEAREST) {
                Vec3d to = e.getBoundingBox().getCenter().subtract(eye).normalize();
                double angle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, look.dotProduct(to)))));
                if (angle > cfg.maxAngleDeg) continue;
            }
            if (d < bestDist) { bestDist = d; best = (LivingEntity) e; }
        }
        return best;
    }

    private boolean cooldownReady(MinecraftClient mc) {
        double need = cfg.cooldownThreshold + rng.nextDouble() * cfg.cooldownJitter;
        return mc.player.getAttackCooldownProgress(0f) >= Math.min(1.0, need);
    }

    private void attack(MinecraftClient mc, LivingEntity t) {
        mc.interactionManager.attackEntity(mc.player, t);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean triggerDown(MinecraftClient mc) {
        return cfg.triggerMode == StunSlamConfig.TriggerMode.LEFT_CLICK
            ? mc.options.attackKey.isPressed()
            : mc.options.useKey.isPressed();
    }

    private void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) { state = State.IDLE; return; }

        while (toggle.wasPressed()) {
            cfg.enabled = !cfg.enabled;
            cfg.save();
            mc.player.sendMessage(Text.literal("StunSlam: " + (cfg.enabled ? "ON" : "OFF")), true);
        }

        boolean down = triggerDown(mc);
        boolean edge = down && !prevTriggerDown;
        prevTriggerDown = down;

        if (!cfg.enabled) { state = State.IDLE; return; }
        if (mc.currentScreen != null) { if (state != State.IDLE && state != State.WAIT_RESTORE) state = State.IDLE; return; }

        switch (state) {
            case IDLE -> {
                if (!edge || now() < nextActionAt) return;
                if (rng.nextDouble() < cfg.missChance) return;
                LivingEntity t = findTarget(mc);
                if (t == null) { dbg(mc, "no target in range"); return; }
                if (cfg.requireBlocking && !t.isBlocking()) { dbg(mc, "target not blocking"); return; }
                if (findSlot(mc, true) < 0) { dbg(mc, "no axe in hotbar"); return; }
                if (findSlot(mc, false) < 0) { dbg(mc, "no mace in hotbar"); return; }
                target = t;
                originalSlot = mc.player.getInventory().getSelectedSlot();
                nextActionAt = now() + gaussDelay(cfg.reactionMinMs, cfg.reactionMaxMs);
                deadline = now() + cfg.sequenceTimeoutMs;
                state = State.WAIT_AXE_HIT;
            }
            case WAIT_AXE_HIT -> {
                if (now() < nextActionAt) return;
                if (now() > deadline || target == null || !target.isAlive()) { dbg(mc, "axe stage aborted"); abort(mc); return; }
                if (reach(mc, target) > cfg.maxReach) { dbg(mc, "target out of reach"); abort(mc); return; }
                int axe = findSlot(mc, true);
                if (axe < 0) { abort(mc); return; }
                selectSlot(mc, axe);
                if (cfg.axeRequireCooldown && !cooldownReady(mc)) return;
                attack(mc, target);
                nextActionAt = now() + gaussDelay(cfg.swapDelayMinMs, cfg.swapDelayMaxMs);
                state = State.WAIT_SWAP;
            }
            case WAIT_SWAP -> {
                if (now() < nextActionAt) return;
                int mace = findSlot(mc, false);
                if (mace < 0) { abort(mc); return; }
                selectSlot(mc, mace);
                nextActionAt = now() + gaussDelay(cfg.attackDelayMinMs, cfg.attackDelayMaxMs);
                deadline = now() + cfg.smashWindowMs;
                state = State.WAIT_SMASH;
            }
            case WAIT_SMASH -> {
                if (now() < nextActionAt) return;
                if (now() > deadline || target == null || !target.isAlive()) { dbg(mc, "smash window expired"); finish(mc); return; }
                if (cfg.requireFall && mc.player.fallDistance < cfg.minFallDistance) return;
                if (reach(mc, target) > cfg.maxReach) return;
                if (cfg.smashRequireCooldown && !cooldownReady(mc)) return;
                attack(mc, target);
                finish(mc);
            }
            case WAIT_RESTORE -> {
                if (now() < nextActionAt) return;
                if (originalSlot >= 0) selectSlot(mc, originalSlot);
                reset();
            }
        }
    }

    private void abort(MinecraftClient mc) {
        if (cfg.restoreSlotAfter && originalSlot >= 0) selectSlot(mc, originalSlot);
        reset();
    }

    private void finish(MinecraftClient mc) {
        if (cfg.restoreSlotAfter) {
            nextActionAt = now() + gaussDelay(cfg.restoreDelayMinMs, cfg.restoreDelayMaxMs);
            state = State.WAIT_RESTORE;
        } else reset();
    }

    private void reset() { state = State.IDLE; target = null; nextActionAt = now() + 400; }
}
