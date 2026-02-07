package net.simonofsamaria.soulslikespells.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
                                                    ", Experience: " + player.totalExperience +
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
                                                        }))))))
                // /soulslike inspect <player> - show all ISS magic attributes
                .then(Commands.literal("inspect")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("§6[SoulslikeSpells] §eAttribute Inspect: §f").append(player.getName().getString()).append("\n");

                                    // Iron's Spells attributes we scale
                                    String[] issAttrs = {
                                            "irons_spellbooks:spell_power",
                                            "irons_spellbooks:max_mana",
                                            "irons_spellbooks:mana_regen",
                                            "irons_spellbooks:cooldown_reduction",
                                            "irons_spellbooks:cast_time_reduction",
                                            "irons_spellbooks:spell_resist",
                                            "irons_spellbooks:summon_damage",
                                    };

                                    sb.append("§6--- Iron's Spells ---\n");
                                    for (String attrId : issAttrs) {
                                        appendAttrLine(sb, player, attrId);
                                    }

                                    String result = sb.toString();
                                    ctx.getSource().sendSuccess(() -> Component.literal(result), false);
                                    return 1;
                                })))
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

    private static void appendAttrLine(StringBuilder sb, ServerPlayer player, String attrId) {
        ResourceLocation loc = ResourceLocation.parse(attrId);
        var holderOpt = BuiltInRegistries.ATTRIBUTE.getHolder(
                net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ATTRIBUTE, loc)
        );
        if (holderOpt.isPresent()) {
            AttributeInstance inst = player.getAttribute(holderOpt.get());
            if (inst != null) {
                double base = inst.getBaseValue();
                double total = inst.getValue();
                sb.append(String.format("  §7%s§r: §fbase=%.2f §atotal=%.2f\n", loc, base, total));
            } else {
                sb.append(String.format("  §7%s§r: §c(not registered on entity)\n", loc));
            }
        } else {
            sb.append(String.format("  §7%s§r: §c(attribute not found in registry)\n", loc));
        }
    }
}
