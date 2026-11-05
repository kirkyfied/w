package com.example.stunslam;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

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
    private int originalSlot = -1;
    private PlayerEntity target;

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
        double mean = (min + max) / 2.0, sd = Math.max(1, (max - min) / 4.0);
        double v = mean + rng.nextGaussian() * sd;
        return (long) Math.max(min, Math.min(max, v));
    }

    private int findSlot(MinecraftClient mc, boolean axe) {
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            var s = inv.getStack(i);
            if (axe ? s.getItem() instanceof AxeItem : s.isOf(Items.MACE)) return i;
        }
        return -1;
    }

    private PlayerEntity crosshairPlayer(MinecraftClient mc) {
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return null;
        Entity e = ((EntityHitResult) mc.crosshairTarget).getEntity();
        return e instanceof PlayerEntity p && mc.player.distanceTo(p) <= cfg.maxReach ? p : null;
    }

    private boolean cooldownReady(MinecraftClient mc) {
        double need = cfg.cooldownThreshold + rng.nextDouble() * cfg.cooldownJitter;
        return mc.player.getAttackCooldownProgress(0f) >= Math.min(1.0, need);
    }

    private void attack(MinecraftClient mc, PlayerEntity t) {
        mc.interactionManager.attackEntity(mc.player, t);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        while (toggle.wasPressed()) {
            cfg.enabled = !cfg.enabled;
            cfg.save();
            mc.player.sendMessage(Text.literal("StunSlam: " + (cfg.enabled ? "ON" : "OFF")), true);
        }
        if (!cfg.enabled || mc.currentScreen != null) { state = State.IDLE; return; }

        switch (state) {
            case IDLE -> {
                PlayerEntity p = crosshairPlayer(mc);
                if (p == null || !p.isBlocking()) return;
                if (now() < nextActionAt) return;
                if (rng.nextDouble() < cfg.missChance) { nextActionAt = now() + 400; return; }
                if (findSlot(mc, true) < 0 || findSlot(mc, false) < 0) return;
                target = p;
                originalSlot = mc.player.getInventory().getSelectedSlot();
                nextActionAt = now() + gaussDelay(cfg.reactionMinMs, cfg.reactionMaxMs);
                state = State.WAIT_AXE_HIT;
            }
            case WAIT_AXE_HIT -> {
                if (now() < nextActionAt) return;
                if (target == null || !target.isAlive() || mc.player.distanceTo(target) > cfg.maxReach) { reset(); return; }
                int axe = findSlot(mc, true);
                if (axe < 0) { reset(); return; }
                if (mc.player.getInventory().getSelectedSlot() != axe) mc.player.getInventory().setSelectedSlot(axe);
                if (!cooldownReady(mc)) return;
                attack(mc, target);
                nextActionAt = now() + gaussDelay(cfg.swapDelayMinMs, cfg.swapDelayMaxMs);
                state = State.WAIT_SWAP;
            }
            case WAIT_SWAP -> {
                if (now() < nextActionAt) return;
                int mace = findSlot(mc, false);
                if (mace < 0) { reset(); return; }
                mc.player.getInventory().setSelectedSlot(mace);
                state = State.WAIT_SMASH;
                nextActionAt = now() + 3000;
            }
            case WAIT_SMASH -> {
                if (now() > nextActionAt || target == null || !target.isAlive()) { finish(mc); return; }
                if (mc.player.fallDistance < cfg.minFallDistance) return;
                if (mc.player.distanceTo(target) > cfg.maxReach) return;
                if (cfg.requireCrosshairOnTarget && crosshairPlayer(mc) != target) return;
                if (!cooldownReady(mc)) return;
                attack(mc, target);
                finish(mc);
            }
            case WAIT_RESTORE -> {
                if (now() < nextActionAt) return;
                if (originalSlot >= 0) mc.player.getInventory().setSelectedSlot(originalSlot);
                reset();
            }
        }
    }

    private void finish(MinecraftClient mc) {
        if (cfg.restoreSlotAfter) {
            nextActionAt = now() + gaussDelay(cfg.restoreDelayMinMs, cfg.restoreDelayMaxMs);
            state = State.WAIT_RESTORE;
        } else reset();
    }

    private void reset() { state = State.IDLE; target = null; nextActionAt = now() + 500; }
}
