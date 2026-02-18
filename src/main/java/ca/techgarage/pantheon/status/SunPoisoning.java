package ca.techgarage.pantheon.status;

import ca.techgarage.pantheon.DamageSources.ModDamageSources;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class SunPoisoning extends StatusEffect implements PolymerStatusEffect {
    public SunPoisoning(StatusEffectCategory category, int color) {
        super(category, color);
    }
    public ItemStack getPolymerIcon(StatusEffect effect, ServerPlayerEntity player) {
        return new ItemStack(Items.SUNFLOWER);
    }
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // run every tick
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {

        entity.damage(
                world,
                ModDamageSources.sunPoisoning(world),
                1.66f
        );



        world.spawnParticles(
                ParticleTypes.FIREFLY,
                entity.getX(),
                entity.getY() + 0.5,
                entity.getZ(),
                2,
                0.2,
                0.25,
                0.2,
                0.0
        );

        return true;
    }


    public Text getName() {
        return Text.translatable("effect.pantheon.sun_poisoning");
    }

}
