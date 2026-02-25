package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IcarusWings extends Item implements PolymerItem {
    public IcarusWings(Settings settings) {
        super(settings.maxDamage(232).component(DataComponentTypes.GLIDER, Unit.INSTANCE).component(DataComponentTypes.MAX_STACK_SIZE, 1).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).equipSound(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA).model(EquipmentAssetKeys.ELYTRA).damageOnHurt(false).build()).repairable(Items.HONEYCOMB).fireproof());
    }
    private static final String ICARUS_BREAK_CD = "icarus_break_cd";
    private static final String ICARUS_RECHARGE_INTERVAL = "icarus_recharge_interval";
    private static final Set<UUID> rechargingPlayers = new HashSet<>();

    public static void icarusFall() {

        ServerTickEvents.END_SERVER_TICK.register(server -> {

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

                UUID uuid = player.getUuid();
                ServerWorld world = player.getEntityWorld();
                ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);

                if (!(chest.getItem() instanceof IcarusWings)) {
                    rechargingPlayers.remove(uuid);
                    continue;
                }

                boolean breakCooldown = Cooldowns.isOnCooldown(player, ICARUS_BREAK_CD);


                if (!breakCooldown && player.getY() > 384) {

                    world.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLOCK_DECORATED_POT_SHATTER,
                            SoundCategory.PLAYERS,
                            1.0F,
                            0F
                    );

                    Cooldowns.start(player, ICARUS_BREAK_CD, 20 * 30);
                    player.addStatusEffect(
                            new StatusEffectInstance(
                                    ModEffects.SUN_POISONING,
                                    20 * 5,
                                    1,
                                    true, true, true
                            )
                    );

                    chest.setDamage(chest.getMaxDamage());
                    rechargingPlayers.remove(uuid);
                    continue;
                }

                if (breakCooldown) {
                    rechargingPlayers.remove(uuid);
                    continue;
                }

                if (!chest.isDamaged()) {
                    rechargingPlayers.remove(uuid);
                    continue;
                }

                int maxDamage = chest.getMaxDamage();
                int currentDamage = chest.getDamage();

                if (currentDamage >= maxDamage - 15) {
                    rechargingPlayers.add(uuid);
                }

                if (!rechargingPlayers.contains(uuid))
                    continue;

                if (currentDamage <= 0) {
                    chest.setDamage(0);
                    rechargingPlayers.remove(uuid);
                    continue;
                }

                if (!Cooldowns.isOnCooldown(player, ICARUS_RECHARGE_INTERVAL)
                        && player.isOnGround()) {

                    chest.setDamage(Math.max(0, currentDamage - 5));
                    Cooldowns.start(player, ICARUS_RECHARGE_INTERVAL, 1);
                }
            }
        });
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.icurus_wings");
    }
    
    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.ELYTRA;
    }
}
