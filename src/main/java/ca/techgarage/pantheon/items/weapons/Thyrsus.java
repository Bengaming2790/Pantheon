package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.api.HomingTracker;
import ca.techgarage.pantheon.entity.PartyFoulEntity;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.LinkedHashSet;
import java.util.List;

public class Thyrsus extends Item implements PolymerItem {
    public Thyrsus(Properties settings) {
        super(settings.component(DataComponents.UNBREAKABLE, Unit.INSTANCE).component(DataComponents.MAX_STACK_SIZE, 1).component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributeModifiers()).fireResistant()
                .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>(List.of(
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.UNBREAKABLE
                )))));
    }
    public static final String THYRUS_SNOWBALL_CD = "thyrsus_snowball_cd";

    public static ItemAttributeModifiers createAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                BASE_ATTACK_DAMAGE_ID,
                                7.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                BASE_ATTACK_SPEED_ID,
                                -2.2,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {

            if (player.isShiftKeyDown() && !Cooldowns.isOnCooldown(player, THYRUS_SNOWBALL_CD)) {
                player.addEffect(
                        new MobEffectInstance(MobEffects.STRENGTH, 30 * 20, 2, false, false, false)
                );
                player.addEffect(
                        new MobEffectInstance(MobEffects.REGENERATION, 30 * 20, 1, false, false, false)
                );

                level.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.HONEY_DRINK,
                        SoundSource.PLAYERS,
                        2.0f,
                        0.25F
                );

                if (!player.getAbilities().instabuild) {
                    Cooldowns.start(player, THYRUS_SNOWBALL_CD, 120 * 20);
                }

                return InteractionResult.SUCCESS;
            }

            Snowball snowball = new Snowball(level, player, player.getItemInHand(hand));
            snowball.setItem(new ItemStack(Items.SNOWBALL));

            snowball.setPos(
                    player.getX(),
                    player.getEyeY(),
                    player.getZ()
            );

            snowball.shootFromRotation(
                    player,
                    player.getXRot(), // pitch
                    player.getYRot(), // yaw
                    0f,
                    1.5f,
                    0f
            );

            level.addFreshEntity(snowball);

            HomingTracker.attach(snowball, player);

            if (!player.isCreative()) {
                player.getCooldowns().addCooldown(player.getActiveItem(), 20 * 10);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.STICK;
    }
}
