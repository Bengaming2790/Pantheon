package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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

public class Uranicide extends Item implements PolymerItem {

    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("pantheon", "orcus");
    public static final String URANICIDE_L_C_FREEZE = "uranicideLCFreeze";
    public static final String URANICIDE_TITAN_DOMAIN = "uranicidetd";
    public static final String URANICIDE_TITAN_DOMAIN_ACTIVE = "uranicidetd_active";



    public Uranicide(Properties settings) {
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
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 13.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, 1.8-4,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }


    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(attacker instanceof Player player)) return;
        if (!(target instanceof Player)) return;
        if (!Cooldowns.isOnCooldown(player, URANICIDE_L_C_FREEZE)) {

            Level world = target.level();

            world.playSound(
                    null,
                    target.getX(), target.getY(), target.getZ(),
                    SoundEvents.NOTE_BLOCK_HARP,
                    SoundSource.PLAYERS,
                    1.0F, // volume
                    (float) (Math.random()*2) // pitch
            );
            ServerLevel serverWorld = (ServerLevel) world;
            serverWorld.sendParticles(
                    ParticleTypes.NOTE,
                    target.getX(),
                    target.getY() + 0.5,
                    target.getZ(),
                    80,
                    1,
                    1,
                    1,
                    0.0
            );
            target.addEffect(new MobEffectInstance(ModEffects.TIME_FROZEN, 2 * 20));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 2 * 20, 255,true, false));
            target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 2 * 20, 255,true, false));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 2 * 20, 255,true, false));
            Cooldowns.start(player, URANICIDE_L_C_FREEZE, 15 * 20, "Local Time Freeze");
        }
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide()) return InteractionResult.PASS;

        if (!Cooldowns.isOnCooldown(player,URANICIDE_TITAN_DOMAIN)) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.PLAYERS,
                    1.0f,
                    0f
            );

            world.getServer().tickRateManager().setTickRate(10);

            AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
            AttributeInstance gravity = player.getAttribute(Attributes.GRAVITY);
            AttributeInstance movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
            AttributeInstance jumpHeight = player.getAttribute(Attributes.JUMP_STRENGTH);

            if (attackSpeed != null) {
                attackSpeed.removeModifier(
                        Identifier.fromNamespaceAndPath("pantheon", "titan_domain_attack_speed")
                );

                attackSpeed.addTransientModifier(
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("pantheon", "titan_domain_attack_speed"),
                                4.0,
                                AttributeModifier.Operation.ADD_VALUE
                        )
                );
            }
            if (gravity != null) {
                gravity.addTransientModifier(
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("pantheon", "titan_domain_gravity"),
                                0.05,
                                AttributeModifier.Operation.ADD_VALUE
                        )
                );
            }
            if (movement != null) {
                movement.addTransientModifier(
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("pantheon", "titan_domain_speed"),
                                1.0,
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        )
                );
            }
            if (jumpHeight != null) {
                jumpHeight.addTransientModifier(
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("pantheon", "titan_domain_jump"),
                                0.1,
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        )
                );
            }
            Cooldowns.start(player, URANICIDE_TITAN_DOMAIN_ACTIVE, 10 * 20);
            Cooldowns.start(player, URANICIDE_TITAN_DOMAIN, 130 * 20, "Titan Domain");

        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {return MODEL;}

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pantheon.uranicide");
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.DIAMOND_HOE;
    }
}
