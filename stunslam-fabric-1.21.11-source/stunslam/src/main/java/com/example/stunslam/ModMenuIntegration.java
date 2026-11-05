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

            ConfigCategory general = b.getOrCreateCategory(Text.literal("General"));
            general.addEntry(e.startBooleanToggle(Text.literal("Enabled"), c.enabled)
                .setDefaultValue(d.enabled).setSaveConsumer(v -> c.enabled = v).build());
            general.addEntry(e.startBooleanToggle(Text.literal("Require crosshair on target"), c.requireCrosshairOnTarget)
                .setDefaultValue(d.requireCrosshairOnTarget).setSaveConsumer(v -> c.requireCrosshairOnTarget = v).build());
            general.addEntry(e.startBooleanToggle(Text.literal("Restore original slot"), c.restoreSlotAfter)
                .setDefaultValue(d.restoreSlotAfter).setSaveConsumer(v -> c.restoreSlotAfter = v).build());
            general.addEntry(e.startDoubleField(Text.literal("Max reach"), c.maxReach)
                .setDefaultValue(d.maxReach).setSaveConsumer(v -> c.maxReach = v).build());
            general.addEntry(e.startDoubleField(Text.literal("Min fall distance"), c.minFallDistance)
                .setDefaultValue(d.minFallDistance).setSaveConsumer(v -> c.minFallDistance = v).build());

            ConfigCategory timing = b.getOrCreateCategory(Text.literal("Timing"));
            timing.addEntry(e.startIntField(Text.literal("Reaction min (ms)"), c.reactionMinMs)
                .setDefaultValue(d.reactionMinMs).setSaveConsumer(v -> c.reactionMinMs = v).build());
            timing.addEntry(e.startIntField(Text.literal("Reaction max (ms)"), c.reactionMaxMs)
                .setDefaultValue(d.reactionMaxMs).setSaveConsumer(v -> c.reactionMaxMs = v).build());
            timing.addEntry(e.startIntField(Text.literal("Swap delay min (ms)"), c.swapDelayMinMs)
                .setDefaultValue(d.swapDelayMinMs).setSaveConsumer(v -> c.swapDelayMinMs = v).build());
            timing.addEntry(e.startIntField(Text.literal("Swap delay max (ms)"), c.swapDelayMaxMs)
                .setDefaultValue(d.swapDelayMaxMs).setSaveConsumer(v -> c.swapDelayMaxMs = v).build());
            timing.addEntry(e.startIntField(Text.literal("Restore delay min (ms)"), c.restoreDelayMinMs)
                .setDefaultValue(d.restoreDelayMinMs).setSaveConsumer(v -> c.restoreDelayMinMs = v).build());
            timing.addEntry(e.startIntField(Text.literal("Restore delay max (ms)"), c.restoreDelayMaxMs)
                .setDefaultValue(d.restoreDelayMaxMs).setSaveConsumer(v -> c.restoreDelayMaxMs = v).build());

            ConfigCategory human = b.getOrCreateCategory(Text.literal("Humanization"));
            human.addEntry(e.startDoubleField(Text.literal("Miss chance (0-1)"), c.missChance)
                .setDefaultValue(d.missChance).setSaveConsumer(v -> c.missChance = v).build());
            human.addEntry(e.startDoubleField(Text.literal("Cooldown threshold (0-1)"), c.cooldownThreshold)
                .setDefaultValue(d.cooldownThreshold).setSaveConsumer(v -> c.cooldownThreshold = v).build());
            human.addEntry(e.startDoubleField(Text.literal("Cooldown jitter"), c.cooldownJitter)
                .setDefaultValue(d.cooldownJitter).setSaveConsumer(v -> c.cooldownJitter = v).build());

            return b.build();
        };
    }
}
