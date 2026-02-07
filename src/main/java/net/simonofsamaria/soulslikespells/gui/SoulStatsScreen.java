package net.simonofsamaria.soulslikespells.gui;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;
import net.simonofsamaria.soulslikespells.registry.ModStatTypes;
import net.simonofsamaria.soulslikespells.scaling.LevelCostCalculator;

public class SoulStatsScreen extends Screen {

    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 280;
    private static final int BG_COLOR = 0xEE1A1A2E;
    private static final int BORDER_COLOR = 0xFF8B7355;
    private static final int TITLE_COLOR = 0xFFD4AF37;
    private static final int LABEL_COLOR = 0xFFE0D8C0;
    private static final int VALUE_COLOR = 0xFFFFD700;
    private static final int SECTION_COLOR = 0xFFB8860B;

    private int leftPos;
    private int topPos;

    public SoulStatsScreen() {
        super(GameNarrator.NO_TITLE);
    }

    @Override
    protected void init() {
        super.init();
        leftPos = (width - PANEL_WIDTH) / 2;
        topPos = (height - PANEL_HEIGHT) / 2;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);

        // Panel background
        graphics.fill(leftPos - 2, topPos - 2, leftPos + PANEL_WIDTH + 2, topPos + PANEL_HEIGHT + 2, BORDER_COLOR);
        graphics.fill(leftPos, topPos, leftPos + PANEL_WIDTH, topPos + PANEL_HEIGHT, BG_COLOR);

        if (minecraft == null || minecraft.player == null) return;

        PlayerSoulData data = minecraft.player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
        int cost = LevelCostCalculator.getCost(data.getSoulLevel());

        int y = topPos + 12;

        // Title
        String title = Component.translatable("gui.soulslikespells.soul_stats.title").getString();
        graphics.drawCenteredString(font, title, leftPos + PANEL_WIDTH / 2, y, TITLE_COLOR);
        y += 20;

        // Soul level, souls, cost
        graphics.drawString(font, Component.translatable("gui.soulslikespells.soul_stats.soul_level").getString(), leftPos + 12, y, LABEL_COLOR);
        graphics.drawString(font, String.valueOf(data.getSoulLevel()), leftPos + 140, y, VALUE_COLOR);
        y += 14;

        graphics.drawString(font, Component.translatable("gui.soulslikespells.soul_stats.souls").getString(), leftPos + 12, y, LABEL_COLOR);
        graphics.drawString(font, String.valueOf(minecraft.player.totalExperience), leftPos + 140, y, VALUE_COLOR);
        y += 14;

        graphics.drawString(font, Component.translatable("gui.soulslikespells.soul_stats.cost_to_level").getString(), leftPos + 12, y, LABEL_COLOR);
        graphics.drawString(font, String.valueOf(cost), leftPos + 140, y, VALUE_COLOR);
        y += 22;

        // Stats section
        graphics.drawString(font, Component.translatable("gui.soulslikespells.soul_stats.stats_section").getString(), leftPos + 12, y, SECTION_COLOR);
        y += 16;

        renderStatRow(graphics, ModStatTypes.MIND_ID, data.getStatLevel(ModStatTypes.MIND_ID), y);
        y += 14;
        renderStatRow(graphics, ModStatTypes.DEXTERITY_ID, data.getStatLevel(ModStatTypes.DEXTERITY_ID), y);
        y += 14;
        renderStatRow(graphics, ModStatTypes.INTELLIGENCE_ID, data.getStatLevel(ModStatTypes.INTELLIGENCE_ID), y);
        y += 14;
        renderStatRow(graphics, ModStatTypes.FAITH_ID, data.getStatLevel(ModStatTypes.FAITH_ID), y);
        y += 14;
        renderStatRow(graphics, ModStatTypes.ARCANE_ID, data.getStatLevel(ModStatTypes.ARCANE_ID), y);
        y += 22;

        // ISS attributes section
        graphics.drawString(font, Component.translatable("gui.soulslikespells.soul_stats.iss_section").getString(), leftPos + 12, y, SECTION_COLOR);
        y += 16;

        String[] issAttrs = {
                "irons_spellbooks:spell_power",
                "irons_spellbooks:max_mana",
                "irons_spellbooks:mana_regen",
                "irons_spellbooks:cooldown_reduction",
                "irons_spellbooks:cast_time_reduction",
                "irons_spellbooks:spell_resist",
                "irons_spellbooks:summon_damage",
        };
        String[] issKeys = {
                "gui.soulslikespells.iss.spell_power",
                "gui.soulslikespells.iss.max_mana",
                "gui.soulslikespells.iss.mana_regen",
                "gui.soulslikespells.iss.cooldown_reduction",
                "gui.soulslikespells.iss.cast_time_reduction",
                "gui.soulslikespells.iss.spell_resist",
                "gui.soulslikespells.iss.summon_damage",
        };

        for (int i = 0; i < issAttrs.length && y < topPos + PANEL_HEIGHT - 20; i++) {
            double value = getAttributeValue(issAttrs[i]);
            graphics.drawString(font, Component.translatable(issKeys[i]).getString() + ":", leftPos + 12, y, LABEL_COLOR);
            graphics.drawString(font, formatAttributeValue(value, issAttrs[i]), leftPos + 160, y, VALUE_COLOR);
            y += 14;
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderStatRow(GuiGraphics graphics, ResourceLocation statId, int value, int y) {
        String key = "stat." + statId.getNamespace() + "." + statId.getPath();
        graphics.drawString(font, Component.translatable(key).getString() + ":", leftPos + 12, y, LABEL_COLOR);
        graphics.drawString(font, String.valueOf(value), leftPos + 140, y, VALUE_COLOR);
    }

    private double getAttributeValue(String attrId) {
        if (minecraft == null || minecraft.player == null) return 0;
        var holderOpt = BuiltInRegistries.ATTRIBUTE.getHolder(
                ResourceKey.create(Registries.ATTRIBUTE, ResourceLocation.parse(attrId))
        );
        return holderOpt.map(h -> minecraft.player.getAttributeValue(h)).orElse(0.0);
    }

    private String formatAttributeValue(double value, String attrId) {
        // mana_regen, cooldown_reduction, cast_time_reduction, spell_resist, spell_power, summon_damage are multipliers (1.0 = 100%)
        if (attrId.contains("mana_regen") || attrId.contains("cooldown") || attrId.contains("cast_time") ||
                attrId.contains("spell_resist") || attrId.contains("spell_power") || attrId.contains("summon_damage")) {
            return String.format("%.1f%%", value * 100);
        }
        return String.format("%.1f", value);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && (keyCode == 256 || keyCode == minecraft.options.keyInventory.getKey().getValue())) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
