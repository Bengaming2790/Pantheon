package ca.techgarage.pantheon;

import ca.techgarage.pantheon.api.DashState;
import ca.techgarage.pantheon.api.PeithoTick;
import ca.techgarage.pantheon.items.ModItems;
import ca.techgarage.pantheon.items.weapons.Peitho;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;

import java.util.Optional;

public class Pantheon implements ModInitializer {
    public static final String MOD_ID = "pantheon";
    public static final boolean isPantheonSMP = true;
    public Logger logger = LoggerFactory.getLogger(MOD_ID);;

    @Override
    public void onInitialize() {



        if (isPantheonSMP || FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            logger.info("[Pantheon] Mod Initialized");
        } else {
            logger.error("[Pantheon] Mod failed to initialize properly due to not being the official server.");
            return;
        }

        ModItems.registerItems();
        logger.info("[Pantheon] Registered Mod Items");
        Optional<?> modContainer = FabricLoader.getInstance().getModContainer(MOD_ID);
        if (modContainer.isEmpty()) {
            logger.error("[Pantheon] Mod container '{}' not found. Check that `fabric.mod.json` contains the matching mod id.", MOD_ID);
            return;
        }
        PolymerResourcePackUtils.addModAssets(MOD_ID);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            DashState.tick(server);
        });
        PeithoTick.register();

    }

    public static void log(String message) {
        Logger logger = LoggerFactory.getLogger(MOD_ID);
        logger.info(message);
    }
}
