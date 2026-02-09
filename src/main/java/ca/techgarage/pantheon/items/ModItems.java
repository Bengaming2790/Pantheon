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

import java.util.function.Function;

public class ModItems {

    public static DrachmaItem DRACHMA
    public static Varatha VARATHA
    public static Peitho PEITHO;
    public static Astrape ASTRAPE;

    public static void registerItems() {
       DRACHMA = (DrachmaItem) Registry.register(
                Registries.ITEM,
                Identifier.of(Pantheon.MOD_ID, "drachma"),
                new DrachmaItem(
                        new Item.Settings()
                                .registryKey(
                                        RegistryKey.of(
                                                RegistryKeys.ITEM,
                                                Identifier.of(Pantheon.MOD_ID, "drachma")
                                        )
                                )
                )
        );
       VARATHA = (Varatha) Registry.register(
                Registries.ITEM,
                Identifier.of(Pantheon.MOD_ID, "varatha"),
                new Varatha(
                        new Item.Settings()
                                .registryKey(
                                        RegistryKey.of(
                                                RegistryKeys.ITEM,
                                                Identifier.of(Pantheon.MOD_ID, "varatha")
                                        )
                                )
                )
        );

      ASTRAPE = (Astrape) Registry.register(
                Registries.ITEM,
                Identifier.of(Pantheon.MOD_ID, "astrape"),
                new Astrape(
                        new Item.Settings()
                                .registryKey(
                                        RegistryKey.of(
                                                RegistryKeys.ITEM,
                                                Identifier.of(Pantheon.MOD_ID, "astrape")
                                        )
                                )
                )
        );

        PEITHO = (Peitho) Registry.register(
                Registries.ITEM,
                Identifier.of(Pantheon.MOD_ID, "peitho"),
                new Peitho(
                        new Item.Settings()
                                .registryKey(
                                        RegistryKey.of(
                                                RegistryKeys.ITEM,
                                                Identifier.of(Pantheon.MOD_ID, "peitho")
                                        )
                                )
                )
        );

        Registry.register(
                Registries.STATUS_EFFECT, Identifier.of(Pantheon.MOD_ID, "conducting"), new Conducting(StatusEffectCategory.HARMFUL, 0xC5FF00)
        );
    }
}
