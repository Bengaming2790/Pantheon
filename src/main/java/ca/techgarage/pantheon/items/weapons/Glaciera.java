package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.AOEDamage;
import ca.techgarage.pantheon.api.Cooldowns;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class Glaciera extends Item implements PolymerItem {

    private static final String GLACIERA_COMBO_TIMER = "glaciera_hit_combo_timer";
    private static final String GLACIERA_COMBO_HITS = "glaciera_hit_combo_hits";
    private static final String FREEZE_LENGTH = "glaciera_freeze_length";

    public Glaciera(Settings settings) {
        super(settings
                .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
                .component(DataComponentTypes.MAX_STACK_SIZE, 1)
                .component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers()).fireproof()
        );
    }
    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                17,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                BASE_ATTACK_SPEED_MODIFIER_ID,
                                0.9 - 4.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }


    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getEntityWorld().isClient()) return;
        if (!(attacker instanceof PlayerEntity player)) {applyFreeze(target); return;}
        if (!(stack.getItem() instanceof Glaciera)) return;
        // First hit → start combo
        if (!Cooldowns.isOnCooldown(player, GLACIERA_COMBO_TIMER)) {
            Cooldowns.start(player, GLACIERA_COMBO_TIMER, 20 * 15); // 10s window
            Cooldowns.setInt(player, GLACIERA_COMBO_HITS, 1);
            return;
        }

        // Combo already active
        int hits = Cooldowns.getInt(player, GLACIERA_COMBO_HITS) + 1;
        Cooldowns.setInt(player, GLACIERA_COMBO_HITS, hits);

        // Third hit → Freeze
        if (hits >= 3) {
            applyFreeze(target);

            // Reset combo
            Cooldowns.clear(player, GLACIERA_COMBO_TIMER);
            Cooldowns.clear(player, GLACIERA_COMBO_HITS);
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient()) {
            AOEDamage.applyAoeDamage(player, 8.0f, 3f, true);
            ServerWorld serverworld = (ServerWorld) world;
            serverworld.spawnParticles(
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
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.getItemCooldownManager().set(player.getStackInHand(hand), 20 * 60); //60 second cooldown
            }

        }
        return ActionResult.SUCCESS;
    }

    public static void registerFrostWalkerTrait() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

                if (!(player.getMainHandStack().getItem() instanceof Glaciera))
                    continue;

                if (!player.isOnGround()) continue;
                if (player.hasVehicle()) continue;

                ServerWorld world = player.getEntityWorld();
                int radius = 5;

                BlockPos center = player.getBlockPos();

                for (BlockPos pos : BlockPos.iterate(
                        center.add(-radius, -1, -radius),
                        center.add(radius, -1, radius)
                )) {

                    if (!pos.isWithinDistance(player.getEntityPos(), radius))
                        continue;

                    BlockState state = world.getBlockState(pos);
                    BlockState above = world.getBlockState(pos.up());

                    if (state.getBlock() == Blocks.WATER
                            && state.getFluidState().isStill()
                            && above.isAir()) {

                        world.setBlockState(pos, Blocks.FROSTED_ICE.getDefaultState());

                        world.scheduleBlockTick(
                                pos,
                                Blocks.FROSTED_ICE,
                                MathHelper.nextInt(player.getRandom(), 60, 120)
                        );
                    }
                }
            }
        });
    }

    public static void applyFreeze(LivingEntity target) {
        if (target.getEntityWorld().isClient()) return;

        int freezeTime = 20 * 40; // 40 seconds

        target.setInPowderSnow(true);
        target.setFrozenTicks(freezeTime);
        target.addStatusEffect(
                new StatusEffectInstance(StatusEffects.SLOWNESS, freezeTime, 1, false, true, false)
        );

        // Only players should use Cooldowns system
        if (target instanceof PlayerEntity player) {
            Cooldowns.start(player, FREEZE_LENGTH, freezeTime);
        }
    }
    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.STICK;
    }
}
