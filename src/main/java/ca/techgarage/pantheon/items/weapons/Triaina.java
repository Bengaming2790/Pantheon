package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.api.Dash;
import ca.techgarage.pantheon.api.DashState;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class Triaina extends Item implements PolymerItem {
    public Triaina(Settings settings) {
        super(settings.component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).component(DataComponentTypes.MAX_STACK_SIZE, 1).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers()));
        applyEffects();
    }

    private static final String TRIAINA_COMBO_TIMER = "triaina_hit_combo_timer";
    private static final String TRIAINA_COMBO_HITS = "triaina_hit_combo_hits";

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                14.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                BASE_ATTACK_SPEED_MODIFIER_ID,
                                1.4,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            if(user.raycast(2, 0, false).getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                return ActionResult.PASS;
            }
            if (!(user.getGameMode() == GameMode.CREATIVE)) {
                user.getItemCooldownManager().set(stack, 200); //10 second cooldown
            }
            user.useRiptide(10, 5.0f, stack);
            Dash.dashForward(user, 0.75f);
            DashState.start((ServerPlayerEntity) user, 10, ParticleTypes.FALLING_WATER);

            world.playSound(
                    null,
                    user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_CONDUIT_ACTIVATE,
                    SoundCategory.PLAYERS,
                    1.0F, // volume
                    1.0F  // pitch
            );
        }
        return ActionResult.SUCCESS;
    }
    private static ItemStack getHeldEnyalios(PlayerEntity player) {
        if (player.getMainHandStack().getItem() instanceof Triaina)
            return player.getMainHandStack();
        return null;
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getEntityWorld().isClient()) return;
        if (!(attacker instanceof PlayerEntity player)) return;
        if (!(stack.getItem() instanceof Triaina)) return;

        // First hit → start combo
        if (!Cooldowns.isOnCooldown(player, TRIAINA_COMBO_TIMER)) {
            Cooldowns.start(player, TRIAINA_COMBO_TIMER, 20 * 10); // 10s window
            Cooldowns.setInt(player, TRIAINA_COMBO_HITS, 1);
            return;
        }

        // Combo already active
        int hits = Cooldowns.getInt(player, TRIAINA_COMBO_HITS) + 1;
        Cooldowns.setInt(player, TRIAINA_COMBO_HITS, hits);

        // Third hit → knockback
        if (hits >= 3) {
            applyComboKnockback(player, target);

            // Reset combo
            Cooldowns.clear(player, TRIAINA_COMBO_TIMER);
            Cooldowns.clear(player, TRIAINA_COMBO_HITS);
        }
    }
    private static void applyComboKnockback(PlayerEntity player, LivingEntity target) {
        Vec3d direction = target.getEntityPos()
                .subtract(player.getEntityPos())
                .normalize();

        Vec3d velocity = new Vec3d(
                direction.x * 1.2,
                0.45,
                direction.z * 1.2
        );

        target.setVelocity(velocity);

        if (target instanceof ServerPlayerEntity serverTarget) {
            serverTarget.networkHandler.sendPacket(
                    new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(serverTarget)
            );
        }
    }


    public static void applyEffects(){
        // Sea God's Boon
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (getHeldEnyalios(player) == null) continue;

                player.addStatusEffect(
                        new StatusEffectInstance(
                                StatusEffects.DOLPHINS_GRACE,
                                40,
                                2,
                                true,
                                false,
                                false
                        )
                );
                player.addStatusEffect(
                        new StatusEffectInstance(
                                StatusEffects.WATER_BREATHING,
                                40,
                                2,
                                true,
                                false,
                                false
                        )
                );
            }
        });
    }
    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.triaina");
    }
    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.STICK;
    }

}
