package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import xyz.nucleoid.packettweaker.PacketContext;

public class Aegis extends ShieldItem implements PolymerItem {

    private static final String AEGIS_CD = "aegis_cd";
    private static final int COOLDOWN = 20 * 60; // 1 minute
    private static final int DEFLECT_BUFF_TIME = 20 * 5;

    public Aegis(Settings settings) {
        super(settings
                .component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers())
                .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE)
                .component(DataComponentTypes.MAX_STACK_SIZE, 1)
        );
        registerEvents();
    }


    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                7,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                BASE_ATTACK_SPEED_MODIFIER_ID,
                                -2.3,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    private static void registerEvents() {

        // ABSOLUTE DEFLECT (one-use block)
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (entity, source, baseDamage, damageTaken, blocked) -> {

                    if (!(entity instanceof ServerPlayerEntity player)) return;

                    if (!blocked) return;

                    boolean holdingAegis =
                            player.getMainHandStack().getItem() instanceof Aegis
                                    || player.getOffHandStack().getItem() instanceof Aegis;

                    if (!holdingAegis) return;

                    player.getItemCooldownManager().set(player.getActiveItem(), 20 * 60);
                    deflect(player, source, baseDamage);
                    player.addStatusEffect(
                            new StatusEffectInstance(
                                    StatusEffects.RESISTANCE,
                                    40,
                                    1
                            )
                    );
                }
        );


        // DIVINE PROTECTION (Resistance I while held)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (getHeldAegis(player) == null) continue;

                player.addStatusEffect(
                        new StatusEffectInstance(
                                StatusEffects.RESISTANCE,
                                40,
                                0,
                                true,
                                false,
                                false
                        )
                );
            }
        });
    }



    private static void deflect(ServerPlayerEntity player, DamageSource source, float damage) {

        // Reflect damage
        if (source.getAttacker() instanceof LivingEntity attacker) {
            attacker.damage(player.getEntityWorld(),
                    player.getEntityWorld().getDamageSources().playerAttack(player),
                    damage * 1.25f
            );
        }

        // Resistance II
        player.addStatusEffect(
                new StatusEffectInstance(
                        StatusEffects.RESISTANCE,
                        DEFLECT_BUFF_TIME,
                        1
                )
        );

        player.addStatusEffect(
                new StatusEffectInstance(
                        StatusEffects.STRENGTH,
                        DEFLECT_BUFF_TIME,
                        0
                )
        );

        // Disable shield
        player.getItemCooldownManager().set(getHeldAegis(player), COOLDOWN);

        // Internal cooldown tracking
        Cooldowns.start(player, AEGIS_CD, COOLDOWN);
    }


    private static ItemStack getHeldAegis(PlayerEntity player) {
        if (player.getMainHandStack().getItem() instanceof Aegis)
            return player.getMainHandStack();
        if (player.getOffHandStack().getItem() instanceof Aegis)
            return player.getOffHandStack();
        return null;
    }


    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.SHIELD;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.aegis");
    }
}
