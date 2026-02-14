package ca.techgarage.pantheon.items;

import ca.techgarage.pantheon.Pantheon;
import ca.techgarage.pantheon.items.weapons.Astrape;
import ca.techgarage.pantheon.items.weapons.Peitho;
import ca.techgarage.pantheon.items.weapons.Varatha;
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

    public static Item DRACHMA;
    public static Item VARATHA;

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
    }
    private static Item register(RegistryKey<Item> key, Item item) {
        return Registry.register(Registries.ITEM, key, item);
    }


    public static final Astrape ASTRAPE = registerItem("astrape",
            new Astrape(createSettings("astrape")));

    public static final Peitho PEITHO = registerItem("peitho",
            new Peitho(createSettings("peitho")));

    // Status Effects
    public static final Conducting CONDUCTING = Registry.register(
            Registries.STATUS_EFFECT,
            Identifier.of(MOD_ID, "conducting"),
            new Conducting(StatusEffectCategory.HARMFUL, 0xC5FF00)
    );

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