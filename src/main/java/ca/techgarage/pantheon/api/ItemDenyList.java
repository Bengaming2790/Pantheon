package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.PantheonConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.*;

public class ItemDenyList {



    private static final Set<Item> DENIED_ITEMS = new HashSet<>();

    private static final Map<Item, Set<Identifier>> DENIED_POTION_TYPES = new HashMap<>();

    private static final Map<Identifier, Integer> DENIED_ENCHANTMENTS = new HashMap<>();

    private static final Map<Identifier, Integer> DENIED_EFFECTS = new HashMap<>();



    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(ItemDenyList::onServerTick);
    }

    static {
        deny(Items.END_CRYSTAL);
        deny(Items.TNT_MINECART);

        denyPotion(Potions.HEALING);
        denyPotion(Potions.STRONG_HEALING);
        denyPotion(Potions.SWIFTNESS);
        denyPotion(Potions.STRONG_SWIFTNESS);
        denyPotion(Potions.TURTLE_MASTER);
        denyPotion(Potions.STRONG_TURTLE_MASTER);
        denyPotion(Potions.HARMING);
        denyPotion(Potions.STRONG_HARMING);

        denyTippedArrow(Potions.HARMING);
        denyTippedArrow(Potions.STRONG_HARMING);

        denyEnchantmentAboveLevel(Enchantments.PROTECTION, PantheonConfig.bannedProtectionLevel - 1);

        denyEffectAboveAmplifier(MobEffects.INSTANT_HEALTH,   -1);
        denyEffectAboveAmplifier(MobEffects.INSTANT_DAMAGE,   -1);
        denyEffectAboveAmplifier(MobEffects.SPEED, 0);
        denyEffectAboveAmplifier(MobEffects.SLOWNESS, 2);
        denyEffectAboveAmplifier(MobEffects.RESISTANCE,  2);
    }


    public static void deny(Item item) {
        DENIED_ITEMS.add(item);
    }

    public static void denyPotion(Holder<Potion> potionHolder) {
        Identifier key = resolveHolder(potionHolder);
        if (key == null) return;
        denyPotionForItem(Items.POTION,          key);
        denyPotionForItem(Items.SPLASH_POTION,   key);
        denyPotionForItem(Items.LINGERING_POTION, key);
    }

    public static void denyTippedArrow(Holder<Potion> potionHolder) {
        Identifier key = resolveHolder(potionHolder);
        if (key == null) return;
        denyPotionForItem(Items.TIPPED_ARROW, key);
    }

    public static void denyPotionForItem(Item item, Identifier potionKey) {
        DENIED_POTION_TYPES.computeIfAbsent(item, k -> new HashSet<>()).add(potionKey);
    }

    public static void denyEnchantmentAboveLevel(ResourceKey<Enchantment> enchKey, int maxAllowedLevel) {
        DENIED_ENCHANTMENTS.put(enchKey.identifier(), maxAllowedLevel);
    }


    public static void denyEffectAboveAmplifier(Holder<MobEffect> effectHolder, int maxAllowedAmplifier) {
        Identifier key = resolveHolder(effectHolder);
        if (key == null) return;
        DENIED_EFFECTS.put(key, maxAllowedAmplifier);
    }


    public static boolean isDenied(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (DENIED_ITEMS.contains(stack.getItem())) return true;

        Set<Identifier> deniedPotions = DENIED_POTION_TYPES.get(stack.getItem());
        if (deniedPotions != null) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents != null) {
                Optional<Identifier> potionId = contents.potion()
                        .flatMap(h -> h.unwrapKey().map(k -> k.identifier()));
                if (potionId.isPresent() && deniedPotions.contains(potionId.get())) {
                    return true;
                }
            }
        }

        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments != null) {
            for (Holder<Enchantment> holder : enchantments.keySet()) {
                Optional<Identifier> locOpt = holder.unwrapKey().map(k -> k.identifier());
                if (locOpt.isEmpty()) continue;
                Identifier loc = locOpt.get();
                if (DENIED_ENCHANTMENTS.containsKey(loc)) {
                    if (enchantments.getLevel(holder) > DENIED_ENCHANTMENTS.get(loc)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isDenied(Item item) {
        return DENIED_ITEMS.contains(item);
    }


    private static int tickCounter = 0;

    private static void onServerTick(MinecraftServer server) {
        if (++tickCounter % 2 != 0) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            stripDeniedEffects(player);
            stripDeniedTippedArrows(player);
        }
    }

    private static void stripDeniedEffects(ServerPlayer player) {
        if (DENIED_EFFECTS.isEmpty()) return;

        // Snapshot keys to avoid ConcurrentModificationException
        List<Holder<MobEffect>> toRemove = new ArrayList<>();

        for (MobEffectInstance instance : player.getActiveEffects()) {
            Holder<MobEffect> holder = instance.getEffect();
            Optional<Identifier> locOpt = holder.unwrapKey().map(k -> k.identifier());
            if (locOpt.isEmpty()) continue;

            Identifier loc = locOpt.get();
            if (!DENIED_EFFECTS.containsKey(loc)) continue;

            int maxAllowed = DENIED_EFFECTS.get(loc);
            if (maxAllowed == -1 || instance.getAmplifier() > maxAllowed) {
                toRemove.add(holder);
            }
        }

        for (Holder<MobEffect> effect : toRemove) {
            player.removeEffect(effect);
        }
    }


    private static void stripDeniedTippedArrows(ServerPlayer player) {
        Set<Identifier> deniedArrowPotions = DENIED_POTION_TYPES.get(Items.TIPPED_ARROW);
        if (deniedArrowPotions == null || deniedArrowPotions.isEmpty()) return;

        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || stack.getItem() != Items.TIPPED_ARROW) continue;

            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents == null) continue;

            Optional<Identifier> potionId = contents.potion()
                    .flatMap(h -> h.unwrapKey().map(k -> k.identifier()));

            if (potionId.isPresent() && deniedArrowPotions.contains(potionId.get())) {
                player.drop(stack, false);
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }


    private static <T> Identifier resolveHolder(Holder<T> holder) {
        return holder.unwrapKey().map(k -> k.identifier()).orElse(null);
    }
}