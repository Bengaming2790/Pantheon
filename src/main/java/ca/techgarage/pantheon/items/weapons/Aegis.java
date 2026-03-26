package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.Cooldowns;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public class Aegis extends ShieldItem implements PolymerItem {

    private static final String AEGIS_CD = "aegis_cd";
    private static final int COOLDOWN = 20 * 60; // 1 minute
    private static final int DEFLECT_BUFF_TIME = 20 * 5;

    public Aegis(Properties settings) {
        super(settings
                .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributeModifiers())
                .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                .component(DataComponents.MAX_STACK_SIZE, 1)
                .component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(0.25F, 1.0F,
                        List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                        new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F), Optional.of(DamageTypeTags.BYPASSES_SHIELD), Optional.of(SoundEvents.SHIELD_BLOCK),
                        Optional.of(SoundEvents.SHIELD_BREAK))).component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)
                .fireResistant()
                .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>(List.of(
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.UNBREAKABLE
                ))))
        );
        registerEvents();
    }


    public static ItemAttributeModifiers createAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                BASE_ATTACK_DAMAGE_ID,
                                7,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                BASE_ATTACK_SPEED_ID,
                                -2.3,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    private static void registerEvents() {

        // ABSOLUTE DEFLECT (one-use block)
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (entity, source, baseDamage, damageTaken, blocked) -> {

                    if (!(entity instanceof ServerPlayer player)) return;

                    if (!blocked) return;

                    boolean holdingAegis =
                            player.getMainHandItem().getItem() instanceof Aegis
                                    || player.getOffhandItem().getItem() instanceof Aegis;

                    if (!holdingAegis) return;

                    player.getCooldowns().addCooldown(player.getActiveItem(), 20 * 60);
                    deflect(player, source, baseDamage);
                    player.addEffect(
                            new MobEffectInstance(
                                    MobEffects.RESISTANCE,
                                    40,
                                    1
                            )
                    );
                }
        );


        // DIVINE PROTECTION (Resistance I while held)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (getHeldAegis(player) == null) continue;

                player.addEffect(
                        new MobEffectInstance(
                                MobEffects.RESISTANCE,
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



    private static void deflect(ServerPlayer player, DamageSource source, float damage) {

        // Reflect damage
        if (source.getEntity() instanceof LivingEntity attacker) {
            attacker.hurt(
                    player.level().damageSources().playerAttack(player),
                    damage * 1.25f
            );
        }

        // Resistance II
        player.addEffect(
                new MobEffectInstance(
                        MobEffects.RESISTANCE,
                        DEFLECT_BUFF_TIME,
                        1
                )
        );

        player.addEffect(
                new MobEffectInstance(
                        MobEffects.STRENGTH,
                        DEFLECT_BUFF_TIME,
                        0
                )
        );

        // Disable shield
        player.getCooldowns().addCooldown(getHeldAegis(player), COOLDOWN);

        // Internal cooldown tracking
        Cooldowns.start(player, AEGIS_CD, COOLDOWN);
    }


    private static ItemStack getHeldAegis(Player player) {
        if (player.getMainHandItem().getItem() instanceof Aegis)
            return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof Aegis)
            return player.getOffhandItem();
        return null;
    }
    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath("pantheon", "aegis");
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.SHIELD;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pantheon.aegis");
    }
}
