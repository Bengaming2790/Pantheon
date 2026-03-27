package ca.techgarage.pantheon.status;

import eu.pb4.polymer.core.api.other.PolymerMobEffect;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Drowsy extends MobEffect implements PolymerMobEffect {

    public Drowsy(MobEffectCategory harmful, int i) {
        super(MobEffectCategory.HARMFUL, 0x5A5A5A); // gray-ish
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true; // every tick
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {

        int duration = 40;
        int level = amplifier;

        entity.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS,
                duration,
                level,
                false,
                false,
                false
        ));

        entity.addEffect(new MobEffectInstance(
                MobEffects.BLINDNESS,
                duration,
                0,
                false,
                false,
                false
        ));

        entity.addEffect(new MobEffectInstance(
                        MobEffects.MINING_FATIGUE,
                duration,
                level,
                false,
                false,
                false
        ));

        return true;
    }

    // Polymer icon (what players see in inventory)
    @Override
    public ItemStack getPolymerIcon(MobEffect effect, ServerPlayer player) {
        return new ItemStack(Items.PHANTOM_MEMBRANE);
    }

    public Component getName() {
        return Component.translatable("effect.pantheon.drowsy");
    }
}

