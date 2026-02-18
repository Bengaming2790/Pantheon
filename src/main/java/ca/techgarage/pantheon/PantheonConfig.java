package ca.techgarage.pantheon;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "pantheon")
public class PantheonConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public int StartingDrachma = 50;

    @ConfigEntry.Gui.Tooltip
    public int DroppedDrachmaOnDeath = 5;

}
