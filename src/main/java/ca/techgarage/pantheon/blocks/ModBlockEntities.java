package ca.techgarage.pantheon.blocks;

import ca.techgarage.pantheon.blocks.altar.AltarBlockEntity;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class ModBlockEntities {

    public static BlockEntityType<AltarBlockEntity> ALTAR;

    public static void register() {
        BlockEntityType<AltarBlockEntity> ALTAR = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath("pantheon", "altar"),
                FabricBlockEntityTypeBuilder.create(
                        AltarBlockEntity::new,
                        ModAltarBlocks.FIRE_ALTAR,
                        ModAltarBlocks.KHALKEUS_ALTAR,
                        ModAltarBlocks.AEGIS_ALTAR
                ).build()
        );

        PolymerBlockUtils.registerBlockEntity(ALTAR);
    }
}