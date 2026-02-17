package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.api.HomingTracker;
import ca.techgarage.pantheon.entity.PartyFoulEntity;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
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

public class Thyrsus extends Item implements PolymerItem {
    public Thyrsus(Settings settings) {
        super(settings.component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).component(DataComponentTypes.MAX_STACK_SIZE, 1).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers()));
    }
    public static final String THYRUS_SNOWBALL_CD = "thyrsus_snowball_cd";

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                7.0,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_SPEED_MODIFIER_ID,
                                1.8,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            if (user.isSneaking() && !Cooldowns.isOnCooldown(user, THYRUS_SNOWBALL_CD)) {
                user.setStatusEffect(
                        new StatusEffectInstance(StatusEffects.STRENGTH, 30 * 20, 2, false, false, false),
                        user
                );
                user.setStatusEffect(
                        new StatusEffectInstance(StatusEffects.REGENERATION, 30 * 20, 1, false, false, false),
                        user
                );
                world.playSound(
                        null,
                        user.getX(), user.getY(), user.getZ(),
                        SoundEvents.ITEM_HONEY_BOTTLE_DRINK,
                        SoundCategory.PLAYERS,
                        2.0f, // volume
                        0.25F  // pitch
                );
                if (!user.isCreative()) Cooldowns.start(user, THYRUS_SNOWBALL_CD, 120 * 20);
                return ActionResult.SUCCESS;
            }
            SnowballEntity snowball = new SnowballEntity(world, user, user.getStackInHand(hand));
            snowball.setItem(new ItemStack(Items.SNOWBALL));

            snowball.setPosition(
                    user.getX(),
                    user.getEyeY(),
                    user.getZ()
            );

            snowball.setVelocity(
                    user,
                    user.getPitch(),
                    user.getYaw(),
                    0f,
                    1.5f,
                    0f
            );

            world.spawnEntity(snowball);

            // Attach homing behavior
            HomingTracker.attach(snowball, user);

            // 10s cooldown
            if (!user.isCreative()) user.getItemCooldownManager().set(user.getStackInHand(hand), 20 * 10);
        }

        return ActionResult.SUCCESS;
    }






    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.STICK;
    }
}
