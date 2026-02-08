package net.simonofsamaria.soulslikespells.gui.bonfire;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 篝火界面统一弹窗：询问洗点、洗点失败、中止升级、退出洗点等。
 * 不渲染全屏模糊，保持弹窗清晰。
 */
public class BonfireDialogScreen extends Screen {

    public enum Type { CONFIRM, ALERT }

    private final Screen returnScreen;
    private final Component message;
    private final Type type;
    private final Runnable onConfirm;

    /** 是/否确认（如洗点确认、中止升级、退出洗点） */
    public static BonfireDialogScreen confirm(Screen returnScreen, Component title, Component message, Runnable onConfirm) {
        return new BonfireDialogScreen(returnScreen, title, message, Type.CONFIRM, onConfirm);
    }

    /** 仅确定提示（如洗点失败） */
    public static BonfireDialogScreen alert(Screen returnScreen, Component title, Component message) {
        return new BonfireDialogScreen(returnScreen, title, message, Type.ALERT, null);
    }

    private BonfireDialogScreen(Screen returnScreen, Component title, Component message, Type type, Runnable onConfirm) {
        super(title);
        this.returnScreen = returnScreen;
        this.message = message;
        this.type = type;
        this.onConfirm = onConfirm;
    }

    public Screen getReturnScreen() {
        return returnScreen;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = width / 2;
        int btnY = height / 2 + 10;
        int btnWidth = 120;

        if (type == Type.CONFIRM) {
            addRenderableWidget(Button.builder(Component.translatable("gui.yes"), btn -> {
                if (onConfirm != null) onConfirm.run();
            }).bounds(centerX - btnWidth - 5, btnY, btnWidth, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.no"), btn -> returnToPrevious()).bounds(centerX + 5, btnY, btnWidth, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.translatable("gui.ok"), btn -> returnToPrevious()).bounds(centerX - btnWidth / 2, btnY, btnWidth, 20).build());
        }
    }

    private void returnToPrevious() {
        if (minecraft != null) {
            minecraft.setScreen(returnScreen);
        }
    }

    @Override
    public void renderBackground(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 不渲染全屏模糊，保持弹窗清晰
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        var lines = font.split(message, (int) (width * 0.9));
        int y = height / 2 - 20 - lines.size() * 6;
        for (var line : lines) {
            graphics.drawCenteredString(font, line, width / 2, y, 0xFFFFFF);
            y += 12;
        }
    }
}
