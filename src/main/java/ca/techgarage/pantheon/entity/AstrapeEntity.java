package ca.techgarage.pantheon.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class AstrapeEntity extends TridentEntity implements PolymerEntity {

    public AstrapeEntity(EntityType<? extends TridentEntity> entityType, World world) {
        super(entityType, world);
    }
    public AstrapeEntity(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }

    public AstrapeEntity(World world, PlayerEntity player, ItemStack stack) {
        super(world, player, stack);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        
        
    }
    

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.TRIDENT;
    }
}
