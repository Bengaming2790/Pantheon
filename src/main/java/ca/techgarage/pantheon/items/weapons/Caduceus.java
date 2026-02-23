package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.AOEDamage;
import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;

public class Caduceus extends Item implements PolymerItem {
    public Caduceus(Settings settings) {
        super(settings.component(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE).component(DataComponentTypes.MAX_STACK_SIZE, 1).component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createAttributeModifiers()).fireproof()); applyEffects();
    }
    private static final String CADUCEUS_DROWSY_CD = "caduceus_drowsy_cd";
    private static final String CADUCEUS_RENDEZVOUS_CD = "caduceus_rendezvous_cd";
    private static final String CADUCEUS_RENDEZVOUS_TIMER = "caduceus_rendezvous_timer";
    @Override
    public void postHit(ItemStack stack, net.minecraft.entity.LivingEntity target, net.minecraft.entity.LivingEntity attacker) {
        super.postHit(stack, target, attacker);
        if (attacker instanceof net.minecraft.entity.player.PlayerEntity player) {
            if (!Cooldowns.isOnCooldown(player, CADUCEUS_DROWSY_CD)) {
                target.addStatusEffect(new StatusEffectInstance(ModEffects.DROWSY, 20 * 5, 1, true, true, true), target);
                Cooldowns.start(player, CADUCEUS_DROWSY_CD, 20 * 10);
            }
        }
    }
    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                5,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.ATTACK_SPEED,
                        new EntityAttributeModifier(
                                BASE_ATTACK_SPEED_MODIFIER_ID,
                                -2,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {

        if (world.isClient()) return ActionResult.SUCCESS;

        ServerPlayerEntity player = (ServerPlayerEntity) user;

        // Cooldown check
        if (Cooldowns.isOnCooldown(player, CADUCEUS_RENDEZVOUS_CD)) {
            return ActionResult.FAIL;
        }

        RandevuData existing = RandevuManager.get(player);

        if (existing == null) {

            RandevuManager.create(player);
            player.sendMessage(Text.translatable("item.caduceus.randevu.place"), true);
            Cooldowns.start(player, CADUCEUS_RENDEZVOUS_TIMER, 20 * 30); // 30-second timer to return
            world.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_BEACON_ACTIVATE,
                    SoundCategory.PLAYERS,
                    0.5F, // volume
                    0F  // pitch
            );
            return ActionResult.SUCCESS;
        }

        if (player.getEntityWorld().getTime() > existing.expireTime()) {
            RandevuManager.remove(player);
            player.sendMessage(Text.translatable("item.caduceus.randevu.expire"), true);
            return ActionResult.FAIL;
        }

        // Teleport
        player.teleport(
                existing.world(),
                existing.position().x,
                existing.position().y,
                existing.position().z,
                EnumSet.noneOf(PositionFlag.class),
                player.getYaw(),
                player.getPitch(),
                true
        );
        world.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
                1.0F, // volume
                1.0F  // pitch
        );
        AOEDamage.applyAoeDamage(player, player.getEntityWorld(), player.getEntityPos(), 8f, 2.5f);

        if (player.getGameMode() != GameMode.CREATIVE) {
            Cooldowns.start(player, CADUCEUS_RENDEZVOUS_CD, 20 * 120);
            user.getItemCooldownManager().set(user.getStackInHand(hand), 20 * 120); //10 second cooldown

        }
        RandevuManager.remove(player);

        return ActionResult.SUCCESS;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.STICK;
    }
    private static ItemStack getHeldCaduceus(PlayerEntity player) {
        if (player.getMainHandStack().getItem() instanceof Caduceus)
            return player.getMainHandStack();
        return null;
    }
    public static void applyEffects(){
        // Swift Travel
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (getHeldCaduceus(player) == null) continue;

                player.addStatusEffect(
                        new StatusEffectInstance(
                                StatusEffects.SPEED,
                                40,
                                0,
                                true,
                                false,
                                false
                        )
                );
            }
        });
    }

    /**
     * @param expireTime world time in ticks
     */
    public record RandevuData(ServerWorld world, Vec3d position, long expireTime) {

        public void tick(ServerWorld world) {
                Vec3d pos = this.position();
                world.spawnParticles(
                        ParticleTypes.END_ROD,
                        pos.x, pos.y + 1.0, pos.z,
                        5,
                        0.2, 0.5, 0.2,
                        0.01
                );
            }
        }

    public static class RandevuManager {

        public static final Map<UUID, RandevuData> POINTS = new HashMap<>();

        public static void create(ServerPlayerEntity player) {

            ServerWorld world = player.getEntityWorld();
            Vec3d pos = player.getEntityPos();
            long expire = world.getTime() + (20 * 30); // 30 seconds

            POINTS.put(player.getUuid(),
                    new RandevuData(world, pos, expire));
        }
        public static void tickAll() {
            Iterator<Map.Entry<UUID, RandevuData>> iterator = POINTS.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, RandevuData> entry = iterator.next();
                RandevuData data = entry.getValue();

                // Remove expired points
                if (data.world().getTime() > data.expireTime()) {
                    iterator.remove();
                    continue;
                }

                // Spawn particles
                data.tick(data.world());
            }
        }
        public static RandevuData get(ServerPlayerEntity player) {
            return POINTS.get(player.getUuid());
        }

        public static void remove(ServerPlayerEntity player) {
            POINTS.remove(player.getUuid());
        }
    }

}
