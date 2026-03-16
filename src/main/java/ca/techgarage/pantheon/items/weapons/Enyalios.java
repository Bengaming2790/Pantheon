package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.DamageSources.ModDamageSources;
import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.api.Dash;
import ca.techgarage.pantheon.api.DashState;
import ca.techgarage.pantheon.items.material.ModToolMaterials;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

public class Enyalios extends Item implements PolymerItem {

    private static final Random random = new Random();
    private static final Identifier MODEL =
            Identifier.of("pantheon", "enyalios");

    public static final String ENYALIOS_BLEED_ACTIVE = "enyalios_active_timer";
    public static final String ENYALIOS_BLEED_CD = "enyalios_bleed_cd";
    public Enyalios(Settings settings) {
        super(
                settings.spear(
                        ModToolMaterials.VARATHA_TOOL_MATERIAL,
                        1.05F,
                        1.5F,
                        0.0F,
                        3.0F,
                        0.1F,
                        3.0F,
                        0.1F,
                        3.0F,
                        0.1F
                ).component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, getDefaultAttributeModifiers()).fireproof()
                        .component(DataComponentTypes.TOOLTIP_DISPLAY, new TooltipDisplayComponent(false, new LinkedHashSet<>(List.of(
                                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                DataComponentTypes.UNBREAKABLE
                        ))))
        );
        applyEffects();

    }

    public static void registerHitCheck() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (entity, source, baseAmount, amount, blocked) -> {

                    if (!(entity instanceof ServerPlayerEntity player)) return;

                    if (!(player.getMainHandStack().getItem() instanceof Enyalios)) return;

                    if (source.getAttacker() == player) return;

                    onEnyaliosHit(player, source, amount);
                }
        );
    }
    public static void registerKillEffect() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {

            if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;

            // Only trigger if player is holding Enyalios
            if (!(player.getMainHandStack().getItem() instanceof Enyalios)) return;

            if (player == entity) return;

            applyKillBuff(player);
        });
    }
    private static void applyKillBuff(ServerPlayerEntity player) {
        player.addStatusEffect(
                new StatusEffectInstance(
                        StatusEffects.STRENGTH,
                        20 * 5, // 5 seconds
                        2,      // Strength III
                        true,
                        false,
                        true
                )
        );
    }

    private static void onEnyaliosHit(ServerPlayerEntity player, DamageSource source, float amount) {

        if (!Cooldowns.isOnCooldown(player, ENYALIOS_BLEED_CD)) {
            Cooldowns.start(player, ENYALIOS_BLEED_ACTIVE, 20 * 8);
        }
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player) {
            float damage = 31f;

            // Check for Strength effect
            if (attacker.hasStatusEffect(StatusEffects.STRENGTH)) {
                StatusEffectInstance effect = attacker.getStatusEffect(StatusEffects.STRENGTH);

                int level = effect.getAmplifier() + 1; // convert amplifier → level
                damage += level * 3f; // +3 damage per level
            }

            // Armor ignoring damage (magic)
            target.damage(
                    (ServerWorld) attacker.getEntityWorld(),
                    attacker.getDamageSources().playerAttack(player),
                    damage
            );

            if (Cooldowns.isOnCooldown(player, ENYALIOS_BLEED_ACTIVE)) {
                target.setStatusEffect(
                        new StatusEffectInstance(ModEffects.BLEED, 20 * 8, 2, true, false, false),
                        target
                );
                Cooldowns.clear(player, ENYALIOS_BLEED_ACTIVE);
                Cooldowns.start(player, ENYALIOS_BLEED_CD, 20 * 15);
            }

            if (Math.random() < 0.05) {
                target.setStatusEffect(
                        new StatusEffectInstance(ModEffects.BLEED, 20 * 8, 2, true, false, false),
                        target
                );
            }

        }

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
            user.damage(
                    (ServerWorld) user.getEntityWorld(),
                    user.getDamageSources().magic(),
                    (8f) * 1
            );

            Dash.dashForward(user, 1.5f);
            DashState.start((ServerPlayerEntity) user, 15, new DustParticleEffect(
                    16711680,
                    1.0F
            ));
            user.useRiptide(15, 40, stack);

        }
        return ActionResult.SUCCESS;
    }
    public static AttributeModifiersComponent getDefaultAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                8.0 - 1,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_SPEED_MODIFIER_ID,
                                -2.8,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }
    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.STICK;
    }

    public static void applyEffects(){
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (getHeldEnyalios(player) == null) continue;

                player.addStatusEffect(
                        new StatusEffectInstance(
                                StatusEffects.STRENGTH,
                                40,
                                1,
                                true,
                                false,
                                false
                        )
                );
            }
        });
    }

    private static ItemStack getHeldEnyalios(PlayerEntity player) {
        if (player.getMainHandStack().getItem() instanceof Enyalios)
            return player.getMainHandStack();
        return null;
    }
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }

}