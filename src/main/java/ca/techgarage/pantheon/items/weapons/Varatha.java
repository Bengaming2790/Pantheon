package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.DashState;
import ca.techgarage.pantheon.items.material.ModToolMaterials;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;
import ca.techgarage.pantheon.api.Dash;
import java.util.Random;

public class Varatha extends AxeItem implements PolymerItem {

    private static final Random random = new Random();
    public Varatha(Settings settings) {
        super(ModToolMaterials.VARATHA_TOOL_MATERIAL, 7.0F, 1.2f, settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);
            if(user.raycast(2, 0, false).getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                return ActionResult.PASS;
            }
            if (!(user.getGameMode() == GameMode.CREATIVE)) {
                user.getItemCooldownManager().set(stack, 200); //10 second cooldown
            }
            user.useRiptide(10, 1.0f, stack);
            Dash.dashForward(user, 0.75f);
            DashState.start((ServerPlayerEntity) user, 10, ParticleTypes.RAID_OMEN);

        }
        return ActionResult.SUCCESS;

    }




    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.DIAMOND_SWORD;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.pantheon.varatha");
    }
}
