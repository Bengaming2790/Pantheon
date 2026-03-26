package ca.techgarage.pantheon.items.weapons;

import eu.pb4.polymer.core.api.item.PolymerItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import xyz.nucleoid.packettweaker.PacketContext;
import ca.techgarage.pantheon.api.Grapple;

import java.util.LinkedHashSet;
import java.util.List;

public class Kynthia extends BowItem implements PolymerItem {
    public static final String KYNTHIA_GRAPPLE_CD = "kynthia_grapple_cd";

    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath("pantheon", "kynthia");

    public Kynthia(Properties settings) {
        super(settings.component(DataComponents.MAX_STACK_SIZE, 1)
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                .component(DataComponents.UNBREAKABLE, Unit.INSTANCE).fireResistant()
                .component(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>(List.of(
                        DataComponents.ATTRIBUTE_MODIFIERS,
                        DataComponents.UNBREAKABLE
                ))))
        );
    }


    public void activate(Player player) {
        Grapple.fire(player, 32.0);

    }

    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return MODEL;
    }


    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.BOW;
    }
    @Override
    protected Projectile createProjectile(Level world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        Item item = projectileStack.getItem();
        ArrowItem arrowItem = item instanceof ArrowItem a ? a : (ArrowItem) Items.ARROW;

        AbstractArrow arrow = arrowItem.createArrow(world, projectileStack, shooter, weaponStack);

        arrow.hurt(shooter.damageSources().arrow(arrow, shooter), 10.0f);

        return arrow;
    }
    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.pantheon.kynthia");
    }

}
