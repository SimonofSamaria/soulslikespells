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
                    Codec.INT.fieldOf("experience").forGetter(PlayerSoulData::getExperience),
                    Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT)
                            .fieldOf("allocated_points")
                            .forGetter(PlayerSoulData::getAllocatedPoints)
            ).apply(instance, PlayerSoulData::new)
    );

    private int soulLevel;
    private int experience;
    private final Map<ResourceLocation, Integer> allocatedPoints;

    public PlayerSoulData() {
        this(1, 0, new HashMap<>());
    }

    public PlayerSoulData(int soulLevel, int experience, Map<ResourceLocation, Integer> allocatedPoints) {
        this.soulLevel = soulLevel;
        this.experience = experience;
        this.allocatedPoints = new HashMap<>(allocatedPoints);
    }

    public int getSoulLevel() { return soulLevel; }
    public void setSoulLevel(int soulLevel) { this.soulLevel = soulLevel; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
    public void addExperience(int amount) { this.experience += amount; }

    public boolean spendExperience(int amount) {
        if (experience >= amount) {
            experience -= amount;
            return true;
        }
        return false;
    }

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
        return new PlayerSoulData(soulLevel, experience, new HashMap<>(allocatedPoints));
    }
}
