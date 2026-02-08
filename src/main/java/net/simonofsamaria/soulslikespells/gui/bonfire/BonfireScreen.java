package net.simonofsamaria.soulslikespells.gui.bonfire;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.simonofsamaria.soulslikespells.config.SoulslikeCommonConfig;
import net.simonofsamaria.soulslikespells.api.stat.StatType;
import net.simonofsamaria.soulslikespells.network.ConfirmLevelUpPayload;
import net.simonofsamaria.soulslikespells.network.RespecApplyPayload;
import net.simonofsamaria.soulslikespells.network.RespecRequestPayload;
import net.simonofsamaria.soulslikespells.registry.ModRegistries;
import net.simonofsamaria.soulslikespells.registry.ModStatTypes;
import net.simonofsamaria.soulslikespells.scaling.LevelCostCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BonfireScreen extends AbstractContainerScreen<BonfireMenu> {

    private static final int BG_COLOR = 0xCC1A1A2E;
    private static final int BORDER_COLOR = 0xFF8B7355;
    private static final int TITLE_COLOR = 0xFFD4AF37;
    private static final int TEXT_COLOR = 0xFFE0D8C0;
    private static final int VALUE_COLOR = 0xFFFFD700;
    private static final int PROJECTED_COLOR = 0xFF6495ED; // blue for pending
    private static final int COST_COLOR = 0xFF90EE90;
    private static final int COST_INSUFFICIENT_COLOR = 0xFFFF6666; // red when souls < cost
    private static final int DISABLED_COLOR = 0xFF808080;
    private static final StatInfo[] STATS = {
            new StatInfo(BonfireMenu.DATA_MIND, ModStatTypes.MIND_ID),
            new StatInfo(BonfireMenu.DATA_DEXTERITY, ModStatTypes.DEXTERITY_ID),
            new StatInfo(BonfireMenu.DATA_INTELLIGENCE, ModStatTypes.INTELLIGENCE_ID),
            new StatInfo(BonfireMenu.DATA_FAITH, ModStatTypes.FAITH_ID),
            new StatInfo(BonfireMenu.DATA_ARCANE, ModStatTypes.ARCANE_ID),
    };

    record StatInfo(int dataIndex, ResourceLocation statId) {}

    // Pending allocations (client-side, before confirm)
    private final Map<ResourceLocation, Integer> pendingDeltas = new HashMap<>();

    // Respec mode
    private boolean respecMode = false;
    private int respecOriginalLevel = 0;

    private final List<StatButton> plusButtons = new ArrayList<>();
    private final List<StatButton> minusButtons = new ArrayList<>();
    private Button confirmButton;
    private Button respecButton;

    public BonfireScreen(BonfireMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 240;
    }

    @Override
    protected void init() {
        super.init();
        plusButtons.clear();
        minusButtons.clear();

        int startX = leftPos + 30;
        int startY = topPos + 78;
        int colProjected = startX + 130;
        int btnPlusX = colProjected + 50;
        int btnMinusX = colProjected + 30;

        for (int i = 0; i < STATS.length; i++) {
            StatInfo stat = STATS[i];
            int y = startY + i * 18;

            StatButton plusBtn = new StatButton(btnPlusX, y, 16, 14, Component.literal("+"),
                    btn -> modifyPending(stat.statId(), 1), stat.statId());
            StatButton minusBtn = new StatButton(btnMinusX, y, 16, 14, Component.literal("-"),
                    btn -> modifyPending(stat.statId(), -1), stat.statId());

            plusButtons.add(plusBtn);
            minusButtons.add(minusBtn);
            addRenderableWidget(plusBtn);
            addRenderableWidget(minusBtn);
        }

        confirmButton = Button.builder(
                Component.translatable("gui.soulslikespells.bonfire.confirm"),
                btn -> onConfirm()
        ).bounds(leftPos + 70, topPos + 195, 116, 20).build();
        addRenderableWidget(confirmButton);

        if (!respecMode) {
            respecButton = Button.builder(
                    Component.translatable("gui.soulslikespells.bonfire.respec"),
                    btn -> onRespecClick()
            ).bounds(leftPos + 70, topPos + 216, 116, 20).build();
            addRenderableWidget(respecButton);
        }
    }

    private void modifyPending(ResourceLocation statId, int delta) {
        int current = pendingDeltas.getOrDefault(statId, 0);
        int next = current + delta;
        if (next < 0) return;

        int baseValue = respecMode ? 0 : menu.getStatValue(getDataIndex(statId));
        int projected = baseValue + next;
        int maxLevel = getMaxLevelForStat(statId);
        if (projected > maxLevel) return;

        if (respecMode) {
            int totalPending = getTotalPending();
            if (delta > 0 && totalPending + 1 > respecOriginalLevel) return;
        } else {
            int souls = getSouls();
            long newCost = getTotalCostForPending(delta);
            if (delta > 0 && souls < newCost) return;
        }

        pendingDeltas.put(statId, next);
    }

    private int getDataIndex(ResourceLocation statId) {
        for (StatInfo s : STATS) {
            if (s.statId().equals(statId)) return s.dataIndex();
        }
        return -1;
    }

    private int getTotalPending() {
        return pendingDeltas.values().stream().mapToInt(Integer::intValue).sum();
    }

    private long getTotalCostForPending(int extraPoint) {
        int baseLevel = respecMode ? 0 : menu.getSoulLevel();
        int totalPoints = getTotalPending() + extraPoint;
        long cost = 0;
        for (int i = 0; i < totalPoints; i++) {
            cost += LevelCostCalculator.getCost(baseLevel + i);
        }
        return cost;
    }

    private void onConfirm() {
        if (hasNoPending()) return;

        if (respecMode) {
            int total = getTotalPending();
            if (total != respecOriginalLevel) return;

            Map<ResourceLocation, Integer> allocation = new HashMap<>();
            for (StatInfo s : STATS) {
                int val = pendingDeltas.getOrDefault(s.statId(), 0);
                if (val > 0) allocation.put(s.statId(), val);
            }
            PacketDistributor.sendToServer(new RespecApplyPayload(allocation, respecOriginalLevel));
            exitRespecMode();
        } else {
            Map<ResourceLocation, Integer> deltas = new HashMap<>(pendingDeltas);
            deltas.entrySet().removeIf(e -> e.getValue() <= 0);
            if (deltas.isEmpty()) return;
            PacketDistributor.sendToServer(new ConfirmLevelUpPayload(deltas));
            pendingDeltas.clear();
        }

        if (minecraft != null) minecraft.setScreen(null);
    }

    private void onRespecClick() {
        if (minecraft == null) return;
        BonfireScreen self = this;
        Component confirmText = getRespecConfirmText();
        minecraft.setScreen(BonfireDialogScreen.confirm(self,
                Component.translatable("gui.soulslikespells.bonfire.respec_confirm_title"),
                confirmText,
                () -> PacketDistributor.sendToServer(new RespecRequestPayload())));
    }

    private static Component getRespecConfirmText() {
        int amount = SoulslikeCommonConfig.getRespecAmount();
        if (amount <= 0) {
            return Component.translatable("gui.soulslikespells.bonfire.respec_confirm_text_free");
        }
        var item = SoulslikeCommonConfig.getRespecItem();
        Component itemName = new ItemStack(item).getHoverName();
        return Component.translatable("gui.soulslikespells.bonfire.respec_confirm_text", amount, itemName);
    }

    public void enterRespecMode(int originalLevel) {
        respecMode = true;
        respecOriginalLevel = originalLevel;
        pendingDeltas.clear();
        init(minecraft, width, height);
    }

    private void exitRespecMode() {
        respecMode = false;
        respecOriginalLevel = 0;
        pendingDeltas.clear();
    }

    private void showAbortUpgradeDialog() {
        if (minecraft == null) return;
        BonfireScreen self = this;
        minecraft.setScreen(BonfireDialogScreen.confirm(self,
                Component.translatable("gui.soulslikespells.bonfire.abort_upgrade_title"),
                Component.translatable("gui.soulslikespells.bonfire.abort_upgrade_text"),
                () -> {
                    pendingDeltas.clear();
                    if (minecraft != null) minecraft.setScreen(null);
                }));
    }

    private void showExitRespecDialog() {
        if (minecraft == null) return;
        BonfireScreen self = this;
        minecraft.setScreen(BonfireDialogScreen.confirm(self,
                Component.translatable("gui.soulslikespells.bonfire.respec_exit_title"),
                Component.translatable("gui.soulslikespells.bonfire.respec_exit_text"),
                () -> {
                    exitRespecMode();
                    if (minecraft != null) minecraft.setScreen(null);
                }));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft == null) return super.keyPressed(keyCode, scanCode, modifiers);
        // 使用 matches 而非 isActiveAndMatches，避免 GUI 打开时 context 导致 E 键不生效
        boolean isCloseKey = keyCode == 256 || minecraft.options.keyInventory.matches(keyCode, scanCode);
        if (!isCloseKey) return super.keyPressed(keyCode, scanCode, modifiers);
        if (respecMode) {
            showExitRespecDialog();
            return true;
        }
        if (!hasNoPending()) {
            showAbortUpgradeDialog();
            return true;
        }
        onClose();
        return true;
    }

    private int getSouls() {
        return minecraft != null && minecraft.player != null ? minecraft.player.totalExperience : 0;
    }

    private boolean hasNoPending() {
        return pendingDeltas.values().stream().allMatch(v -> v <= 0);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, BG_COLOR);
        drawBorder(graphics, leftPos, topPos, imageWidth, imageHeight, BORDER_COLOR);
        graphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + 24, 0x44D4AF37);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        String title = respecMode
                ? Component.translatable("gui.soulslikespells.bonfire.title_respec").getString()
                : Component.translatable("gui.soulslikespells.bonfire.title").getString();
        int titleWidth = font.width(title);
        graphics.drawString(font, title, leftPos + (imageWidth - titleWidth) / 2, topPos + 8, TITLE_COLOR, true);

        // Level row
        int baseLevel = respecMode ? 0 : menu.getSoulLevel();
        int projectedLevel = baseLevel + getTotalPending();
        String levelText = Component.translatable("gui.soulslikespells.bonfire.level").getString();
        graphics.drawString(font, levelText + baseLevel + " -> " + projectedLevel, leftPos + 10, topPos + 32, VALUE_COLOR, true);

        // Souls row
        int souls = getSouls();
        long totalCostOfPending = getTotalCostForPending(0);
        int costForNextLevel = LevelCostCalculator.getCost(projectedLevel); // 升到下一级所需
        int projectedSouls;
        String soulsText = Component.translatable("gui.soulslikespells.bonfire.souls").getString();
        if (respecMode) {
            long refund = 0;
            for (int i = 0; i < respecOriginalLevel; i++) {
                refund += LevelCostCalculator.getCost(i);
            }
            projectedSouls = souls + (int) refund - (int) totalCostOfPending;
            graphics.drawString(font, soulsText + souls + " -> " + projectedSouls, leftPos + 10, topPos + 44, VALUE_COLOR, true);
        } else {
            projectedSouls = souls - (int) totalCostOfPending;
            if (totalCostOfPending > 0) {
                graphics.drawString(font, soulsText + souls + " -> " + projectedSouls, leftPos + 10, topPos + 44, VALUE_COLOR, true);
            } else {
                graphics.drawString(font, soulsText + souls, leftPos + 10, topPos + 44, VALUE_COLOR, true);
            }
        }
        String costText = Component.translatable("gui.soulslikespells.bonfire.cost").getString() + costForNextLevel;
        int costColor = projectedSouls >= costForNextLevel ? COST_COLOR : COST_INSUFFICIENT_COLOR;
        graphics.drawString(font, costText, leftPos + 10, topPos + 56, costColor, true);

        // Divider
        graphics.fill(leftPos + 5, topPos + 66, leftPos + imageWidth - 5, topPos + 67, BORDER_COLOR);

        // Ability header
        graphics.drawString(font, Component.translatable("gui.soulslikespells.bonfire.ability_header").getString(), leftPos + 30, topPos + 70, TITLE_COLOR, true);

        // Stat rows - three columns: name, current, projected
        int startY = topPos + 78;
        for (int i = 0; i < STATS.length; i++) {
            StatInfo stat = STATS[i];
            int y = startY + i * 18;
            int current = respecMode ? 0 : menu.getStatValue(stat.dataIndex());
            int pending = pendingDeltas.getOrDefault(stat.statId(), 0);
            int projected = current + pending;

            String statKey = "stat." + stat.statId().getNamespace() + "." + stat.statId().getPath();
            graphics.drawString(font, Component.translatable(statKey).getString(), leftPos + 30, y + 4, TEXT_COLOR, true);
            graphics.drawString(font, String.valueOf(current), leftPos + 120, y + 4, VALUE_COLOR, true);
            graphics.drawString(font, String.valueOf(projected), leftPos + 160, y + 4, pending > 0 ? PROJECTED_COLOR : VALUE_COLOR, true);

            // Update button states
            int maxLevel = getMaxLevelForStat(stat.statId());
            boolean canAdd = projected < maxLevel;
            if (respecMode) {
                canAdd = canAdd && getTotalPending() < respecOriginalLevel && projectedSouls >= costForNextLevel;
            } else {
                canAdd = canAdd && projectedSouls >= costForNextLevel;
            }
            plusButtons.get(i).active = canAdd;
            minusButtons.get(i).active = pending > 0;
        }

        // Confirm button
        confirmButton.active = !hasNoPending();
        if (respecMode) {
            confirmButton.active = confirmButton.active && getTotalPending() == respecOriginalLevel;
        }

        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 2, color);
        graphics.fill(x, y + height - 2, x + width, y + height, color);
        graphics.fill(x, y, x + 2, y + height, color);
        graphics.fill(x + width - 2, y, x + width, y + height, color);
    }

    private int getMaxLevelForStat(ResourceLocation statId) {
        if (ModRegistries.STAT_TYPE_REGISTRY != null) {
            StatType type = ModRegistries.STAT_TYPE_REGISTRY.get(statId);
            if (type != null) return type.getMaxLevel();
        }
        return 99;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
