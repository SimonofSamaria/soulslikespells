package net.simonofsamaria.soulslikespells.scaling;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.api.stat.ScalingCurve;
import net.simonofsamaria.soulslikespells.api.stat.StatScaling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reload listener that loads scaling curve definitions from data packs.
 * Loads JSON files from data/{namespace}/soulslikespells/scaling/*.json
 */
public class ScalingReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();

    public ScalingReloadListener() {
        super(GSON, "soulslikespells/scaling");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        ScalingManager manager = ScalingManager.getInstance();
        manager.clear();
        int count = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                StatScaling scaling = parseScaling(json);
                manager.addScaling(scaling);
                count++;
            } catch (Exception e) {
                SoulslikeSpells.LOGGER.error("Failed to load scaling definition {}: {}", entry.getKey(), e.getMessage());
            }
        }

        SoulslikeSpells.LOGGER.info("Loaded {} scaling definitions", count);
    }

    private StatScaling parseScaling(JsonObject json) {
        ResourceLocation statId = ResourceLocation.parse(json.get("stat").getAsString());
        ResourceLocation targetAttribute = ResourceLocation.parse(json.get("target_attribute").getAsString());

        String operationStr = json.get("operation").getAsString();
        AttributeModifier.Operation operation = switch (operationStr) {
            case "ADD_VALUE" -> AttributeModifier.Operation.ADD_VALUE;
            case "ADD_MULTIPLIED_BASE" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "ADD_MULTIPLIED_TOTAL" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> throw new IllegalArgumentException("Unknown operation: " + operationStr);
        };

        JsonArray breakpointsJson = json.getAsJsonArray("breakpoints");
        List<ScalingCurve.Breakpoint> breakpoints = new ArrayList<>();
        for (JsonElement bp : breakpointsJson) {
            JsonObject bpObj = bp.getAsJsonObject();
            breakpoints.add(new ScalingCurve.Breakpoint(
                    bpObj.get("level").getAsInt(),
                    bpObj.get("bonus_per_point").getAsDouble()
            ));
        }

        return new StatScaling(statId, targetAttribute, operation, new ScalingCurve(breakpoints));
    }
}
