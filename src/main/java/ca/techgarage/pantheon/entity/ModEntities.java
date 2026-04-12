package ca.techgarage.pantheon.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final Identifier ASTRAPE_ID = Identifier.fromNamespaceAndPath("pantheon", "astrape");

    public static final ResourceKey<EntityType<?>> ASTRAPE_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            ASTRAPE_ID
    );

    public static final EntityType<AstrapeEntity> ASTRAPE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ASTRAPE_KEY,
            FabricEntityTypeBuilder.<AstrapeEntity>create(MobCategory.MISC, AstrapeEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(10)
                    .build(ASTRAPE_KEY)
    );

    public static void init() {
        PolymerEntityUtils.registerType(ASTRAPE);
    }
}