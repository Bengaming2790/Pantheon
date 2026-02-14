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


    public static Item DRACHMA;
    public static Item VARATHA;
    public static Item KHALKEOUS;
    public static Item AEGIS;


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

    }
    private static Item register(RegistryKey<Item> key, Item item) {
        return Registry.register(Registries.ITEM, key, item);
    }


    public static final Astrape ASTRAPE = registerItem("astrape",
            new Astrape(createSettings("astrape")));

    public static final Peitho PEITHO = registerItem("peitho",
            new Peitho(createSettings("peitho")));


    private static Item.Settings createSettings(String name) {
        return new Item.Settings()
                .registryKey(RegistryKey.of(
                        RegistryKeys.ITEM,
                        Identifier.of(MOD_ID, name)
                ));
    }

    private static <T extends Item> T registerItem(String name, T item) {
        return Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, name),
                item
        );
    }


    public static void registerItems() {
        Pantheon.log("Registering items...");

    }
}