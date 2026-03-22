package ca.techgarage.pantheon;

public class PantheonConfig {
    @Comment("Amount of Drachma player can start with [Integer]")
    public static int StartingDrachma = 50;
    @Comment("Amount of Drachma dropper on death [Integer]")
    public static int DroppedDrachmaOnDeath = 5;

    public static boolean diableNetheriteUpgrade = true;

    public static boolean noCobwebSlow = true;
    public static boolean disableDripstoneDamage = false;
    public static double enderPearlCooldown = 30;

    @Comment("Wind Charge Cooldown in seconds")
    public static double windChargeCooldown = 15;
    @Comment("Disable Totem of Undying {true/false}")
    public static boolean disableTotemOfUndying = true;
    @Comment("Time banned in mins default: 60 [Integer]")
    public static int timeBannedFromCombatLogInMinutes = 60;

    public static boolean dropBannedItems = true;

}
