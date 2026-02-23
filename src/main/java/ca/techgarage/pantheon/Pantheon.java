package ca.techgarage.pantheon;

import ca.techgarage.pantheon.api.DashState;
import ca.techgarage.pantheon.api.PeithoTick;
import ca.techgarage.pantheon.bank.BankDatabase;
import ca.techgarage.pantheon.blocks.ModAltarBlocks;
import ca.techgarage.pantheon.blocks.ModBlockEntities;
import ca.techgarage.pantheon.items.DrachmaItem;
import ca.techgarage.pantheon.items.ModItems;
import ca.techgarage.pantheon.items.weapons.*;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.Identifier;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

import com.mojang.brigadier.arguments.IntegerArgumentType;


import java.util.Optional;
import java.util.UUID;

public class Pantheon implements ModInitializer {
    public static final String MOD_ID = "pantheon";
    public static final boolean isPantheonSMP = true;
    public Logger logger = LoggerFactory.getLogger(MOD_ID);
    public static PantheonConfig CONFIG;




    @Override
    public void onInitialize() {

        if (isPantheonSMP) {
            logger.info("[Pantheon] Mod Initialized");
        } else {
            logger.error("[Pantheon] Mod failed to initialize properly due to not being the official server.");
            return;
        }
        AutoConfig.register(PantheonConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(PantheonConfig.class).getConfig();


        ModItems.registerModItems();
        ModEffects.register();

//        ModAltarBlocks.register();
//        ModBlockEntities.register();

        logger.info("[Pantheon] Registered Assets");

        Optional<?> modContainer = FabricLoader.getInstance().getModContainer(MOD_ID);
        if (modContainer.isEmpty()) {
            logger.error("[Pantheon] Mod container '{}' not found. Check that `fabric.mod.json` contains the matching mod id.", MOD_ID);
            return;
        }

        ServerTickEvents.END_SERVER_TICK.register(DashState::tick);
        PeithoTick.register();
        Enyalios.registerKillEffect();
        Glaciera.registerFrostWalkerTrait();
        IcarusWings.icarusFall();
        PolymerResourcePackUtils.addModAssets(MOD_ID);

        PolymerResourcePackUtils.markAsRequired();
        BankDatabase.init(
                FabricLoader.getInstance()
                        .getGameDir()
                        .resolve("pantheon-bank.db")
                        .toString()
        );

        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MOD_ID, "items"), PolymerItemGroupUtils.builder()
                .icon(() -> new ItemStack(ModItems.DRACHMA))
                .displayName(Text.translatable("itemGroup.pantheon.items"))
                .entries((context, entries) -> {
                    entries.add(new ItemStack(ModItems.DRACHMA));
                    entries.add(new ItemStack(ModItems.VARATHA));
                    entries.add(new ItemStack(ModItems.ASTRAPE));
                    entries.add(new ItemStack(ModItems.PEITHO));
                    entries.add(new ItemStack(ModItems.KHALKEOUS));
                    entries.add(new ItemStack(ModItems.AEGIS));
                    entries.add(new ItemStack(ModItems.ENYALIOS));
                    entries.add(new ItemStack(ModItems.TRIAINA));
                }).build()
        );

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {


                if (!player.handSwinging) continue;
                if (player.handSwingTicks != 1) continue;

                ItemStack stack = player.getMainHandStack();

                if (!(stack.getItem() instanceof Kynthia kynthia)) continue;

                kynthia.activate(player);

            }

            Caduceus.RandevuManager.tickAll();


        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!player.isSneaking()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!(stack.getItem() instanceof Kynthia kynthia)) return ActionResult.PASS;

            kynthia.activate(player);

            return ActionResult.FAIL;
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            var uuid = player.getUuid();

            if (!BankDatabase.hasAccount(uuid)) {
                BankDatabase.createAccount(uuid, CONFIG.StartingDrachma);
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;

            UUID uuid = player.getUuid();

            int needed = CONFIG.DroppedDrachmaOnDeath;

            // Count drachma in inventory
            int invCount = DrachmaItem.countDrachma(player);

            int takenFromInv = Math.min(invCount, needed);
            int remaining = needed - takenFromInv;

            if (takenFromInv > 0) {
                DrachmaItem.removeDrachmaFromInventory(player, takenFromInv);
            }

            if (remaining > 0) {
                BankDatabase.remove(uuid, remaining);
            }
            if (BankDatabase.getBalance(player.getUuid()) < 0) {
                return;
            }
            // Drop total taken
            DrachmaItem.dropDrachma(player, takenFromInv + remaining);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(
                    literal("drachma")
                            .then(literal("balance")
                                    .executes(ctx -> {
                                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                                        int balance = BankDatabase.getBalance(player.getUuid());

                                        player.sendMessage(
                                                Text.translatable("command.pantheon.drachma.balance", balance),
                                                false
                                        );

                                        return 1;
                                    })
                            )

                            .then(literal("deposit")
                                    .then(argument("amount", IntegerArgumentType.integer(1))
                                            .executes(ctx -> {

                                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                                int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                                int invCount = DrachmaItem.countDrachma(player);

                                                if (invCount < amount) {
                                                    player.sendMessage(
                                                            Text.translatable("command.pantheon.drachma.not_enough_inv"),
                                                            false
                                                    );
                                                    return 0;
                                                }

                                                DrachmaItem.removeDrachmaFromInventory(player, amount);
                                                BankDatabase.add(player.getUuid(), amount);

                                                player.sendMessage(
                                                        Text.translatable("command.pantheon.drachma.deposit.success", amount),
                                                        false
                                                );

                                                return 1;
                                            })
                                    )
                            )

                            .then(literal("withdraw")
                                    .then(argument("amount", IntegerArgumentType.integer(1))
                                            .executes(ctx -> {

                                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                                int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                                int balance = BankDatabase.getBalance(player.getUuid());

                                                if (balance < amount) {
                                                    player.sendMessage(
                                                            Text.translatable("command.pantheon.drachma.not_enough_bank"),
                                                            false
                                                    );
                                                    return 0;
                                                }

                                                BankDatabase.remove(player.getUuid(), amount);
                                                player.getInventory().insertStack(
                                                        new ItemStack(ModItems.DRACHMA, amount)
                                                );

                                                player.sendMessage(
                                                        Text.translatable("command.pantheon.drachma.withdraw.success", amount),
                                                        false
                                                );

                                                return 1;
                                            })
                                    )
                            )
            );

        });


    }

    public static void log(String message) {
        Logger logger = LoggerFactory.getLogger(MOD_ID);
        logger.info(message);
    }
    public static PantheonConfig getConfig() {
        return CONFIG;
    }


}