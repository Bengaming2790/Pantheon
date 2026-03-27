package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.AOEDamage;
import ca.techgarage.pantheon.api.Cooldowns;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.List;

public class Glaciera extends Item implements PolymerItem {

    private static final String GLACIERA_COMBO_TIMER = "glaciera_hit_combo_timer";
    private static final String GLACIERA_COMBO_HITS = "glaciera_hit_combo_hits";
    private static final String FREEZE_LENGTH = "glaciera_freeze_length";

    public Glaciera(Properties settings) {
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
                        new AttributeModifier(
                                BASE_ATTACK_DAMAGE_ID,
                                17,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                BASE_ATTACK_SPEED_ID,
                                0.9 - 4.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide()) return;

        if (!(attacker instanceof Player player)) {
            applyFreeze(target);
            return;
        }

        if (!(stack.getItem() instanceof Glaciera)) return;

        if (!Cooldowns.isOnCooldown(player, GLACIERA_COMBO_TIMER)) {
            Cooldowns.start(player, GLACIERA_COMBO_TIMER, 20 * 15);
            Cooldowns.setInt(player, GLACIERA_COMBO_HITS, 1);
            return;
        }

        int hits = Cooldowns.getInt(player, GLACIERA_COMBO_HITS) + 1;
        Cooldowns.setInt(player, GLACIERA_COMBO_HITS, hits);

        if (hits >= 3) {
            applyFreeze(target);

            Cooldowns.clear(player, GLACIERA_COMBO_TIMER);
            Cooldowns.clear(player, GLACIERA_COMBO_HITS);
        }
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide()) {
            AOEDamage.applyAoeDamage(player, 8.0f, 3f, true);

            ServerLevel serverWorld = (ServerLevel) world;
            serverWorld.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    player.getX(),
                    player.getY() + 0.5,
                    player.getZ(),
                    160,
                    2,
                    1,
                    2,
                    0.0
            );

            if (!player.isCreative()) {
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 20 * 60);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static void registerFrostWalkerTrait() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {

                if (!(player.getMainHandItem().getItem() instanceof Glaciera))
                    continue;

                if (!player.onGround()) continue;
                if (player.getVehicle() != null) continue;

                ServerLevel world = player.level();
                int radius = 5;

                BlockPos center = player.getOnPos();

                for (BlockPos pos : BlockPos.betweenClosed(
                        center.offset(-radius, -1, -radius),
                        center.offset(radius, -1, radius)
                )) {
                    Vec3i iPos = new Vec3i((int) player.position().x, (int) player.position().y, (int) player.position().z);
                    if (!pos.closerThan(iPos, radius))
                        continue;

                    BlockState state = world.getBlockState(pos);
                    BlockState above = world.getBlockState(pos.above());

                    if (state.getBlock() == Blocks.WATER
                            && state.getFluidState().isSource()
                            && above.isAir()) {

                        world.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());

                        world.scheduleTick(
                                pos,
                                Blocks.FROSTED_ICE,
                                Mth.nextInt(player.getRandom(), 60, 120)
                        );
                    }
                }
            }
        });
    }

    public static void applyFreeze(LivingEntity target) {
        if (target.level().isClientSide()) return;

        int freezeTime = 20 * 40;

        target.setIsInPowderSnow(true);
        target.setTicksFrozen(freezeTime);
        target.addEffect(
                new MobEffectInstance(MobEffects.SLOWNESS, freezeTime, 1, false, true, false)
        );

        if (target instanceof Player player) {
            Cooldowns.start(player, FREEZE_LENGTH, freezeTime);
        }
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.STICK;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pantheon.glaceria");
    }
}