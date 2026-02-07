package net.simonofsamaria.soulslikespells.api.stat;

/**
 * Represents a stat type in the Soulslike stat system.
 * Each StatType is registered in the custom STAT_TYPE registry.
 */
public class StatType {
    private final int maxLevel;
    private final int defaultValue;
    private final int iconU;
    private final int iconV;

    public StatType(int maxLevel, int defaultValue, int iconU, int iconV) {
        this.maxLevel = maxLevel;
        this.defaultValue = defaultValue;
        this.iconU = iconU;
        this.iconV = iconV;
    }

    public StatType(int maxLevel, int defaultValue) {
        this(maxLevel, defaultValue, 0, 0);
    }

    /** Maximum level this stat can reach */
    public int getMaxLevel() { return maxLevel; }

    /** Default starting value of this stat */
    public int getDefaultValue() { return defaultValue; }

    /** Icon texture U offset for GUI rendering */
    public int getIconU() { return iconU; }

    /** Icon texture V offset for GUI rendering */
    public int getIconV() { return iconV; }
}
