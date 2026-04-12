package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItem;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.LinkedHashSet;
import java.util.List;

public class Phoebus extends Item implements PolymerItem {
    public Phoebus(net.minecraft.world.item.Item.Properties settings) {
        super(settings.component(DataComponents.MAX_STACK_SIZE, 1).component(DataComponents.UNBREAKABLE, Unit.INSTANCE).component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributeModifiers()).fireResistant()
                .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>(List.of(
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.UNBREAKABLE
                )))));
    }

    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath("pantheon", "phoebus");

    public static final String PHOEBUS_SONG_CD = "phoebus_song_cd";
    public static final String PHOEBUS_SONG_ACTIVE = "phoebus_song_active_timer";

    public static ItemAttributeModifiers createAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                BASE_ATTACK_DAMAGE_ID,
                                14.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                BASE_ATTACK_SPEED_ID,
                                1.6 - 4.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (target.level().isClientSide()) {
            return;
        }

        target.igniteForTicks(5);
        target.addEffect(
                new MobEffectInstance(MobEffects.GLOWING, 20 * 5, 1, true, true, false),
                target
        );

        if (attacker instanceof Player player) {
            if (Cooldowns.isOnCooldown(player, PHOEBUS_SONG_ACTIVE)) {
                target.addEffect(
                        new MobEffectInstance(ModEffects.SUN_POISONING, 20 * 5, 1, true, true, true),
                        target
                );
            }
        }

    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            user.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 10, 0), user);
             if (user.gameMode() != GameType.CREATIVE) {
                user.getCooldowns().addCooldown(user.getActiveItem(), 20 * 45); //45 second cooldown
            }
            Cooldowns.start(user, PHOEBUS_SONG_ACTIVE, 20 * 15);

            world.playSound(
                    null,
                    user.getX(), user.getY(), user.getZ(),
                    SoundEvents.CONDUIT_ACTIVATE,
                    SoundSource.PLAYERS,
                    1.0F, // volume
                    1.0F  // pitch
            );
            ServerLevel serverWorld = (ServerLevel) world;
            serverWorld.sendParticles(
                    ParticleTypes.NOTE,
                    user.getX(),
                    user.getY() + 0.5,
                    user.getZ(),
                    80,
                    1,
                    1,
                    1,
                    0.0
            );

        }
        return InteractionResult.SUCCESS;
    }
    @Override
    public Identifier getPolymerItemModel(ItemStack itemStack, PacketContext context, HolderLookup.Provider lookup) {
        return MODEL;
    }
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pantheon.pheobus");
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.STICK;
    }
}
