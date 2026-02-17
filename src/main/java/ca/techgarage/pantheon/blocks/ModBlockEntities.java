package ca.techgarage.pantheon.blocks;

import ca.techgarage.pantheon.altar.AltarBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static BlockEntityType<AltarBlockEntity> ALTAR;

    public static void register() {
        ALTAR = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of("pantheon", "altar"),
                FabricBlockEntityTypeBuilder.create(
                        AltarBlockEntity::new,
                        ModAltarBlocks.FIRE_ALTAR
                ).build()
        );
    }
}


