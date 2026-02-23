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

public class IcarusWings extends Item implements PolymerItem {
    public IcarusWings(Settings settings) {
        super(settings.maxDamage(232).component(DataComponentTypes.GLIDER, Unit.INSTANCE).component(DataComponentTypes.MAX_STACK_SIZE, 1).component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST).equipSound(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA).model(EquipmentAssetKeys.ELYTRA).damageOnHurt(false).build()).repairable(Items.HONEYCOMB).fireproof());
    }
    private static final String ICARUS_BREAK_CD = "icarus_break_cd";
    public static void icarusFall() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerWorld world = player.getEntityWorld();
                ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);

                if (!(chest.getItem() instanceof IcarusWings))
                    continue;

                boolean onCooldown = Cooldowns.isOnCooldown(player, ICARUS_BREAK_CD);

                if (!onCooldown && chest.isDamaged()) {
                    chest.setDamage(0);
                    world.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLOCK_BEACON_POWER_SELECT,
                            SoundCategory.PLAYERS,
                            1.0F, // volume
                            2.0F  // pitch
                    );
                }
                if (onCooldown)
                    continue;

                if (player.getY() > 384) {
                    world.playSound(
                            null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLOCK_DECORATED_POT_SHATTER,
                            SoundCategory.PLAYERS,
                            1.0F, // volume
                            0F  // pitch
                    );
                    Cooldowns.start(player, ICARUS_BREAK_CD, 20 * 30); // 30 seconds
                    player.addStatusEffect(new StatusEffectInstance(ModEffects.SUN_POISONING, 20 * 5, 1, true, true, true));
                    chest.setDamage(chest.getMaxDamage());
                }
            }
        });
    }
    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.ELYTRA;
    }
}
