package net.simonofsamaria.soulslikespells.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class PlayerSoulData {
    public static final Codec<PlayerSoulData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("soul_level").forGetter(PlayerSoulData::getSoulLevel),
                    Codec.INT.optionalFieldOf("experience", 0).forGetter(d -> 0), // backward compat: read but ignore
                    Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
                            .fieldOf("allocated_points")
                            .forGetter(PlayerSoulData::getAllocatedPoints)
            ).apply(instance, (soulLevel, ign, allocatedPoints) -> new PlayerSoulData(soulLevel, allocatedPoints))
    );

    private int soulLevel;
    private final Map<ResourceLocation, Integer> allocatedPoints;

    public PlayerSoulData() {
        this(1, new HashMap<>());
    }

    public PlayerSoulData(int soulLevel, Map<ResourceLocation, Integer> allocatedPoints) {
        this.soulLevel = soulLevel;
        this.allocatedPoints = new HashMap<>(allocatedPoints);
    }

    public int getSoulLevel() { return soulLevel; }
    public void setSoulLevel(int soulLevel) { this.soulLevel = soulLevel; }

    public Map<ResourceLocation, Integer> getAllocatedPoints() { return allocatedPoints; }

    public int getStatLevel(ResourceLocation statId) {
        return allocatedPoints.getOrDefault(statId, 0);
    }

    public void setStatLevel(ResourceLocation statId, int level) {
        if (level <= 0) {
            allocatedPoints.remove(statId);
        } else {
            allocatedPoints.put(statId, level);
        }
    }

    public void incrementStat(ResourceLocation statId) {
        allocatedPoints.merge(statId, 1, Integer::sum);
        soulLevel++;
    }

    public int getTotalAllocatedPoints() {
        return allocatedPoints.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void reset() {
        soulLevel = 1;
        allocatedPoints.clear();
    }

    public PlayerSoulData copy() {
        return new PlayerSoulData(soulLevel, new HashMap<>(allocatedPoints));
    }
}
