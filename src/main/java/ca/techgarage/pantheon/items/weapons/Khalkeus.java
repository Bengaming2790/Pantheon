package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.AOEDamage;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Timer;

public class Khalkeus extends MaceItem implements PolymerItem {
    public Khalkeus(Settings settings) {
        super(settings.component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, getDefaultAttributeModifiers()).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true));
    }

    private static final String AOE_CD = "khalkeus_aoe_cd";
    public static AttributeModifiersComponent getDefaultAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                7,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                Item.BASE_ATTACK_SPEED_MODIFIER_ID,
                                -3.2,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            if(user.raycast(2, 0, false).getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                return ActionResult.PASS;
            }
            if (!(user.getGameMode() == GameMode.CREATIVE)) {
                user.getItemCooldownManager().set(stack, 200);
            }
            user.useRiptide(10, 1.0f, stack);
            Dash.dashForward(user, 0.95f);
            DashState.start((ServerPlayerEntity) user, 10, ParticleTypes.FLAME);
        }
        return ActionResult.SUCCESS;
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
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (attacker.getEntityWorld().isClient()) return;
            target.setOnFireForTicks(20 * 4);

            if (!Cooldowns.isOnCooldown((PlayerEntity) attacker, AOE_CD)) {
                AOEDamage.applyAoeDamage(attacker, target, 6.0f, 2.5f, true, 4);
                Cooldowns.start((PlayerEntity) attacker, AOE_CD, 20 * 15);
            }

    }
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.khalkeus").formatted();
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.DIAMOND;
    }
}
