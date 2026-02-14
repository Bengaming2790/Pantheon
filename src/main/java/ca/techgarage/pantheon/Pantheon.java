package ca.techgarage.pantheon;

import ca.techgarage.pantheon.api.DashState;
import ca.techgarage.pantheon.api.PeithoTick;
import ca.techgarage.pantheon.items.DrachmaItem;
import ca.techgarage.pantheon.items.ModItems;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class Pantheon implements ModInitializer {
    public static final String MOD_ID = "pantheon";
    public static final boolean isPantheonSMP = true;
    public Logger logger = LoggerFactory.getLogger(MOD_ID);




    @Override
    public void onInitialize() {

        if (isPantheonSMP) {
            logger.info("[Pantheon] Mod Initialized");
        } else {
            logger.error("[Pantheon] Mod failed to initialize properly due to not being the official server.");
            return;
        }



        ModItems.registerModItems();
        logger.info("[Pantheon] Registered Mod Items");

        Optional<?> modContainer = FabricLoader.getInstance().getModContainer(MOD_ID);
        if (modContainer.isEmpty()) {
            logger.error("[Pantheon] Mod container '{}' not found. Check that `fabric.mod.json` contains the matching mod id.", MOD_ID);
            return;
        }

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DashState.tick(server);
        });
        PeithoTick.register();


        PolymerResourcePackUtils.addModAssets(MOD_ID);

        PolymerResourcePackUtils.markAsRequired();

        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(MOD_ID, "items"), PolymerItemGroupUtils.builder()
                .icon(() -> new ItemStack(ModItems.DRACHMA))
                .displayName(Text.translatable("itemGroup.pantheon.items"))
                .entries((context, entries) -> {
                    entries.add(new ItemStack(ModItems.DRACHMA));
                    entries.add(new ItemStack(ModItems.VARATHA));
                    entries.add(new ItemStack(ModItems.ASTRAPE));
                    entries.add(new ItemStack(ModItems.PEITHO));

                }).build()
        );

    }

    public static void log(String message) {
        Logger logger = LoggerFactory.getLogger(MOD_ID);
        logger.info(message);
    }
}