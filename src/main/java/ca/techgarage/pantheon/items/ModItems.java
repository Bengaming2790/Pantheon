package ca.techgarage.pantheon.items;

import ca.techgarage.pantheon.api.DroppedItemGlow;
import ca.techgarage.pantheon.items.weapons.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static ca.techgarage.pantheon.Pantheon.MOD_ID;

public class ModItems {

    public static final ResourceKey<Item> DRACHMA_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "drachma"));

    public static final ResourceKey<Item> VARATHA_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "varatha"));

    public static final ResourceKey<Item> KHALKEOUS_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "khalkeous"));

    public static final ResourceKey<Item> AEGIS_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "aegis"));

    public static final ResourceKey<Item> KYNTHIA_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "kynthia"));

    public static final ResourceKey<Item> ENYALIOS_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "enyalios"));

    public static final ResourceKey<Item> ASTRAPE_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "astrape"));

    public static final ResourceKey<Item> PEITHO_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "peitho"));

    public static final ResourceKey<Item> TRIAINA_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "triaina"));

    public static final ResourceKey<Item> CADUCEUS_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "caduceus"));

    public static final ResourceKey<Item> PHOEBUS_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "phoebus"));

    public static final ResourceKey<Item> THYRSUS_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "thyrsus"));

    public static final ResourceKey<Item> GLACIERA_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "glaciera"));

    public static final ResourceKey<Item> ICARUS_WINGS_KEY =
            ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "icarus_wings"));

    public static Item DRACHMA;
    public static Item VARATHA;
    public static Item KHALKEOUS;
    public static Item AEGIS;
    public static Item KYNTHIA;
    public static Item ENYALIOS;
    public static Item ASTRAPE;
    public static Item PEITHO;
    public static Item TRIAINA;
    public static Item CADUCEUS;
    public static Item PHOEBUS;
    public static Item THYRSUS;
    public static Item GLACIERA;
    public static Item ICARUS_WINGS;

    public static void registerModItems() {
        // In Mojang Mappings, Registry.register uses BuiltInRegistries.ITEM
        DRACHMA = Registry.register(
                BuiltInRegistries.ITEM,
                DRACHMA_KEY,
                new DrachmaItem(new Item.Properties().setId(DRACHMA_KEY))
        );
        VARATHA = Registry.register(
                BuiltInRegistries.ITEM,
                VARATHA_KEY,
                new Varatha(new Item.Properties().setId(VARATHA_KEY))
        );
        KHALKEOUS = Registry.register(
                BuiltInRegistries.ITEM,
                KHALKEOUS_KEY,
                new Khalkeus(new Item.Properties().setId(KHALKEOUS_KEY))
        );
        AEGIS = Registry.register(
                BuiltInRegistries.ITEM,
                AEGIS_KEY,
                new Aegis(new Item.Properties().setId(AEGIS_KEY))
        );
        KYNTHIA = Registry.register(
                BuiltInRegistries.ITEM,
                KYNTHIA_KEY,
                new Kynthia(new Item.Properties().setId(KYNTHIA_KEY))
        );
        ENYALIOS = Registry.register(
                BuiltInRegistries.ITEM,
                ENYALIOS_KEY,
                new Enyalios(new Item.Properties().setId(ENYALIOS_KEY))
        );
        ASTRAPE = Registry.register(
                BuiltInRegistries.ITEM,
                ASTRAPE_KEY,
                new Astrape(new Item.Properties().setId(ASTRAPE_KEY))
        );
        PEITHO = Registry.register(
                BuiltInRegistries.ITEM,
                PEITHO_KEY,
                new Peitho(new Item.Properties().setId(PEITHO_KEY)));
        TRIAINA = Registry.register(
                BuiltInRegistries.ITEM,
                TRIAINA_KEY,
                new Triaina(new Item.Properties().setId(TRIAINA_KEY)));
        CADUCEUS = Registry.register(
                BuiltInRegistries.ITEM,
                CADUCEUS_KEY,
                new Caduceus(new Item.Properties().setId(CADUCEUS_KEY)));
        PHOEBUS = Registry.register(
                BuiltInRegistries.ITEM,
                PHOEBUS_KEY,
                new Phoebus(new Item.Properties().setId(PHOEBUS_KEY)));
        THYRSUS = Registry.register(
                BuiltInRegistries.ITEM,
                THYRSUS_KEY,
                new Thyrsus(new Item.Properties().setId(THYRSUS_KEY)));
        GLACIERA = Registry.register(
                BuiltInRegistries.ITEM,
                GLACIERA_KEY,
                new Glaciera(new Item.Properties().setId(GLACIERA_KEY)));
        ICARUS_WINGS = Registry.register(
                BuiltInRegistries.ITEM,
                ICARUS_WINGS_KEY,
                new IcarusWings(new Item.Properties().setId(ICARUS_WINGS_KEY)));
    }

    public static Set<Item> getAllItems() {
        Set<Item> items = new HashSet<>();
        try {
            for (Field field : ModItems.class.getDeclaredFields()) {
                if (field.getType() == Item.class) {
                    Item item = (Item) field.get(null);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static void applyGlowToAllDrops(ItemEntity itemEntity) {
        Item item = itemEntity.getItem().getItem(); // getStack() -> getItem(), getItem() -> getItem()

        if (!(item instanceof GlowItem glowItem)) return;

        DroppedItemGlow.applyGlow(itemEntity, glowItem.getGlowColor());
    }
}