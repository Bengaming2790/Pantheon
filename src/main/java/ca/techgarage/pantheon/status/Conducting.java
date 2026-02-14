package ca.techgarage.pantheon.status;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class Conducting extends StatusEffect implements PolymerStatusEffect {
    public Conducting(StatusEffectCategory category, int color) {
        super(StatusEffectCategory.HARMFUL, 0xC5FF00);
    }
    @Override
    public ItemStack getPolymerIcon(StatusEffect effect, ServerPlayerEntity player) {
        return new ItemStack(Items.LIGHTNING_ROD);
    }
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // run every tick
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        // Leave empty if logic is handled elsewhere (like postHit)
        return true;
    }
    @Override
    public Text getName() {
        return Text.translatable("effect.pantheon.conducting");
    }
}
