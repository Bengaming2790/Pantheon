package ca.techgarage.pantheon.items;

import ca.techgarage.pantheon.Pantheon;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {



    private static SimplePolymerItem registerPolymerItem(String name, Function<SimplePolymerItem.Settings, SimplePolymerItem> function) {
        return Registry.register(Registries.ITEM, Identifier.of(Pantheon.MOD_ID, name),
                function.apply(new SimplePolymerItem.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Pantheon.MOD_ID, name)))));
    }

    public static void registerItems() {
        // Example of registering a simple item
        registerPolymerItem("drachma", SimplePolymerItem::new);
    }
}
