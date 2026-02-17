package ca.techgarage.pantheon.entity;

import ca.techgarage.pantheon.api.HomingTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public class PartyFoulEntity {

    public static void launch(World world, LivingEntity owner) {
        SnowballEntity snowball = new SnowballEntity(world, owner, owner.getMainHandStack());

        snowball.setItem(new ItemStack(Items.SNOWBALL));
        snowball.setPosition(owner.getX(), owner.getEyeY(), owner.getZ());
        snowball.setVelocity(owner, owner.getPitch(), owner.getYaw(), 0f, 1.5f, 0f);

        world.spawnEntity(snowball);

        HomingTracker.attach(snowball, owner);
    }
}
