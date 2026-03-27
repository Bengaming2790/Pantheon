package ca.techgarage.pantheon.items.weapons;


import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.api.Dash;
import ca.techgarage.pantheon.api.DashState;
import ca.techgarage.pantheon.items.GlowItem;
import eu.pb4.polymer.core.api.item.PolymerItem;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import java.util.LinkedHashSet;
import java.util.List;

public class Peitho extends Item implements PolymerItem, GlowItem {
    public Peitho(Properties settings) {
        super(settings.component(DataComponents.UNBREAKABLE,  Unit.INSTANCE)
                .component(DataComponents.MAX_STACK_SIZE, 1)
                .component(DataComponents.ATTRIBUTE_MODIFIERS, getDefaultAttributeModifiers())
                .component(DataComponents.LORE, lore).fireResistant().component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>(List.of(
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.UNBREAKABLE
                ))))
        );
    }
    private static final String PEITHO_25_CD = "peitho_25_cd";
    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath("pantheon", "peitho");

    private static ItemLore lore = new ItemLore(List.of(
            Component.literal("A Dagger Wielded By ")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.LIGHT_PURPLE).withBold(false))
                    .append(Component.literal("Aphrodite")
                            .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.LIGHT_PURPLE).withBold(true))),
            Component.literal("Heartbreak")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GOLD).withBold(true)),
            Component.literal("   Deal 7.5% instead of traditional damage")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)),
            Component.literal("Sorrowful Rose")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GOLD).withBold(true))
                    .append(Component.literal(" - Crouch + Left Click")
                            .setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY).withBold(false))),
            Component.literal("   Launch a powerful attack forward that deals")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)),
            Component.literal("   35% of Current Health of those hit")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)),

            Component.literal("Lovestuck Lunge")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GOLD).withBold(true))
                    .append(Component.literal(" - Right Click")
                            .setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY).withBold(false))),
            Component.literal("   Charge forward and gain Regeneration")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)),
            Component.literal("   Cooldown: 10s")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)),
            Component.literal("Love's Favor")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GOLD).withBold(true)),
            Component.literal("   Grants Health Boost")
                    .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY))
    ));

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide()) {
            ItemStack stack = user.getItemInHand(hand);
            if(user.pick(2, 0, false).getType() ==net.minecraft.world.phys.HitResult.Type.BLOCK) {
                return InteractionResult.PASS;
            }
            if (!(user.gameMode().isCreative())) {
                user.getCooldowns().addCooldown(stack, 20 * 15);
            }
            Dash.dashForward(user, 0.75f);

            DashState.start((ServerPlayer) user, 10, ParticleTypes.HEART);
            user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1), user);
            user.level().playSound(
                    null,
                    user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BREEZE_JUMP,
                    SoundSource.PLAYERS,
                    1.0F, // volume
                    0.5F  // pitch
            );
            user.startAutoSpinAttack(10, 0f, stack);

        }
        return InteractionResult.SUCCESS;
    }


    public static ItemAttributeModifiers getDefaultAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                Item.BASE_ATTACK_SPEED_ID,
                                2 - 4,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        out.remove(DataComponents.CUSTOM_DATA);
    }
    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide()) return;
      if (attacker instanceof Player player) {
          if (attacker.isShiftKeyDown() && !(Cooldowns.isOnCooldown(player,PEITHO_25_CD))) {
              double damageAmount = target.getMaxHealth() * 0.25;
              target.hurt( target.damageSources().generic(), (float) damageAmount);
              player.level().playSound(
                      null,
                      player.getX(), player.getY(), player.getZ(),
                      SoundEvents.PLAYER_ATTACK_CRIT,
                      SoundSource.PLAYERS,
                      1.0F, // volume
                      0.3F  // pitch
              );
              if (!player.gameMode().isCreative()) Cooldowns.start(player, PEITHO_25_CD, 20 * 45);
              return;
          }
      }
        double damageAmount = target.getMaxHealth() * 0.075;
        target.hurt(target.damageSources().generic(), (float) damageAmount);
    }


    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        Player player = (Player) entity;
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            player.sendSystemMessage(Component.translatable("item.anvil.rename"));
            stack.remove(DataComponents.CUSTOM_NAME);
        }
        if (stack.has(DataComponents.ENCHANTMENTS)) {
            stack.remove(DataComponents.ENCHANTMENTS);
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
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pantheon.peitho");
    }

    @Override
    public String getGlowColor() {
        return "#AA0000";
    }
}
