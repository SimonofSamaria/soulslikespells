package net.simonofsamaria.soulslikespells.service;

import net.minecraft.server.level.ServerPlayer;
import net.simonofsamaria.soulslikespells.catalyst.CatalystAttributeHandler;
import net.simonofsamaria.soulslikespells.network.SyncSoulDataPayload;
import net.simonofsamaria.soulslikespells.scaling.ScalingManager;

/**
 * 统一服务：玩家属性重算并同步到客户端。
 * 当等级、洗点、装备等变更时调用，避免各模块重复逻辑。
 */
public final class PlayerStatService {

    private PlayerStatService() {}

    /**
     * 重算所有属性修饰符（stat scaling + catalyst）并同步 SoulData 到客户端。
     */
    public static void recalculateAndSync(ServerPlayer player) {
        ScalingManager.getInstance().recalculateAll(player);
        CatalystAttributeHandler.applyCatalystModifiers(player);
        SyncSoulDataPayload.sendToPlayer(player);
    }
}
