package com.example.stunslam;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;

public class StunSlamConfig {
    public boolean enabled = true;
    public int reactionMinMs = 90;
    public int reactionMaxMs = 220;
    public int swapDelayMinMs = 60;
    public int swapDelayMaxMs = 160;
    public double missChance = 0.04;
    public double minFallDistance = 1.6;
    public double maxReach = 3.0;
    public double cooldownThreshold = 0.92;
    public double cooldownJitter = 0.06;
    public boolean requireCrosshairOnTarget = true;
    public boolean restoreSlotAfter = true;
    public int restoreDelayMinMs = 120;
    public int restoreDelayMaxMs = 300;
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
        if (reactionMinMs > reactionMaxMs) { int t = reactionMinMs; reactionMinMs = reactionMaxMs; reactionMaxMs = t; }
        if (swapDelayMinMs > swapDelayMaxMs) { int t = swapDelayMinMs; swapDelayMinMs = swapDelayMaxMs; swapDelayMaxMs = t; }
        if (restoreDelayMinMs > restoreDelayMaxMs) { int t = restoreDelayMinMs; restoreDelayMinMs = restoreDelayMaxMs; restoreDelayMaxMs = t; }
        missChance = Math.max(0, Math.min(1, missChance));
        cooldownThreshold = Math.max(0, Math.min(1, cooldownThreshold));
        cooldownJitter = Math.max(0, cooldownJitter);
    }
}
