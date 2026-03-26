package ca.techgarage.pantheon.entity;

import ca.techgarage.pantheon.api.HomingTracker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class PartyFoulEntity {

    public static void launch(Level level, LivingEntity owner) {
        Snowball snowball = new Snowball(level, owner, new ItemStack(Items.SNOWBALL));

        snowball.setPos(owner.getX(), owner.getEyeY(), owner.getZ());

        snowball.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0f, 1.5f, 0.0f);

        level.addFreshEntity(snowball);

        HomingTracker.attach(snowball, owner);
    }
}