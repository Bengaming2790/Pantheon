package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.AOEDamage;
import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.api.Dash;
import ca.techgarage.pantheon.api.DashState;
import eu.pb4.polymer.core.api.item.PolymerItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.util.Unit;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.Level;

import net.minecraft.world.phys.Vec3;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

import net.minecraft.core.particles.ParticleTypes;

import org.jspecify.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Predicate;

public class Khalkeus extends MaceItem implements PolymerItem {

    private static final String KH_SLAM_CD = "kh_slam_timer";
    private static final String AOE_CD = "khalkeus_aoe_cd";

    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath("pantheon", "khalkeus");

    public Khalkeus(Properties settings) {
        super(settings
                .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                .component(DataComponents.ATTRIBUTE_MODIFIERS, getDefaultAttributeModifiers())
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false)
                .component(DataComponents.MAX_STACK_SIZE, 1)
                .fireResistant()
        );
    }

    public static ItemAttributeModifiers getDefaultAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 7, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.0, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (!level.isClientSide()) {

            ItemStack stack = user.getItemInHand(hand);

            if (user.pick(2, 0, false).getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                return InteractionResult.PASS;
            }

            if (!user.isCreative()) {
                user.getCooldowns().addCooldown(stack, 200);
            }

            Dash.dashForward(user, 0.95f);
            DashState.start((ServerPlayer) user, 10, ParticleTypes.FLAME);

            level.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.PLAYERS,
                    1.5F, 1.75F);

            user.startAutoSpinAttack(10, 0f, user.getItemInHand(hand));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot equipmentSlot) {
        if (!(entity instanceof Player player)) return;

        if (stack.has(DataComponents.CUSTOM_NAME)) {
            player.displayClientMessage(Component.translatable("item.anvil.rename"), true);
            stack.remove(DataComponents.CUSTOM_NAME);
        }

        if (stack.has(DataComponents.ENCHANTMENTS)) {
            stack.remove(DataComponents.ENCHANTMENTS);
        }
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide()) return;

        target.igniteForTicks(4);

        if (attacker instanceof Player player) {
            if (!Cooldowns.isOnCooldown(player, AOE_CD)) {
                AOEDamage.applyAoeDamage(attacker, target, 6.0f, 2.5f, true, 4);
                Cooldowns.start(player, AOE_CD, 20 * 15);
            }
        }

        return;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pantheon.khalkeus");
    }

    private static void knockbackNearbyEntities(Level level, Entity attacker, Entity attacked) {
        level.levelEvent(2013, attacked.blockPosition(), 750);

        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                attacked.getBoundingBox().inflate(3.5))) {

            if (!getKnockbackPredicate(attacker, attacked).test(entity)) continue;

            Vec3 delta = entity.position().subtract(attacked.position());
            double strength = getKnockback(attacker, entity, delta);

            if (strength <= 0) continue;

            Vec3 motion = delta.normalize().scale(strength);
            entity.push(motion.x, 0.7F, motion.z);

            if (entity instanceof ServerPlayer sp) {
                sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
            }
        }
    }

    private static Predicate<LivingEntity> getKnockbackPredicate(Entity attacker, Entity attacked) {
        return entity ->
                !entity.isSpectator()
                        && entity != attacker
                        && entity != attacked
                        && !attacker.isAlliedTo(entity)
                        && attacked.distanceToSqr(entity) <= 3.5 * 3.5;
    }

    private static double getKnockback(Entity attacker, LivingEntity attacked, Vec3 distance) {
        return (3.5 - distance.length())
                * 0.7
                * (attacker.fallDistance > 5 ? 2 : 1)
                * (1.0 - attacked.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.DIAMOND;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }
}