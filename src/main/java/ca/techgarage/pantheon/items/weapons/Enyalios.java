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

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.Level;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

public class Enyalios extends Item implements PolymerItem {

    private static final Random random = new Random();
    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath("pantheon", "enyalios");

    public static final String ENYALIOS_BLEED_ACTIVE = "enyalios_active_timer";
    public static final String ENYALIOS_BLEED_CD = "enyalios_bleed_cd";
    public Enyalios(Item.Properties settings) {
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
                ).component(DataComponents.UNBREAKABLE, Unit.INSTANCE).component(DataComponents.ATTRIBUTE_MODIFIERS, getDefaultAttributeModifiers()).fireResistant()
                        .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>(List.of(
                                DataComponents.ATTRIBUTE_MODIFIERS,
                                DataComponents.UNBREAKABLE
                        ))))
        );
        applyEffects();

    }

    public static void registerHitCheck() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (entity, source, baseAmount, amount, blocked) -> {

                    if (!(entity instanceof ServerPlayer player)) return;

                    if (!(player.getMainHandItem().getItem() instanceof Enyalios)) return;

                    if (source.getEntity() != player) return;

                    onEnyaliosHit(player, source, amount);
                }
        );
    }
    public static void registerKillEffect() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {

            if (!(source.getEntity() instanceof Player)) return;
            ServerPlayer player = (ServerPlayer) source.getEntity();
            // Only trigger if player is holding Enyalios
            if (!(player.getMainHandItem().getItem() instanceof Enyalios)) return;

            if (player == entity) return;

            applyKillBuff(player);
        });
    }
    private static void applyKillBuff(ServerPlayer player) {
        player.addEffect(
                new MobEffectInstance(
                        MobEffects.STRENGTH,
                        20 * 5, // 5 seconds
                        2,      // Strength III
                        true,
                        false,
                        true
                )
        );
    }

    private static void onEnyaliosHit(ServerPlayer player, DamageSource source, float amount) {

        if (!Cooldowns.isOnCooldown(player, ENYALIOS_BLEED_CD)) {
            Cooldowns.start(player, ENYALIOS_BLEED_ACTIVE, 20 * 8);
        }
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            float damage = 31f;

            // Check for Strength effect
            if (attacker.hasEffect(MobEffects.STRENGTH)) {
                MobEffectInstance effect = attacker.getEffect(MobEffects.STRENGTH);

                int level = effect.getAmplifier() + 1; // convert amplifier → level
                damage += level * 3f; // +3 damage per level
            }

            // Armor ignoring damage (magic)
            target.hurt(
                    attacker.damageSources().playerAttack(player),
                    damage
            );

            if (Cooldowns.isOnCooldown(player, ENYALIOS_BLEED_ACTIVE)) {
                target.addEffect(
                        new MobEffectInstance( ModEffects.BLEED, 20 * 8, 2, true, false, false),
                        target
                );
                Cooldowns.clear(player, ENYALIOS_BLEED_ACTIVE);
                Cooldowns.start(player, ENYALIOS_BLEED_CD, 20 * 15);
            }

            if (Math.random() < 0.05) {
                target.addEffect(
                        new MobEffectInstance((Holder<MobEffect>) ModEffects.BLEED, 20 * 8, 2, true, false, false),
                        target
                );
            }

        }

    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            ItemStack stack = user.getItemInHand(hand);
            if(user.pick(2, 0, false).getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                return InteractionResult.PASS;
            }
            if (!(user.gameMode().isCreative())) {
                user.getCooldowns().addCooldown(stack, 200); //10 second cooldown
            }
            user.hurt(
                    user.damageSources().magic(),
                    (8f) * 1
            );

            Dash.dashForward(user, 1.5f);
            DashState.start((ServerPlayer) user, 15, new DustParticleOptions(
                    16711680,
                    1.0F
            ));
            user.startAutoSpinAttack(15, 40, stack);

        }
        return InteractionResult.SUCCESS;
    }
    public static ItemAttributeModifiers getDefaultAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_ID,
                                8.0 - 1,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                BASE_ATTACK_SPEED_ID,
                                -2.8,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }
    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.STICK;
    }

    public static void applyEffects(){
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (getHeldEnyalios(player) == null) continue;

                player.addEffect(
                        new MobEffectInstance(
                                MobEffects.STRENGTH,
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

    private static ItemStack getHeldEnyalios(Player player) {
        if (player.getMainHandItem().getItem() instanceof Enyalios)
            return player.getMainHandItem();
        return null;
    }
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }

}