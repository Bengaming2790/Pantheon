package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.Pantheon;
import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.entity.AstrapeEntity;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.LinkedHashSet;
import java.util.List;

public class Astrape extends TridentItem implements PolymerItem {

    public Astrape(Item.Properties settings) {
        super(settings.component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                .component(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                        List.of(1f),
                        List.of(),
                        List.of(),
                        List.of()
                ))
                .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributeModifiers())
                .component(DataComponents.MAX_STACK_SIZE, 1).fireResistant()
                .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>(List.of(
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.UNBREAKABLE
                )))));
    }

    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath("pantheon", "astrape");
    private static final String ASTRAPE_CD = "astrape_lightning_cd";
    private static final String ASTRAPE_THROW_CD = "astrape_throw_cd";

    public static ItemAttributeModifiers createAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                BASE_ATTACK_DAMAGE_ID,
                                9.0,
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
                .add(
                        Attributes.SAFE_FALL_DISTANCE,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("pantheon", "astrape_safe_fall_distance"),
                                1024.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.ANY
                )
                .build();
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide()) return;
        if (!(attacker instanceof Player player)) return;

        double randomConduct = Math.random();

        if (randomConduct <= 0.25) {
            for (int i = 0; i < 5; i++) {
                LightningBolt lightning =
                        new LightningBolt(EntityType.LIGHTNING_BOLT, attacker.level());
                lightning.setPos(target.getX(), target.getY(), target.getZ());
                attacker.level().addFreshEntity(lightning);
            }
        }

        MobEffectInstance conducting = target.getEffect((Holder<MobEffect>) ModEffects.CONDUCTING);
        if (conducting == null) return;
        if (Cooldowns.isOnCooldown(player, ASTRAPE_CD)) return;

        int level = conducting.getAmplifier() + 1;
        float extraDamage = 5.0f * level;

        target.hurtServer(
                (ServerLevel) attacker.level(),
                attacker.level().damageSources().playerAttack(player),
                extraDamage
        );
        for (int i = 0; i < 5; i++) {
            LightningBolt lightning =
                    new LightningBolt(EntityType.LIGHTNING_BOLT, attacker.level());
            lightning.setPos(target.getX(), target.getY(), target.getZ());
            attacker.level().addFreshEntity(lightning);
        }
        Cooldowns.start(player, ASTRAPE_CD, 10);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);

        if (!world.isClientSide()) {
            AstrapeEntity entity = new AstrapeEntity(world, user, stack);
            entity.setPos(user.getX(), user.getEyeY(), user.getZ());
            entity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 3.5f, 1.0F);
            world.addFreshEntity(entity);
            if (!user.isCreative()) user.getCooldowns().addCooldown(stack, 15 * 20);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        Player player = (Player) entity;
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            player.displayClientMessage(Component.translatable("item.anvil.rename"), true);
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

    @Override
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pantheon.astrape");
    }
}