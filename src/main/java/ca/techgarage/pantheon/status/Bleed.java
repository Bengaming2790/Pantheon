package ca.techgarage.pantheon.status;

import ca.techgarage.pantheon.DamageSources.ModDamageSources;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Bleed extends MobEffect implements PolymerStatusEffect {
    public Bleed(MobEffectCategory category, int color) {
        super(category, color);
    }
    public ItemStack getPolymerIcon(MobEffect effect, ServerPlayer player) {
        return new ItemStack(Items.REDSTONE);
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true; // run every tick
    }

    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {

        entity.hurt(
                ModDamageSources.bleeding(world),
                (0.5f + 1f) * amplifier
        );


        DustParticleOptions dust = new DustParticleOptions(
                16711680,
                1.0f
        );

        world.sendParticles(
                dust,
                entity.getX(),
                entity.getY() + 0.5,
                entity.getZ(),
                5,
                0.2,
                0.25,
                0.2,
                0.0
        );

        return true;
    }


    public Component getName() {
        return Component.translatable("effect.pantheon.bleed");
    }

}
