package ca.techgarage.pantheon;

import ca.techgarage.bscm.Bscm;
import ca.techgarage.pantheon.api.*;
import ca.techgarage.pantheon.blocks.ModAltarBlocks;
import ca.techgarage.pantheon.blocks.ModBlockEntities;
import ca.techgarage.pantheon.commands.*;
import ca.techgarage.pantheon.database.BanDatabase;
import ca.techgarage.pantheon.database.BankDatabase;
import ca.techgarage.pantheon.database.LegendaryDatabase;
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
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import net.minecraft.world.item.alchemy.Potions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.*;


public class Pantheon implements ModInitializer {
    public static final String MOD_ID = "pantheon";
    public static final boolean isPantheonSMP = true;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ArrayList<Player> playersAFK = new ArrayList<>();
    public static final long AFK_TIME = 20 * 5 * 1; // 5 minutes = 6000 ticks

    public static final java.util.Map<UUID, Vec3> lastPos = new java.util.HashMap<>();
    public static final java.util.Map<UUID, Integer> lastActiveTick = new java.util.HashMap<>();
    public static final java.util.Set<UUID> afkPlayers = new java.util.HashSet<>();
    public static final java.util.Map<UUID, Component> originalTabName = new java.util.HashMap<>();
    public static final Map<UUID, Float> lastYaw = new HashMap<>();
    public static final Map<UUID, Float> lastPitch = new HashMap<>();
    public static final Map<UUID, Vec3> lastInputPos = new HashMap<>();

    @Override
    public void onInitialize() {
        if (!isPantheonSMP) {
            LOGGER.error("[Pantheon] Mod failed to initialize: Not the official server.");
            return;
        }

        Bscm.load(PantheonConfig.class, "pantheon");
        ItemDenyList.init();



        ModItems.registerModItems();
        ModEffects.register();

        // Command Registration
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
            TempBanCommand.register(dispatcher);
            TempBanListCommand.register(dispatcher);
            TempBanRemoveCommand.register(dispatcher);
            ResetCooldownsCommand.register(dispatcher);
            PantheonCommand.register(dispatcher);
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

        ModAltarBlocks.register();
        ModBlockEntities.register();
        ModEntities.init();
        ItemFrameBlocker.register();
        InventoryBlocker.register();

        ServerTickEvents.END_SERVER_TICK.register(DashState::tick);
        PeithoTick.register();
        Glaciera.registerFrostWalkerTrait();
        IcarusWings.icarusFall();
        CombatLogAutoBan.register();
        Enyalios.registerKillEffect();
        Enyalios.registerDeathReset();
        Enyalios.registerHitCheck();
        PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

        Path db = FabricLoader.getInstance().getGameDir().resolve("database/bank.db");
        BankDatabase.init(db);

        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(
                Identifier.fromNamespaceAndPath(MOD_ID, "items"),
                PolymerCreativeModeTabUtils.builder()
                        .icon(() -> new ItemStack(ModItems.DRACHMA))
                        .title(Component.translatable("itemGroup.pantheon.items"))
                        .displayItems((_, entries) -> {
                            entries.accept(ModItems.DRACHMA);
                            entries.accept(ModItems.VARATHA);
                            entries.accept(ModItems.ASTRAPE);
                            entries.accept(ModItems.PEITHO);
                            entries.accept(ModItems.KHALKEOUS);
                            entries.accept(ModItems.AEGIS);
                            entries.accept(ModItems.ENYALIOS);
                            entries.accept(ModItems.TRIAINA);
                            entries.accept(ModItems.PHOEBUS);
                            entries.accept(ModItems.CADUCEUS);
                        }).build()
        );



        // Main Tick Logic
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                LegendaryDatabase.updateLegendaryOwnership(player);
                if (!player.hasEffect(ModEffects.TIME_FROZEN)) player.setNoGravity(false);
                if (player.swinging && player.swingTime == 1) {
                    ItemStack stack = player.getMainHandItem();
                    if (stack.getItem() instanceof Kynthia kynthia) {
                        kynthia.activate(player);
                    }
                }

                if (PantheonConfig.dropBannedItems) {
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (!stack.isEmpty() && ItemDenyList.isDenied(stack.getItem())) {
                            player.sendSystemMessage(Component.translatable("item.anvil.rename"), true);
                            player.getInventory().removeItemNoUpdate(i);
                            player.drop(stack, false, false);
                        }
                    }
                }

