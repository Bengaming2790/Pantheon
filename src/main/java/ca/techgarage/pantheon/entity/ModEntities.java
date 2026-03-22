package ca.techgarage.pantheon.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final RegistryKey<EntityType<?>> ASTRAPE_KEY =
            RegistryKey.of(Registries.ENTITY_TYPE.getKey(), Identifier.of("pantheon", "astrape"));
    public static final EntityType<AstrapeEntity> ASTRAPE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of("pantheon", "astrape"),
            FabricEntityTypeBuilder.<AstrapeEntity>create(SpawnGroup.MISC, AstrapeEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(10)
                    .build(ASTRAPE_KEY)
    );

    public static void init() {PolymerEntityUtils.registerType(ASTRAPE);}
}