package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.Equippable;

import net.minecraft.world.effect.MobEffectInstance;

import java.util.*;

public class IcarusWings extends Item implements PolymerItem {

    private static final String ICARUS_BREAK_CD = "icarus_break_cd";
    private static final String ICARUS_RECHARGE_INTERVAL = "icarus_recharge_interval";
    private static final Set<UUID> rechargingPlayers = new HashSet<>();

    public IcarusWings(Properties settings) {
        super(settings
                .durability(232)
                .component(DataComponents.GLIDER, Unit.INSTANCE)
                .component(DataComponents.MAX_STACK_SIZE, 1)
                .component(DataComponents.EQUIPPABLE,
                        Equippable.builder(EquipmentSlot.CHEST)
                                .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
                                .build()
                )
                .repairable(Items.HONEYCOMB)
                .component(DataComponents.TOOLTIP_DISPLAY,
                        new TooltipDisplay(false, new LinkedHashSet<>(List.of(
                                DataComponents.ATTRIBUTE_MODIFIERS,
                                DataComponents.UNBREAKABLE
                        )))
                )
                .fireResistant()
        );
    }

    public static void icarusFall() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {

                UUID uuid = player.getUUID();
                ServerLevel world = player.level();
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

                if (!(chest.getItem() instanceof IcarusWings)) {
                    rechargingPlayers.remove(uuid);
                    continue;
                }

                boolean breakCooldown = Cooldowns.isOnCooldown(player, ICARUS_BREAK_CD);

                if (!breakCooldown && player.getY() > 384) {

                    world.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.DECORATED_POT_SHATTER,
                            SoundSource.PLAYERS,
                            1.0F,
                            0F
                    );

                    Cooldowns.start(player, ICARUS_BREAK_CD, 20 * 30);

                    player.addEffect(
                            new MobEffectInstance(
                                    (Holder<MobEffect>) ModEffects.SUN_POISONING,
                                    20 * 5,
                                    1,
                                    true, true, true
                            )
                    );

                    chest.setDamageValue(chest.getMaxDamage());
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
                int currentDamage = chest.getDamageValue();

                if (currentDamage >= maxDamage - 15) {
                    rechargingPlayers.add(uuid);
                }

                if (!rechargingPlayers.contains(uuid))
                    continue;

                if (currentDamage <= 0) {
                    chest.setDamageValue(0);
                    rechargingPlayers.remove(uuid);
                    continue;
                }

                if (!Cooldowns.isOnCooldown(player, ICARUS_RECHARGE_INTERVAL)
                        && player.onGround()) {

                    chest.setDamageValue(Math.max(0, currentDamage - 5));
                    Cooldowns.start(player, ICARUS_RECHARGE_INTERVAL, 1);
                }
            }
        });
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pantheon.icurus_wings");
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.ELYTRA;
    }
}