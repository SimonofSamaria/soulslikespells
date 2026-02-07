package net.simonofsamaria.soulslikespells.util;

import net.minecraft.world.entity.player.Player;

/**
 * Helper for vanilla Minecraft experience. Uses Player.totalExperience (read) and
 * Player.giveExperiencePoints(int) (write; supports negative to deduct).
 * Soul level is independent of vanilla level.
 */
public final class VanillaExperienceHelper {

    private VanillaExperienceHelper() {}

    /**
     * Get the player's total experience points (vanilla field).
     */
    public static int getExperience(Player player) {
        return player.totalExperience;
    }

    /**
     * Check if the player has at least the given amount of experience.
     */
    public static boolean hasExperience(Player player, int amount) {
        return player.totalExperience >= amount;
    }

    /**
     * Deduct experience from the player. Returns true if successful.
     * Uses vanilla giveExperiencePoints(-amount) which handles level/progress internally.
     */
    public static boolean deductExperience(Player player, int amount) {
        if (amount <= 0 || player.totalExperience < amount) {
            return false;
        }
        player.giveExperiencePoints(-amount);
        return true;
    }

    /**
     * Set the player's total experience. Uses vanilla API to update level/progress correctly.
     */
    public static void setExperience(Player player, int totalXp) {
        totalXp = Math.max(0, totalXp);
        player.giveExperiencePoints(totalXp - player.totalExperience);
    }
}