                if (Cooldowns.isOnCooldown(player, Uranicide.URANICIDE_TITAN_DOMAIN)) {
                    if (!Cooldowns.isOnCooldown(player, Uranicide.URANICIDE_TITAN_DOMAIN_ACTIVE)) {

                        player.level().getServer().tickRateManager().setTickRate(20);

                        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
                        AttributeInstance gravity = player.getAttribute(Attributes.GRAVITY);
                        AttributeInstance movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
                        AttributeInstance jumpHeight = player.getAttribute(Attributes.JUMP_STRENGTH);

                        if (attackSpeed != null) {
                            attackSpeed.removeModifier(
                                    Identifier.fromNamespaceAndPath("pantheon", "titan_domain_attack_speed")
                            );
                        }

                        if (gravity != null) {
                            gravity.removeModifier(
                                    Identifier.fromNamespaceAndPath("pantheon", "titan_domain_gravity")
                            );
                        }

                        if (movement != null) {
                            movement.removeModifier(
                                    Identifier.fromNamespaceAndPath("pantheon", "titan_domain_speed")
                            );
                        }
                        if (movement != null) {
                            movement.removeModifier(
                                    Identifier.fromNamespaceAndPath("pantheon", "titan_domain_jump")
                            );
                        }
                    }
                }
            }
            Caduceus.RandevuManager.tickAll();
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) {
                return;
            }

            for (ServerLevel world : server.getAllLevels()) {
                for (Villager villager : world.getEntities(
                        EntityType.VILLAGER,
                        EntitySelector.ENTITY_STILL_ALIVE)) {

                    villager.restock();
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int currentTick = server.getTickCount();

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();

                float yaw = player.getYRot();
                float pitch = player.getXRot();
                Vec3 pos = player.position();

                boolean cameraMoved = lastYaw.containsKey(uuid)
                        && (Math.abs(lastYaw.get(uuid) - yaw) > 0.01f
                        || Math.abs(lastPitch.get(uuid) - pitch) > 0.01f);

                Vec3 last = lastInputPos.get(uuid);

                boolean movedByInput = false;

                if (last != null) {
                    double dx = pos.x - last.x;
                    double dz = pos.z - last.z;

                    double dist = Math.sqrt(dx * dx + dz * dz);

                    movedByInput = dist > 0.01 && (player.zza != 0 || player.xxa != 0);
                }

                boolean active = cameraMoved || movedByInput;


                if (active) {
                    lastYaw.put(uuid, yaw);
                    lastPitch.put(uuid, pitch);
                    lastInputPos.put(uuid, pos);

                    lastActiveTick.put(uuid, currentTick);

                    if (afkPlayers.remove(uuid)) {
                        player.sendSystemMessage(Component.literal("§aYou are no longer AFK."));

                        // restore name safely
                        Component original = originalTabName.getOrDefault(uuid, player.getName());
                        player.setCustomName(null);
                    }
                }

                int lastTick = lastActiveTick.getOrDefault(uuid, currentTick);
                boolean shouldBeAFK = (currentTick - lastTick) >= AFK_TIME;

                if (shouldBeAFK && !afkPlayers.contains(uuid)) {
                    afkPlayers.add(uuid);

                    player.sendSystemMessage(Component.literal("§eYou are now AFK."));

                    Component baseName = player.getDisplayName();
                    if (baseName == null) baseName = player.getName();

                    player.setCustomName(
                            Component.literal(baseName.getString())
                                    .withStyle(ChatFormatting.GRAY)
                    );
                }
            }
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer player) {
                if (Pantheon.afkPlayers.contains(player.getUUID())) {
                    return false; // cancel ALL damage
                }
            }
            return true;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (!(entity instanceof Villager villager)) {
                return InteractionResult.PASS;
            }

            if (!player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }

            TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
            villager.saveWithoutId(output);
            CompoundTag entityTag = output.buildResult();

            entityTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(villager.getType()).toString());

            ItemStack egg = new ItemStack(Items.VILLAGER_SPAWN_EGG);
            egg.set(DataComponents.ENTITY_DATA, TypedEntityData.of(villager.getType(), entityTag));

            if (!player.getInventory().add(egg)) {
                player.drop(egg, false);
            }

            villager.discard();
            return InteractionResult.SUCCESS;
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, _) -> {
            if (entity instanceof ItemEntity itemEntity) {
                if (itemEntity.getItem().getItem() instanceof GlowItem) {
                    ModItems.applyGlowToAllDrops(itemEntity);
                }
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((_, pl, alive) -> Orcus.deactivateShadowstep(pl));
        ServerEntityEvents.ENTITY_UNLOAD.register((e, w) -> {
            if (e instanceof ServerPlayer p) Orcus.deactivateShadowstep(p);
        });
        AttackEntityCallback.EVENT.register((player, level, hand, _, _) -> {
            if (level.isClientSide()) return InteractionResult.PASS;
            if (!player.isShiftKeyDown()) return InteractionResult.PASS;

            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof Kynthia kynthia) {
                kynthia.activate(player);
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> {
            ServerPlayer player = handler.player;
            if (!BankDatabase.hasAccount(player.getUUID())) {
                BankDatabase.createAccount(player.getUUID(), PantheonConfig.StartingDrachma);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> {
            ServerPlayer player = handler.player;
            UUID uuid = player.getUUID();

            lastPos.put(uuid, player.position());
            lastActiveTick.put(uuid, player.level().getServer().getTickCount());
            lastYaw.put(uuid, player.getYRot());
            lastPitch.put(uuid, player.getXRot());
            lastInputPos.put(uuid, player.position());
            afkPlayers.remove(uuid);

            originalTabName.putIfAbsent(uuid, player.getTabListDisplayName());
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, _) -> {
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
            LegendaryDatabase.init(server);
            JoinListener.register();
        });
    }


    public static void log(String message) {
        LOGGER.info(message);
    }
}