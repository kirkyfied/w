package com.example.stunslam;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;

public class StunSlamConfig {
    public enum TriggerMode { RIGHT_CLICK, LEFT_CLICK }
    public enum TargetMode { CROSSHAIR_ONLY, CROSSHAIR_THEN_NEAREST, NEAREST }

    // General
    public boolean enabled = true;
    public boolean debug = true;                 // action-bar messages explaining why nothing fired
    public TriggerMode triggerMode = TriggerMode.RIGHT_CLICK;
    public TargetMode targetMode = TargetMode.CROSSHAIR_THEN_NEAREST;
    public double maxReach = 3.0;                // distance to hitbox
    public double maxAngleDeg = 25.0;            // for NEAREST fallback: max angle from view direction
    public boolean playersOnly = true;           // set false if your bots are not player entities
    public boolean requireBlocking = false;      // only run if target is visibly blocking
    public boolean requireFall = false;          // mace hit only once falling (real smash needs > 1.5)
    public double minFallDistance = 1.6;
    public boolean axeRequireCooldown = true;
    public boolean smashRequireCooldown = false;
    public boolean restoreSlotAfter = true;

    // Timing (ms)
    public int reactionMinMs = 40;               // trigger -> axe hit
    public int reactionMaxMs = 140;
    public int swapDelayMinMs = 60;              // axe hit -> swap to mace
    public int swapDelayMaxMs = 160;
    public int attackDelayMinMs = 40;            // swap -> mace hit
    public int attackDelayMaxMs = 110;
    public int restoreDelayMinMs = 120;
    public int restoreDelayMaxMs = 300;
    public int smashWindowMs = 3000;             // how long to wait for fall/cooldown conditions
    public int sequenceTimeoutMs = 4000;         // axe stage timeout

    // Humanization
    public double missChance = 0.0;              // chance to ignore a trigger
    public double cooldownThreshold = 0.92;
    public double cooldownJitter = 0.06;

    public int toggleKey = org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_BRACKET;

    private static final Path PATH =
        FabricLoader.getInstance().getConfigDir().resolve("stunslam.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static StunSlamConfig load() {
        try {
            if (Files.exists(PATH)) {
                StunSlamConfig c = GSON.fromJson(Files.readString(PATH), StunSlamConfig.class);
                if (c != null) { c.sanitize(); return c; }
            }
        } catch (Exception ignored) {}
        StunSlamConfig c = new StunSlamConfig();
        c.save();
        return c;
    }

    public void save() {
        try { Files.writeString(PATH, GSON.toJson(this)); } catch (Exception ignored) {}
    }

    public void sanitize() {
        if (triggerMode == null) triggerMode = TriggerMode.RIGHT_CLICK;
        if (targetMode == null) targetMode = TargetMode.CROSSHAIR_THEN_NEAREST;
        if (reactionMinMs > reactionMaxMs) { int t = reactionMinMs; reactionMinMs = reactionMaxMs; reactionMaxMs = t; }
        if (swapDelayMinMs > swapDelayMaxMs) { int t = swapDelayMinMs; swapDelayMinMs = swapDelayMaxMs; swapDelayMaxMs = t; }
        if (attackDelayMinMs > attackDelayMaxMs) { int t = attackDelayMinMs; attackDelayMinMs = attackDelayMaxMs; attackDelayMaxMs = t; }
        if (restoreDelayMinMs > restoreDelayMaxMs) { int t = restoreDelayMinMs; restoreDelayMinMs = restoreDelayMaxMs; restoreDelayMaxMs = t; }
        missChance = Math.max(0, Math.min(1, missChance));
        cooldownThreshold = Math.max(0, Math.min(1, cooldownThreshold));
        cooldownJitter = Math.max(0, cooldownJitter);
        maxAngleDeg = Math.max(1, Math.min(180, maxAngleDeg));
        maxReach = Math.max(1, Math.min(6, maxReach));
    }
}
