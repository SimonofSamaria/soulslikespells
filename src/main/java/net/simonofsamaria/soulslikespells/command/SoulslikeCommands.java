package net.simonofsamaria.soulslikespells.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.network.SyncSoulDataPayload;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;
import net.simonofsamaria.soulslikespells.scaling.ScalingManager;

public class SoulslikeCommands {

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("soulslike")
                .requires(source -> source.hasPermission(2))
                // /soulslike get <player>
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "[SoulslikeSpells] " + player.getName().getString() +
                                                    " - Soul Level: " + data.getSoulLevel() +
                                                    ", Experience: " + data.getExperience() +
                                                    ", Points: " + data.getAllocatedPoints()
                                    ), false);
                                    return 1;
                                })))
                // /soulslike set stat <player> <stat> <level>
                .then(Commands.literal("set")
                        .then(Commands.literal("stat")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("stat", StringArgumentType.string())
                                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 99))
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                            String stat = StringArgumentType.getString(ctx, "stat");
                                                            int level = IntegerArgumentType.getInteger(ctx, "level");
                                                            ResourceLocation statId = ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, stat);

                                                            PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
                                                            data.setStatLevel(statId, level);
                                                            data.setSoulLevel(1 + data.getTotalAllocatedPoints());

                                                            ScalingManager.getInstance().recalculateAll(player);
                                                            SyncSoulDataPayload.sendToPlayer(player);

                                                            ctx.getSource().sendSuccess(() -> Component.literal(
                                                                    "[SoulslikeSpells] Set " + stat + " to " + level +
                                                                            " for " + player.getName().getString()
                                                            ), true);
                                                            return 1;
                                                        })))))
                        // /soulslike set experience <player> <amount>
                        .then(Commands.literal("experience")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                                    PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
                                                    data.setExperience(amount);
                                                    SyncSoulDataPayload.sendToPlayer(player);

                                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                                            "[SoulslikeSpells] Set experience to " + amount +
                                                                    " for " + player.getName().getString()
                                                    ), true);
                                                    return 1;
                                                })))))
                // /soulslike reset <player>
                .then(Commands.literal("reset")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
                                    data.reset();

                                    ScalingManager.getInstance().recalculateAll(player);
                                    SyncSoulDataPayload.sendToPlayer(player);

                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "[SoulslikeSpells] Reset all stats for " + player.getName().getString()
                                    ), true);
                                    return 1;
                                })))
        );
    }
}
