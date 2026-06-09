package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.Pantheon;
import ca.techgarage.pantheon.api.Cooldowns;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.*;

public class Orcus extends Item implements PolymerItem {

    private static final Set<UUID> ACTIVE_SHADOWSTEP = ConcurrentHashMap.newKeySet();

    private static final int SHADOWSTEP_DURATION_TICKS = 5 * 20;
    private static final String SHADOWSTEP_KEY = "shadowstep";
    private static final String SHADOWSTEP_CD =  "sSCD";
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("pantheon", "orcus");
    private static final String DEATHSFOIL_CD = "deathsfoil_cd";
    private static final int DEATHSFOIL_CD_TICKS = 20 * 20;
    private static final float SWEEP_RADIUS = 4.0f;
    private static final float PULL_STRENGTH = 0.6f;

    public Orcus(Properties settings) {
        super(settings
                .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                .component(DataComponents.MAX_STACK_SIZE, 1)
                .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributeModifiers())
                .fireResistant()
                .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>(List.of(
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.UNBREAKABLE
                ))))
        );
    }

    public static ItemAttributeModifiers createAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 18.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.COPPER_SWORD;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {return MODEL;}

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof ServerPlayer player)) return;
        if (isInShadowstep(player)) deactivateShadowstep(player);
        performSweep(player, target);

        if (!Cooldowns.isOnCooldown(player, DEATHSFOIL_CD)) {
            applyDeathsFoil(player, target);
            Cooldowns.start(player, DEATHSFOIL_CD, DEATHSFOIL_CD_TICKS, "Death's Foil");
        }

    }

    private void performSweep(ServerPlayer player, LivingEntity primaryTarget) {
        Level world = player.level();

        world.getEntitiesOfClass(LivingEntity.class,
                primaryTarget.getBoundingBox().inflate(SWEEP_RADIUS),
                e -> e != player && e != primaryTarget
        ).forEach(nearby -> {
            nearby.hurt(player.damageSources().playerAttack(player), 4.0f);

            Vec3 toPlayer = player.position().subtract(nearby.position()).normalize();
            nearby.setDeltaMovement(
                    toPlayer.x * PULL_STRENGTH,
                    0.2,
                    toPlayer.z * PULL_STRENGTH
            );
            nearby.hurtMarked = true;
        });

        Vec3 toPlayer = player.position().subtract(primaryTarget.position()).normalize();
        primaryTarget.setDeltaMovement(
                toPlayer.x * PULL_STRENGTH,
                0.2,
                toPlayer.z * PULL_STRENGTH
        );
        primaryTarget.hurtMarked = true;
    }

    private void applyDeathsFoil(ServerPlayer player, LivingEntity target) {
        target.addEffect(new MobEffectInstance(
                MobEffects.WITHER,
                3 * 20,
                1,
                false,
                true,
                true
        ));

        target.hurt(player.damageSources().playerAttack(player), 3.0f);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!(user instanceof ServerPlayer player))
            return InteractionResult.PASS;


        if (isInShadowstep(player) || Cooldowns.isOnCooldown(player, SHADOWSTEP_CD)) {
            player.sendSystemMessage(Component.literal("Shadowstep is on Cooldown"), true);
            return InteractionResult.FAIL;
        }
        activateShadowstep(player);
        Cooldowns.start(player, SHADOWSTEP_CD, 25 * 20, "Shdaowstep");
        return InteractionResult.SUCCESS;
    }

    public static boolean isInShadowstep(ServerPlayer player) {
        return Cooldowns.isOnCooldown(player, SHADOWSTEP_KEY);
    }

    public static void activateShadowstep(ServerPlayer player) {
        if (isInShadowstep(player)) return;

        Cooldowns.start(player, SHADOWSTEP_KEY, SHADOWSTEP_DURATION_TICKS);
        ACTIVE_SHADOWSTEP.add(player.getUUID());
        player.setInvisible(true);
        player.addEffect(new MobEffectInstance(
                MobEffects.SPEED,
                SHADOWSTEP_DURATION_TICKS + 10,
                0,
                false,
                false,
                true
        ));

        broadcastInvisibleFlag(player, true);
        broadcastEmptyEquipment(player);
    }

    public static void deactivateShadowstep(ServerPlayer player) {
        if (!ACTIVE_SHADOWSTEP.remove(player.getUUID())) return;

        Cooldowns.clear(player, SHADOWSTEP_KEY);
        player.removeEffect(MobEffects.SPEED);
        player.setInvisible(false);
        broadcastInvisibleFlag(player, false);
        broadcastRealEquipment(player);
    }

   // Packet Helpers

    private static void broadcastInvisibleFlag(ServerPlayer target, boolean invisible) {
        byte currentFlags = target.getEntityData().get(
                new EntityDataAccessor<>(0, EntityDataSerializers.BYTE)
        );

        if (invisible) {
            currentFlags |= 0x20;
        } else {
            currentFlags &= ~0x20;
        }

        final byte flags = currentFlags;
        List<SynchedEntityData.DataValue<?>> values = List.of(
                SynchedEntityData.DataValue.create(
                        new EntityDataAccessor<>(0, EntityDataSerializers.BYTE),
                        flags
                )
        );

        ClientboundSetEntityDataPacket packet =
                new ClientboundSetEntityDataPacket(target.getId(), values);

        sendToAllNearbyPlayers(target, packet);
    }

    private static void broadcastEmptyEquipment(ServerPlayer target) {
        List<Pair<EquipmentSlot, ItemStack>> emptySlots = Arrays.stream(EquipmentSlot.values())
                .map(slot -> Pair.of(slot, ItemStack.EMPTY))
                .toList();

        ClientboundSetEquipmentPacket packet =
                new ClientboundSetEquipmentPacket(target.getId(), emptySlots);

        sendToOtherNearbyPlayers(target, packet);
    }

    private static void broadcastRealEquipment(ServerPlayer target) {
        List<Pair<EquipmentSlot, ItemStack>> realSlots = Arrays.stream(EquipmentSlot.values())
                .map(slot -> Pair.of(slot, target.getItemBySlot(slot)))
                .toList();

        ClientboundSetEquipmentPacket packet =
                new ClientboundSetEquipmentPacket(target.getId(), realSlots);

        sendToOtherNearbyPlayers(target, packet);
    }
    /** Sends a packet to the target player AND all nearby others. */
    private static void sendToAllNearbyPlayers(ServerPlayer target,
                                               net.minecraft.network.protocol.Packet<?> packet) {
        target.level().players().forEach(other -> {
            ((ServerPlayer) other).connection.send(packet);
        });
    }

    /** Sends a packet to all nearby players EXCEPT the target themselves. */
    private static void sendToOtherNearbyPlayers(ServerPlayer target,
                                                 net.minecraft.network.protocol.Packet<?> packet) {
        target.level().players().forEach(other -> {
            if (!other.getUUID().equals(target.getUUID())) {
                ((ServerPlayer) other).connection.send(packet);
            }
        });
    }
}