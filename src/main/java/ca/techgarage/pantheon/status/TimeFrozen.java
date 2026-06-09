package ca.techgarage.pantheon.status;

import eu.pb4.polymer.core.api.other.PolymerMobEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class TimeFrozen  extends MobEffect implements PolymerMobEffect {
    public TimeFrozen(MobEffectCategory category, int color) {
        super(category, color);
    }


    @Override
    public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier) {

        Vec3 pos = entity.position();

        entity.teleportTo(pos.x, pos.y, pos.z);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setNoGravity(true);

        entity.fallDistance = 0;

        entity.hurtMarked = true;

        return true;
    }
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
    @Override
    public void onMobRemoved(ServerLevel world, LivingEntity entity, int amplifier,
                             Entity.RemovalReason reason) {

        entity.setNoGravity(false);
    }

    @Override
    public ItemStack getPolymerIcon(MobEffect effect, ServerPlayer player) {
        return new ItemStack(Items.CLOCK);
    }
}
