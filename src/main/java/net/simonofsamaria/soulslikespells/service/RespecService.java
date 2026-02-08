package net.simonofsamaria.soulslikespells.service;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.simonofsamaria.soulslikespells.config.SoulslikeCommonConfig;

/**
 * 洗点服务：统一处理洗点物品的检查与消耗逻辑。
 */
public final class RespecService {

    private RespecService() {}

    /** 免费洗点时的返回值 */
    public static final int FREE_RESPEC = -2;

    /**
     * 检查玩家是否拥有足够洗点物品。
     */
    public static boolean hasRespecItem(ServerPlayer player) {
        int amount = SoulslikeCommonConfig.getRespecAmount();
        if (amount <= 0) return true;
        var item = SoulslikeCommonConfig.getRespecItem();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item) && stack.getCount() >= amount) return true;
        }
        return false;
    }

    /**
     * 消耗洗点物品。返回消耗的槽位索引，FREE_RESPEC 表示免费，-1 表示不足。
     */
    public static int consumeRespecItem(ServerPlayer player) {
        int amount = SoulslikeCommonConfig.getRespecAmount();
        if (amount <= 0) return FREE_RESPEC;
        var item = SoulslikeCommonConfig.getRespecItem();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item) && stack.getCount() >= amount) {
                stack.shrink(amount);
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
                return i;
            }
        }
        return -1;
    }
}
