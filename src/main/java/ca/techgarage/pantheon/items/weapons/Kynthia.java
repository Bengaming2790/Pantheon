package ca.techgarage.pantheon.items.weapons;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;
import ca.techgarage.pantheon.api.Grapple;

public class Kynthia extends BowItem implements PolymerItem {
    public static final String KYNTHIA_GRAPPLE_CD = "kynthia_grapple_cd";

    private static final Identifier MODEL =
            Identifier.of("pantheon", "kynthia");

    public Kynthia(Settings settings) {
        super(settings.component(DataComponentTypes.MAX_STACK_SIZE, 1)
                .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
                .component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).fireproof());
    }


    public void activate(PlayerEntity player) {
        Grapple.fire(player, 32.0);

    }

    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }


    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.BOW;
    }
    @Override
    protected ProjectileEntity createArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        Item item = projectileStack.getItem();
        ArrowItem arrowItem = item instanceof ArrowItem a ? a : (ArrowItem) Items.ARROW;

        PersistentProjectileEntity arrow = arrowItem.createArrow(world, projectileStack, shooter, weaponStack);
        if (critical) {
            arrow.setCritical(true);
        }
        arrow.setDamage(10.0);

        return arrow;
    }
    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.kynthia").formatted();
    }

}
