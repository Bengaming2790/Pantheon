package ca.techgarage.pantheon.status;

import eu.pb4.polymer.core.api.other.PolymerMobEffect;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Conducting extends MobEffect implements PolymerMobEffect {
    public Conducting(MobEffectCategory category, int color) {
        super(category, color);
    }
    @Override
    public ItemStack getPolymerIcon(MobEffect effect, ServerPlayer player) {
        return new ItemStack(Items.LIGHTNING_ROD);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true; // run every tick
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {
        // Leave empty if logic is handled elsewhere (like postHit)
        return true;
    }
    public Component getName() {
        return Component.translatable("effect.pantheon.conducting");
    }
}
