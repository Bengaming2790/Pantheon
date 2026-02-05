package ca.techgarage.pantheon;

import ca.techgarage.pantheon.items.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;

public class Pantheon implements ModInitializer {
    Logger logger;
    public static final String MOD_ID = "pantheon";
    @Override
    public void onInitialize() {

        ModItems.registerItems();
        boolean packCreater = PolymerResourcePackUtils.addModAssets(MOD_ID);
        PolymerResourcePackUtils.markAsRequired();

    }


}
