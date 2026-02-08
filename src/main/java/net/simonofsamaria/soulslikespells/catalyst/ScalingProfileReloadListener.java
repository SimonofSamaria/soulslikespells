package net.simonofsamaria.soulslikespells.catalyst;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;

import java.util.Map;

/**
 * Loads ScalingProfiles from data/{namespace}/soulslikespells/scaling_profiles/*.json
 */
public class ScalingProfileReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();

    public ScalingProfileReloadListener() {
        super(GSON, "soulslikespells/scaling_profiles");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        ScalingProfileManager manager = ScalingProfileManager.getInstance();
        manager.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                ScalingProfile profile = ScalingProfile.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                        .getOrThrow();
                manager.put(entry.getKey(), profile);
            } catch (Exception e) {
                SoulslikeSpells.LOGGER.error("Failed to load scaling profile {}: {}", entry.getKey(), e.getMessage());
            }
        }

        SoulslikeSpells.LOGGER.info("Loaded {} scaling profiles", manager.getAll().size());
    }
}
