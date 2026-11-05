package com.example.stunslam;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            StunSlamConfig c = StunSlamClient.CONFIG;
            StunSlamConfig d = new StunSlamConfig();

            ConfigBuilder b = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("StunSlam"))
                .setSavingRunnable(() -> { c.sanitize(); c.save(); });
            ConfigEntryBuilder e = b.entryBuilder();

            ConfigCategory g = b.getOrCreateCategory(Text.literal("General"));
            g.addEntry(e.startBooleanToggle(Text.literal("Enabled"), c.enabled)
                .setDefaultValue(d.enabled).setSaveConsumer(v -> c.enabled = v).build());
            g.addEntry(e.startBooleanToggle(Text.literal("Debug messages (action bar)"), c.debug)
                .setDefaultValue(d.debug).setSaveConsumer(v -> c.debug = v).build());
            g.addEntry(e.startEnumSelector(Text.literal("Trigger"), StunSlamConfig.TriggerMode.class, c.triggerMode)
                .setDefaultValue(d.triggerMode).setSaveConsumer(v -> c.triggerMode = v).build());
            g.addEntry(e.startEnumSelector(Text.literal("Target mode"), StunSlamConfig.TargetMode.class, c.targetMode)
                .setDefaultValue(d.targetMode).setSaveConsumer(v -> c.targetMode = v).build());
            g.addEntry(e.startDoubleField(Text.literal("Max reach"), c.maxReach)
                .setDefaultValue(d.maxReach).setSaveConsumer(v -> c.maxReach = v).build());
            g.addEntry(e.startDoubleField(Text.literal("Max angle for nearest fallback (deg)"), c.maxAngleDeg)
                .setDefaultValue(d.maxAngleDeg).setSaveConsumer(v -> c.maxAngleDeg = v).build());
            g.addEntry(e.startBooleanToggle(Text.literal("Players only (off if bots aren't players)"), c.playersOnly)
                .setDefaultValue(d.playersOnly).setSaveConsumer(v -> c.playersOnly = v).build());
            g.addEntry(e.startBooleanToggle(Text.literal("Require target blocking"), c.requireBlocking)
                .setDefaultValue(d.requireBlocking).setSaveConsumer(v -> c.requireBlocking = v).build());
            g.addEntry(e.startBooleanToggle(Text.literal("Require falling for mace hit"), c.requireFall)
                .setDefaultValue(d.requireFall).setSaveConsumer(v -> c.requireFall = v).build());
            g.addEntry(e.startDoubleField(Text.literal("Min fall distance"), c.minFallDistance)
                .setDefaultValue(d.minFallDistance).setSaveConsumer(v -> c.minFallDistance = v).build());
            g.addEntry(e.startBooleanToggle(Text.literal("Axe hit waits for cooldown"), c.axeRequireCooldown)
                .setDefaultValue(d.axeRequireCooldown).setSaveConsumer(v -> c.axeRequireCooldown = v).build());
            g.addEntry(e.startBooleanToggle(Text.literal("Mace hit waits for cooldown"), c.smashRequireCooldown)
                .setDefaultValue(d.smashRequireCooldown).setSaveConsumer(v -> c.smashRequireCooldown = v).build());
            g.addEntry(e.startBooleanToggle(Text.literal("Restore original slot"), c.restoreSlotAfter)
                .setDefaultValue(d.restoreSlotAfter).setSaveConsumer(v -> c.restoreSlotAfter = v).build());

            ConfigCategory t = b.getOrCreateCategory(Text.literal("Timing"));
            t.addEntry(e.startIntField(Text.literal("Reaction min (ms)"), c.reactionMinMs)
                .setDefaultValue(d.reactionMinMs).setSaveConsumer(v -> c.reactionMinMs = v).build());
            t.addEntry(e.startIntField(Text.literal("Reaction max (ms)"), c.reactionMaxMs)
                .setDefaultValue(d.reactionMaxMs).setSaveConsumer(v -> c.reactionMaxMs = v).build());
            t.addEntry(e.startIntField(Text.literal("Axe -> swap min (ms)"), c.swapDelayMinMs)
                .setDefaultValue(d.swapDelayMinMs).setSaveConsumer(v -> c.swapDelayMinMs = v).build());
            t.addEntry(e.startIntField(Text.literal("Axe -> swap max (ms)"), c.swapDelayMaxMs)
                .setDefaultValue(d.swapDelayMaxMs).setSaveConsumer(v -> c.swapDelayMaxMs = v).build());
            t.addEntry(e.startIntField(Text.literal("Swap -> mace hit min (ms)"), c.attackDelayMinMs)
                .setDefaultValue(d.attackDelayMinMs).setSaveConsumer(v -> c.attackDelayMinMs = v).build());
            t.addEntry(e.startIntField(Text.literal("Swap -> mace hit max (ms)"), c.attackDelayMaxMs)
                .setDefaultValue(d.attackDelayMaxMs).setSaveConsumer(v -> c.attackDelayMaxMs = v).build());
            t.addEntry(e.startIntField(Text.literal("Restore delay min (ms)"), c.restoreDelayMinMs)
                .setDefaultValue(d.restoreDelayMinMs).setSaveConsumer(v -> c.restoreDelayMinMs = v).build());
            t.addEntry(e.startIntField(Text.literal("Restore delay max (ms)"), c.restoreDelayMaxMs)
                .setDefaultValue(d.restoreDelayMaxMs).setSaveConsumer(v -> c.restoreDelayMaxMs = v).build());
            t.addEntry(e.startIntField(Text.literal("Smash window (ms)"), c.smashWindowMs)
                .setDefaultValue(d.smashWindowMs).setSaveConsumer(v -> c.smashWindowMs = v).build());
            t.addEntry(e.startIntField(Text.literal("Axe stage timeout (ms)"), c.sequenceTimeoutMs)
                .setDefaultValue(d.sequenceTimeoutMs).setSaveConsumer(v -> c.sequenceTimeoutMs = v).build());

            ConfigCategory h = b.getOrCreateCategory(Text.literal("Humanization"));
            h.addEntry(e.startDoubleField(Text.literal("Ignore-trigger chance (0-1)"), c.missChance)
                .setDefaultValue(d.missChance).setSaveConsumer(v -> c.missChance = v).build());
            h.addEntry(e.startDoubleField(Text.literal("Cooldown threshold (0-1)"), c.cooldownThreshold)
                .setDefaultValue(d.cooldownThreshold).setSaveConsumer(v -> c.cooldownThreshold = v).build());
            h.addEntry(e.startDoubleField(Text.literal("Cooldown jitter"), c.cooldownJitter)
                .setDefaultValue(d.cooldownJitter).setSaveConsumer(v -> c.cooldownJitter = v).build());

            return b.build();
        };
    }
}
