package ca.techgarage.pantheon.status;

import ca.techgarage.pantheon.DamageSources.ModDamageSources;
import ca.techgarage.pantheon.api.DashState;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class Bleed extends StatusEffect implements PolymerStatusEffect {
    public Bleed(StatusEffectCategory category, int color) {
        super(category, color);
    }
    public ItemStack getPolymerIcon(StatusEffect effect, ServerPlayerEntity player) {
        return new ItemStack(Items.REDSTONE);
    }
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // run every tick
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {

        entity.damage(
                world,
                ModDamageSources.bleeding(world),
                (0.5f + 1f) * amplifier
        );


        DustParticleEffect dust = new DustParticleEffect(
                16711680,
                1.0f
        );

        world.spawnParticles(
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


    public Text getName() {
        return Text.translatable("effect.pantheon.bleed");
    }

}
