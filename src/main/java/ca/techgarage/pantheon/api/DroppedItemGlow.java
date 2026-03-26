package ca.techgarage.pantheon.api;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class DroppedItemGlow {

    /**
     * Applies a glow outline to a dropped ItemEntity using the nearest
     * supported Minecraft team color derived from a hex code.
     *
     * @param itemEntity The dropped item entity to glow.
     * @param hexColor   A hex color string, e.g. "#FF5500" or "FF5500"
     */
    public static void applyGlow(ItemEntity itemEntity, String hexColor) {
        if (!(itemEntity.level() instanceof ServerLevel serverWorld)) return;

        ChatFormatting formatting = hexToNearestFormatting(hexColor);
        String teamName = "glow_" + formatting.getName();

        Scoreboard scoreboard = serverWorld.getScoreboard();

        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setColor(formatting);
            team.setCollisionRule(Team.CollisionRule.NEVER);
            team.setNameTagVisibility(Team.Visibility.NEVER);
        }

        scoreboard.addPlayerToTeam(itemEntity.getScoreboardName(), team);
        itemEntity.setGlowingTag(true);
    }

    public static void removeGlow(ItemEntity itemEntity) {
        if (!(itemEntity.level() instanceof ServerLevel serverWorld)) return;
        itemEntity.setGlowingTag(false);
        serverWorld.getScoreboard().removePlayerFromTeam(itemEntity.getScoreboardName());
    }

    /**
     * Converts a hex color string to the nearest Minecraft ChatFormatting color.
     */
    public static ChatFormatting hexToNearestFormatting(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        ChatFormatting[] candidates = {
                ChatFormatting.BLACK,
                ChatFormatting.DARK_BLUE,
                ChatFormatting.DARK_GREEN,
                ChatFormatting.DARK_AQUA,
                ChatFormatting.DARK_RED,
                ChatFormatting.DARK_PURPLE,
                ChatFormatting.GOLD,
                ChatFormatting.GRAY,
                ChatFormatting.DARK_GRAY,
                ChatFormatting.BLUE,
                ChatFormatting.GREEN,
                ChatFormatting.AQUA,
                ChatFormatting.RED,
                ChatFormatting.LIGHT_PURPLE,
                ChatFormatting.YELLOW,
                ChatFormatting.WHITE
        };

        int[] colorValues = {
                0x000000, 0x0000AA, 0x00AA00, 0x00AAAA,
                0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
                0x555555, 0x5555FF, 0x55FF55, 0x55FFFF,
                0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
        };

        ChatFormatting nearest = ChatFormatting.WHITE;
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