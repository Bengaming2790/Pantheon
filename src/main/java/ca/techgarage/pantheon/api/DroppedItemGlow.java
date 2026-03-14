package ca.techgarage.pantheon.api;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DroppedItemGlow {

    /**
     * Applies a glow outline to a dropped ItemEntity using the nearest
     * supported Minecraft team color derived from a hex code.
     *
     * @param itemEntity The dropped item entity to glow.
     * @param hexColor   A hex color string, e.g. "#FF5500" or "FF5500"
     */
    public static void applyGlow(ItemEntity itemEntity, String hexColor) {
        if (!(itemEntity.getEntityWorld() instanceof ServerWorld serverWorld)) return;

        Formatting formatting = hexToNearestFormatting(hexColor);
        String teamName = "glow_" + formatting.getName();

        Scoreboard scoreboard = serverWorld.getScoreboard();

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.addTeam(teamName);
            team.setColor(formatting);
            team.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
            team.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.NEVER);
        }

        scoreboard.addScoreHolderToTeam(itemEntity.getNameForScoreboard(), team);
        itemEntity.setGlowing(true);
    }


    public static void removeGlow(ItemEntity itemEntity) {
        if (!(itemEntity.getEntityWorld() instanceof ServerWorld serverWorld)) return;
        itemEntity.setGlowing(false);
        serverWorld.getScoreboard().clearTeam(itemEntity.getNameForScoreboard());
    }

    /**
     * Converts a hex color string to the nearest Minecraft Formatting color.
     */
    public static Formatting hexToNearestFormatting(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        Formatting[] candidates = {
                Formatting.BLACK,       // #000000
                Formatting.DARK_BLUE,   // #0000AA
                Formatting.DARK_GREEN,  // #00AA00
                Formatting.DARK_AQUA,   // #00AAAA
                Formatting.DARK_RED,    // #AA0000
                Formatting.DARK_PURPLE, // #AA00AA
                Formatting.GOLD,        // #FFAA00
                Formatting.GRAY,        // #AAAAAA
                Formatting.DARK_GRAY,   // #555555
                Formatting.BLUE,        // #5555FF
                Formatting.GREEN,       // #55FF55
                Formatting.AQUA,        // #55FFFF
                Formatting.RED,         // #FF5555
                Formatting.LIGHT_PURPLE,// #FF55FF
                Formatting.YELLOW,      // #FFFF55
                Formatting.WHITE        // #FFFFFF
        };

        int[] colorValues = {
                0x000000, 0x0000AA, 0x00AA00, 0x00AAAA,
                0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
                0x555555, 0x5555FF, 0x55FF55, 0x55FFFF,
                0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
        };

        Formatting nearest = Formatting.WHITE;
        double minDist = Double.MAX_VALUE;

        for (int i = 0; i < colorValues.length; i++) {
            int cr = (colorValues[i] >> 16) & 0xFF;
            int cg = (colorValues[i] >> 8)  & 0xFF;
            int cb =  colorValues[i]         & 0xFF;

            // Weighted Euclidean distance
            double dist = Math.pow((r - cr) * 0.299, 2)
                    + Math.pow((g - cg) * 0.587, 2)
                    + Math.pow((b - cb) * 0.114, 2);

            if (dist < minDist) {
                minDist = dist;
                nearest = candidates[i];
            }
        }

        return nearest;
    }
}