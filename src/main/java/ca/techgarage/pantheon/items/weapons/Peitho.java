package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.api.Dash;
import ca.techgarage.pantheon.api.DashState;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class Peitho extends Item implements PolymerItem {
    public Peitho(Settings settings) {
        super(settings.component(DataComponentTypes.UNBREAKABLE,  Unit.INSTANCE).component(DataComponentTypes.MAX_STACK_SIZE, 1).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, getDefaultAttributeModifiers()));
    }
    private static final String PEITHO_25_CD = "peitho_25_cd";
    private static final Identifier MODEL =
            Identifier.of("pantheon", "peitho");
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            if(user.raycast(2, 0, false).getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                return ActionResult.PASS;
            }
            if (!(user.getGameMode() == GameMode.CREATIVE)) {
                user.getItemCooldownManager().set(stack, 300); //15 second cooldown
            }
            user.useRiptide(10, 0f, stack);
            Dash.dashForward(user, 0.75f);

            DashState.start((ServerPlayerEntity) user, 10, ParticleTypes.HEART);
            user.setStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1), user);
            user.getEntityWorld().playSound(
                    null,
                    user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_BREEZE_JUMP,
                    SoundCategory.PLAYERS,
                    1.0F, // volume
                    0.5F  // pitch
            );

        }
        return ActionResult.SUCCESS;
    }


    public static AttributeModifiersComponent getDefaultAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_SPEED_MODIFIER_ID,
                                2,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context) {
        out.remove(DataComponentTypes.CUSTOM_DATA);
    }
    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getEntityWorld().isClient()) return;
      if (attacker instanceof PlayerEntity player) {
          if (attacker.isSneaking() && !(Cooldowns.isOnCooldown(player,PEITHO_25_CD))) {
              double damageAmount = target.getMaxHealth() * 0.25;
              target.damage((ServerWorld) target.getEntityWorld(), target.getDamageSources().generic(), (float) damageAmount);
              player.getEntityWorld().playSound(
                      null,
                      player.getX(), player.getY(), player.getZ(),
                      SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                      SoundCategory.PLAYERS,
                      1.0F, // volume
                      0.3F  // pitch
              );
              if (player.getGameMode() != GameMode.CREATIVE) Cooldowns.start(player, PEITHO_25_CD, 20 * 45);
              return;
          }
      }
        double damageAmount = target.getMaxHealth() * 0.075;
        target.damage((ServerWorld) target.getEntityWorld(), target.getDamageSources().generic(), (float) damageAmount);
    }


    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        PlayerEntity player = (PlayerEntity) entity;
        if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            player.sendMessage(Text.translatable("item.anvil.rename").formatted(), true);
            stack.remove(DataComponentTypes.CUSTOM_NAME);
        }
        if (stack.contains(DataComponentTypes.ENCHANTMENTS)) {
            stack.remove(DataComponentTypes.ENCHANTMENTS);
        }
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.STICK;
    }
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.peitho");
    }
}
