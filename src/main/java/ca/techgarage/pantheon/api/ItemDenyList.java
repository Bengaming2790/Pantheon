package ca.techgarage.pantheon.api;

import net.minecraft.item.Item;

import java.util.HashSet;
import java.util.Set;

public class ItemDenyList {

    private static final Set<Item> DENIED_ITEMS = new HashSet<>();

    public static void deny(Item item) {
        DENIED_ITEMS.add(item);
    }

    public static void deny(Item... items) {
        for (Item item : items) {
            DENIED_ITEMS.add(item);
        }
    }

    public static boolean isDenied(Item item) {
        return DENIED_ITEMS.contains(item);
    }

}
