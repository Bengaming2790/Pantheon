package ca.techgarage.pantheon.items.material;

import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;

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
            0f,
            0f,
            22,
            ItemTags.NETHERITE_TOOL_MATERIALS
    );

}
