package ca.techgarage.pantheon.items.weapons;

import ca.techgarage.pantheon.api.AOEDamage;
import ca.techgarage.pantheon.api.Cooldowns;
import ca.techgarage.pantheon.status.ModEffects;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;

public class Caduceus extends Item implements PolymerItem {

    private static final String CADUCEUS_DROWSY_CD = "caduceus_drowsy_cd";
    private static final String CADUCEUS_RENDEZVOUS_CD = "caduceus_rendezvous_cd";
    private static final String CADUCEUS_RENDEZVOUS_TIMER = "caduceus_rendezvous_timer";

    public Caduceus(Properties settings) {
        super(settings
                .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                .component(DataComponents.MAX_STACK_SIZE, 1)
                .component(DataComponents.ATTRIBUTE_MODIFIERS, createAttributeModifiers())
                .fireResistant());
        applyEffects();
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            if (!Cooldowns.isOnCooldown(player, CADUCEUS_DROWSY_CD)) {
                target.addEffect(new MobEffectInstance((Holder<MobEffect>) ModEffects.DROWSY, 100, 1, true, true, true));
                Cooldowns.start(player, CADUCEUS_DROWSY_CD, 200);
            }
        }
        return;
    }

    public static ItemAttributeModifiers createAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ServerPlayer player = (ServerPlayer) user;

        if (Cooldowns.isOnCooldown(player, CADUCEUS_RENDEZVOUS_CD)) {
            return InteractionResult.FAIL;
        }

        RandevuData existing = RandevuManager.get(player);

        if (existing == null) {
            RandevuManager.create(player);
            player.displayClientMessage(Component.translatable("item.caduceus.randevu.place"), true);
            Cooldowns.start(player, CADUCEUS_RENDEZVOUS_TIMER, 600);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5F, 0.0F);
            return InteractionResult.SUCCESS;
        }

        if (level.getGameTime() > existing.expireTime()) {
            RandevuManager.remove(player);
            player.displayClientMessage(Component.translatable("item.caduceus.randevu.expire"), true);
            return InteractionResult.FAIL;
        }


        player.teleportTo(
                (ServerLevel) existing.world(),
                existing.position().x,
                existing.position().y,
                existing.position().z,
                EnumSet.noneOf(Relative.class),
                player.getYRot(),
                player.getXRot(),
                false
        );

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        AOEDamage.applyAoeDamage(player, player.level(), player.position(), 8f, 2.5f);

        if (player.gameMode.getGameModeForPlayer() != GameType.CREATIVE) {
            Cooldowns.start(player, CADUCEUS_RENDEZVOUS_CD, 2400);
            user.getCooldowns().addCooldown(player.getActiveItem(), 2400);
        }
        RandevuManager.remove(player);

        return InteractionResult.SUCCESS;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext packetContext) {
        return Items.STICK;
    }

    private static ItemStack getHeldCaduceus(Player player) {
        if (player.getMainHandItem().getItem() instanceof Caduceus) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof Caduceus) return player.getOffhandItem();
        return null;
    }

    public static void applyEffects(){
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (getHeldCaduceus(player) == null) continue;
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, false));
            }
        });
    }

    public record RandevuData(Level world, Vec3 position, long expireTime) {
        public void tick(ServerLevel level) {
            Vec3 pos = this.position();
            level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0, pos.z, 5, 0.2, 0.5, 0.2, 0.01);
        }
    }

    public static class RandevuManager {
        public static final Map<UUID, RandevuData> POINTS = new HashMap<>();

        public static void create(ServerPlayer player) {
            Level level = player.level();
            Vec3 pos = player.position();
            long expire = level.getGameTime() + 600;
            POINTS.put(player.getUUID(), new RandevuData(level, pos, expire));
        }

        public static void tickAll() {
            Iterator<Map.Entry<UUID, RandevuData>> iterator = POINTS.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, RandevuData> entry = iterator.next();
                RandevuData data = entry.getValue();
                if (data.world().getGameTime() > data.expireTime()) {
                    iterator.remove();
                    continue;
                }
                if (data.world() instanceof ServerLevel serverLevel) {
                    data.tick(serverLevel);
                }
            }
        }

        public static RandevuData get(ServerPlayer player) {
            return POINTS.get(player.getUUID());
        }

        public static void remove(ServerPlayer player) {
            POINTS.remove(player.getUUID());
        }
    }
}