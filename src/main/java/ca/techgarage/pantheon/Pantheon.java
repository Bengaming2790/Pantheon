package ca.techgarage.pantheon;

import ca.techgarage.pantheon.api.*;
import ca.techgarage.pantheon.blocks.ModAltarBlocks;
import ca.techgarage.pantheon.blocks.ModBlockEntities;
import ca.techgarage.pantheon.commands.ResetCooldownsCommand;
import ca.techgarage.pantheon.commands.TempBanCommand;
import ca.techgarage.pantheon.commands.TempBanListCommand;
import ca.techgarage.pantheon.commands.TempBanRemoveCommand;
import ca.techgarage.pantheon.database.BanDatabase;
import ca.techgarage.pantheon.database.BankDatabase;
import ca.techgarage.pantheon.entity.ModEntities;
import ca.techgarage.pantheon.events.JoinListener;
import ca.techgarage.pantheon.items.DrachmaItem;
import ca.techgarage.pantheon.items.GlowItem;
import ca.techgarage.pantheon.items.ModItems;
import ca.techgarage.pantheon.items.weapons.*;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.*;


public class Pantheon implements ModInitializer {
    public static final String MOD_ID = "pantheon";
    public static final boolean isPantheonSMP = true;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        if (!isPantheonSMP) {
            LOGGER.error("[Pantheon] Mod failed to initialize: Not the official server.");
            return;
        }

        ConfigManager.load(PantheonConfig.class);
        ItemDenyList.deny(Items.TRIDENT);

        ModItems.registerModItems();
        ModEffects.register();

        // Command Registration
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TempBanCommand.register(dispatcher);
            TempBanListCommand.register(dispatcher);
            TempBanRemoveCommand.register(dispatcher);
            ResetCooldownsCommand.register(dispatcher);

            // Drachma Bank Command
            dispatcher.register(literal("drachma")
                    .then(literal("balance").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        int balance = BankDatabase.getBalance(player.getUUID());
                        player.sendSystemMessage(Component.translatable("command.pantheon.drachma.balance", balance));
                        return 1;
                    }))
                    .then(literal("deposit").then(argument("amount", integer(1)).executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        int amount = getInteger(ctx, "amount");
                        if (DrachmaItem.countDrachma(player) < amount) {
                            player.sendSystemMessage(Component.translatable("command.pantheon.drachma.not_enough_inv"));
                            return 0;
                        }
                        DrachmaItem.removeDrachmaFromInventory(player, amount);
                        BankDatabase.add(player.getUUID(), amount);
                        player.sendSystemMessage(Component.translatable("command.pantheon.drachma.deposit.success", amount));
                        return 1;
                    })))
                    .then(literal("withdraw").then(argument("amount", integer(1)).executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        int amount = getInteger(ctx, "amount");
                        int balance = BankDatabase.getBalance(player.getUUID());
                        if (balance < amount) {
                            player.sendSystemMessage(Component.translatable("command.pantheon.drachma.not_enough_bank"));
                            return 0;
                        }
                        BankDatabase.remove(player.getUUID(), amount);
                        player.getInventory().add(new ItemStack(ModItems.DRACHMA, amount));
                        player.sendSystemMessage(Component.translatable("command.pantheon.drachma.withdraw.success", amount));
                        return 1;
                    })))
            );
        });

//        ModAltarBlocks.register();
//        ModBlockEntities.register();
        ModEntities.init();
        ItemFrameBlocker.register();
        InventoryBlocker.register();

        ServerTickEvents.END_SERVER_TICK.register(DashState::tick);
        PeithoTick.register();
        Enyalios.registerKillEffect();
        Glaciera.registerFrostWalkerTrait();
        IcarusWings.icarusFall();
        CombatLogAutoBan.register();

        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

        Path db = FabricLoader.getInstance().getGameDir().resolve("database/bank.db");
        BankDatabase.init(db);

        // Polymer Creative Tab Registration
        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(
                Identifier.fromNamespaceAndPath(MOD_ID, "items"),
                PolymerCreativeModeTabUtils.builder()
                        .icon(() -> new ItemStack(ModItems.DRACHMA))
                        .title(Component.translatable("itemGroup.pantheon.items"))
                        .displayItems((context, entries) -> {
                            entries.accept(ModItems.DRACHMA);
                            entries.accept(ModItems.VARATHA);
                            entries.accept(ModItems.ASTRAPE);
                            entries.accept(ModItems.PEITHO);
                            entries.accept(ModItems.KHALKEOUS);
                            entries.accept(ModItems.AEGIS);
                            entries.accept(ModItems.ENYALIOS);
                            entries.accept(ModItems.TRIAINA);
                        }).build()
        );

        // Main Tick Logic
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                // Kynthia Activation Logic
                if (player.swinging && player.swingTime == 1) {
                    ItemStack stack = player.getMainHandItem();
                    if (stack.getItem() instanceof Kynthia kynthia) {
                        kynthia.activate(player);
                    }
                }

                // Item Denial / Banned Items Logic
                if (PantheonConfig.dropBannedItems && server.getTickCount() % 100 == 0) {
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (!stack.isEmpty() && ItemDenyList.isDenied(stack.getItem())) {
                            player.sendSystemMessage(Component.translatable("item.anvil.rename"), true);
                            player.getInventory().removeItemNoUpdate(i);
                            player.drop(stack, false, false);
                        }
                    }
                }
            }
            Caduceus.RandevuManager.tickAll();
        });

        // Entity Load Glow Event
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof ItemEntity itemEntity) {
                if (itemEntity.getItem().getItem() instanceof GlowItem) {
                    ModItems.applyGlowToAllDrops(itemEntity);
                }
            }
        });

        // Attack Event (Kynthia Sneak)
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide()) return InteractionResult.PASS;
            if (!player.isShiftKeyDown()) return InteractionResult.PASS;

            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof Kynthia kynthia) {
                kynthia.activate(player);
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        // Join Logic
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            if (!BankDatabase.hasAccount(player.getUUID())) {
                BankDatabase.createAccount(player.getUUID(), PantheonConfig.StartingDrachma);
            }
        });

        // Death Logic
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                UUID uuid = player.getUUID();
                int needed = PantheonConfig.DroppedDrachmaOnDeath;
                int invCount = DrachmaItem.countDrachma(player);
                int takenFromInv = Math.min(invCount, needed);
                int remaining = needed - takenFromInv;

                if (takenFromInv > 0) DrachmaItem.removeDrachmaFromInventory(player, takenFromInv);
                if (remaining > 0) BankDatabase.remove(uuid, remaining);

                if (BankDatabase.getBalance(uuid) >= 0) {
                    DrachmaItem.dropDrachma(player, takenFromInv + remaining);
                }
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            BanDatabase.init(server);
            JoinListener.register();
        });
    }

    public static void log(String message) {
        LOGGER.info(message);
    }
}