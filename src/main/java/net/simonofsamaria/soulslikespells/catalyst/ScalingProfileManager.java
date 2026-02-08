package net.simonofsamaria.soulslikespells.catalyst;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages data-driven ScalingProfiles loaded from datapacks.
 */
public final class ScalingProfileManager {
    private static final ScalingProfileManager INSTANCE = new ScalingProfileManager();
    private final Map<ResourceLocation, ScalingProfile> profiles = new HashMap<>();

    public static ScalingProfileManager getInstance() {
        return INSTANCE;
    }

    public void clear() {
        profiles.clear();
    }

    public void put(ResourceLocation id, ScalingProfile profile) {
        profiles.put(id, profile);
    }

    public ScalingProfile get(ResourceLocation id) {
        return profiles.get(id);
    }

    public Map<ResourceLocation, ScalingProfile> getAll() {
        return Collections.unmodifiableMap(profiles);
    }
}
