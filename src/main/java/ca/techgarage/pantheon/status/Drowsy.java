package ca.techgarage.pantheon.status;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class Drowsy extends StatusEffect implements PolymerStatusEffect {

    public Drowsy(StatusEffectCategory harmful, int i) {
        super(StatusEffectCategory.HARMFUL, 0x5A5A5A); // gray-ish
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // every tick
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {

        int duration = 40;
        int level = amplifier;

        entity.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS,
                duration,
                level,
                false,
                false,
                false
        ));

        entity.addStatusEffect(new StatusEffectInstance(
                StatusEffects.BLINDNESS,
                duration,
                0,
                false,
                false,
                false
        ));

        entity.addStatusEffect(new StatusEffectInstance(
                StatusEffects.MINING_FATIGUE,
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
    public ItemStack getPolymerIcon(StatusEffect effect, ServerPlayerEntity player) {
        return new ItemStack(Items.PHANTOM_MEMBRANE);
    }

    @Override
    public Text getName() {
        return Text.translatable("effect.pantheon.drowsy");
    }
}

