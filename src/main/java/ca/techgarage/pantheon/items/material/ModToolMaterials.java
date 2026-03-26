package ca.techgarage.pantheon.items.material;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public class ModToolMaterials {

    public static final ToolMaterial ASTRAPE_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            455,
            5.0F,
            1.5F,
            22,
            ItemTags.NETHERITE_TOOL_MATERIALS
    );

    public static final ToolMaterial VARATHA_TOOL_MATERIAL = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            455,
            0.0F,
            0.0F,
            22,
            ItemTags.NETHERITE_TOOL_MATERIALS
    );
}