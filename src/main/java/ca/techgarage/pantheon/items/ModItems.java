package ca.techgarage.pantheon.items;

import ca.techgarage.pantheon.Pantheon;
import ca.techgarage.pantheon.items.weapons.*;
import ca.techgarage.pantheon.status.Conducting;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import static ca.techgarage.pantheon.Pantheon.MOD_ID;

public class ModItems {

    //Items
    public static final RegistryKey<Item> DRACHMA_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "drachma"));

    public static final RegistryKey<Item> VARATHA_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "varatha"));

    public static final RegistryKey<Item> KHALKEOUS_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "khalkeous"));
    public static final RegistryKey<Item> AEGIS_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "aegis"));
    public static final RegistryKey<Item> KYNTHIA_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "kynthia"));
    public static final RegistryKey<Item> ENYALIOS_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "enyalios"));
    public static final RegistryKey<Item> ASTRAPE_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "astrape"));
    public static final RegistryKey<Item> PEITHO_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "peitho"));
    public static final RegistryKey<Item> TRIAINA_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "triaina"));
    public static final RegistryKey<Item> CADUCEUS_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "caduceus"));
    public static final RegistryKey<Item> PHOEBUS_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "phoebus"));
    public static final RegistryKey<Item> THYRSUS_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "thyrsus"));
    public static final RegistryKey<Item> GLACIERA_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "glaciera"));
    public static final RegistryKey<Item> ICARUS_WINGS_KEY =
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "icarus_wings"));

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
        DRACHMA = Registry.register(
                Registries.ITEM,
                DRACHMA_KEY,
                new DrachmaItem(new Item.Settings().registryKey(DRACHMA_KEY))
        );
        VARATHA = Registry.register(
                Registries.ITEM,
                VARATHA_KEY,
                new Varatha(new Item.Settings().registryKey(VARATHA_KEY))
        );
        KHALKEOUS = Registry.register(
                Registries.ITEM,
                KHALKEOUS_KEY,
                new Khalkeus(new Item.Settings().registryKey(KHALKEOUS_KEY))
        );
        AEGIS = Registry.register(
                Registries.ITEM,
                AEGIS_KEY,
                new Aegis(new Item.Settings().registryKey(AEGIS_KEY))
        );
        KYNTHIA = Registry.register(
                Registries.ITEM,
                KYNTHIA_KEY,
                new Kynthia(new Item.Settings().registryKey(KYNTHIA_KEY))
        );
        ENYALIOS = Registry.register(
                Registries.ITEM,
                ENYALIOS_KEY,
                new Enyalios(new Item.Settings().registryKey(ENYALIOS_KEY))
        );
        ASTRAPE = Registry.register(
                Registries.ITEM,
                ASTRAPE_KEY,
                new Astrape(new Item.Settings().registryKey(ASTRAPE_KEY))
        );
        PEITHO = Registry.register(
                Registries.ITEM,
                PEITHO_KEY,
                new Peitho(new Item.Settings().registryKey(PEITHO_KEY)));
        TRIAINA = Registry.register(
                Registries.ITEM,
                TRIAINA_KEY,
                new Triaina(new Item.Settings().registryKey(TRIAINA_KEY)));
        CADUCEUS = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "caduceus"),
                new Caduceus(new Item.Settings().registryKey(CADUCEUS_KEY)));

        PHOEBUS = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "phoebus"),
                new Phoebus(new Item.Settings().registryKey(PHOEBUS_KEY)));
        THYRSUS = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "thyrsus"),
                new Thyrsus(new Item.Settings().registryKey(THYRSUS_KEY)));
        GLACIERA = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "glaciera"),
                new Glaciera(new Item.Settings().registryKey(GLACIERA_KEY)));
        ICARUS_WINGS = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "icarus_wings"),
                new IcarusWings(new Item.Settings().registryKey(ICARUS_WINGS_KEY)));
    }
}