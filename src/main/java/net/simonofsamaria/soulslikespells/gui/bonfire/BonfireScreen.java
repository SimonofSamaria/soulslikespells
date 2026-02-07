package net.simonofsamaria.soulslikespells.gui.bonfire;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.simonofsamaria.soulslikespells.network.LevelUpStatPayload;
import net.simonofsamaria.soulslikespells.registry.ModStatTypes;
import net.simonofsamaria.soulslikespells.scaling.LevelCostCalculator;

import java.util.ArrayList;
import java.util.List;

public class BonfireScreen extends AbstractContainerScreen<BonfireMenu> {

    private static final int BG_COLOR = 0xCC1A1A2E;
    private static final int BORDER_COLOR = 0xFF8B7355;
    private static final int TITLE_COLOR = 0xFFD4AF37;
    private static final int TEXT_COLOR = 0xFFE0D8C0;
    private static final int VALUE_COLOR = 0xFFFFD700;
    private static final int COST_COLOR = 0xFF90EE90;
    private static final int DISABLED_COLOR = 0xFF808080;

    private final List<StatButton> statButtons = new ArrayList<>();

    private static final StatInfo[] STATS = {
            new StatInfo(BonfireMenu.DATA_VIGOR, "Vigor", ModStatTypes.VIGOR_ID),
            new StatInfo(BonfireMenu.DATA_MIND, "Mind", ModStatTypes.MIND_ID),
            new StatInfo(BonfireMenu.DATA_ENDURANCE, "Endurance", ModStatTypes.ENDURANCE_ID),
            new StatInfo(BonfireMenu.DATA_STRENGTH, "Strength", ModStatTypes.STRENGTH_ID),
            new StatInfo(BonfireMenu.DATA_DEXTERITY, "Dexterity", ModStatTypes.DEXTERITY_ID),
            new StatInfo(BonfireMenu.DATA_INTELLIGENCE, "Intelligence", ModStatTypes.INTELLIGENCE_ID),
            new StatInfo(BonfireMenu.DATA_FAITH, "Faith", ModStatTypes.FAITH_ID),
            new StatInfo(BonfireMenu.DATA_ARCANE, "Arcane", ModStatTypes.ARCANE_ID),
    };

    record StatInfo(int dataIndex, String name, ResourceLocation statId) {}

    public BonfireScreen(BonfireMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();
        statButtons.clear();

        int startX = leftPos + 30;
        int startY = topPos + 55;

        for (int i = 0; i < STATS.length; i++) {
            StatInfo stat = STATS[i];
            int y = startY + i * 18;
            StatButton button = new StatButton(
                    startX + 180, y, 20, 16,
                    Component.literal("+"),
                    btn -> onLevelUp(stat.statId()),
                    stat.statId()
            );
            statButtons.add(button);
            addRenderableWidget(button);
        }
    }

    private void onLevelUp(ResourceLocation statId) {
        PacketDistributor.sendToServer(new LevelUpStatPayload(statId));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Dark background
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, BG_COLOR);
        // Border
        drawBorder(graphics, leftPos, topPos, imageWidth, imageHeight, BORDER_COLOR);
        // Title area highlight
        graphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + 24, 0x44D4AF37);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Title
        String title = "— LEVEL UP —";
        int titleWidth = font.width(title);
        graphics.drawString(font, title, leftPos + (imageWidth - titleWidth) / 2, topPos + 8, TITLE_COLOR, true);

        // Soul Level
        String soulLevelText = "Soul Level: " + menu.getSoulLevel();
        graphics.drawString(font, soulLevelText, leftPos + 10, topPos + 30, VALUE_COLOR, true);

        // Experience
        String expText = "Souls: " + menu.getExperience();
        graphics.drawString(font, expText, leftPos + 140, topPos + 30, VALUE_COLOR, true);

        // Level up cost
        int cost = LevelCostCalculator.getCost(menu.getSoulLevel());
        String costText = "Cost: " + cost;
        int costColor = menu.getExperience() >= cost ? COST_COLOR : DISABLED_COLOR;
        graphics.drawString(font, costText, leftPos + 10, topPos + 42, costColor, true);

        // Divider
        graphics.fill(leftPos + 5, topPos + 53, leftPos + imageWidth - 5, topPos + 54, BORDER_COLOR);

        // Stat rows
        int startX = leftPos + 30;
        int startY = topPos + 55;
        for (int i = 0; i < STATS.length; i++) {
            StatInfo stat = STATS[i];
            int y = startY + i * 18;
            int value = menu.getStatValue(stat.dataIndex());

            // Stat name
            graphics.drawString(font, stat.name(), startX, y + 4, TEXT_COLOR, true);
            // Stat value
            graphics.drawString(font, String.valueOf(value), startX + 120, y + 4, VALUE_COLOR, true);

            // Update button state
            if (i < statButtons.size()) {
                statButtons.get(i).active = menu.getExperience() >= cost && value < 99;
            }
        }

        // Bottom divider
        int bottomY = startY + STATS.length * 18 + 4;
        graphics.fill(leftPos + 5, bottomY, leftPos + imageWidth - 5, bottomY + 1, BORDER_COLOR);

        // Hint text
        String hint = "Click + to allocate points";
        int hintWidth = font.width(hint);
        graphics.drawString(font, hint, leftPos + (imageWidth - hintWidth) / 2, bottomY + 6, DISABLED_COLOR, true);

        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Don't render default labels
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 2, color);
        graphics.fill(x, y + height - 2, x + width, y + height, color);
        graphics.fill(x, y, x + 2, y + height, color);
        graphics.fill(x + width - 2, y, x + width, y + height, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
