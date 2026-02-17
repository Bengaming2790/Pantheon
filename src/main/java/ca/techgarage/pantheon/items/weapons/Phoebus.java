package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class Phoebus extends Item implements PolymerItem {
    public Phoebus(Settings settings) {
        super(settings.component(DataComponentTypes.MAX_STACK_SIZE, 1).component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers()));
    }
    public static final String PHOEBUS_SONG_CD = "phoebus_song_cd";
    public static final String PHOEBUS_SONG_ACTIVE = "phoebus_song_active_timer";

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
                                1.6,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    @Override
    public void postHit(ItemStack stack, net.minecraft.entity.LivingEntity target, net.minecraft.entity.LivingEntity attacker) {
        super.postHit(stack, target, attacker);
        if (target.getEntityWorld().isClient()) {
            return;
        }

        target.setOnFireFor(5);
        target.setStatusEffect(
                new StatusEffectInstance(StatusEffects.GLOWING, 20 * 5, 1, true, true, false),
                target
        );

        if (attacker instanceof PlayerEntity player) {
            if (Cooldowns.isOnCooldown(player, PHOEBUS_SONG_ACTIVE)) {
                target.setStatusEffect(
                        new StatusEffectInstance(ModEffects.SUN_POISONING, 20 * 5, 1, true, true, true),
                        target
                );
            }
        }

    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            user.setStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20 * 10, 2), user);
             if (user.getGameMode() != GameMode.CREATIVE) {
                user.getItemCooldownManager().set(user.getStackInHand(hand), 20 * 45); //45 second cooldown
            }
            Cooldowns.start(user, PHOEBUS_SONG_ACTIVE, 20 * 15);

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

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.STICK;
    }
}
