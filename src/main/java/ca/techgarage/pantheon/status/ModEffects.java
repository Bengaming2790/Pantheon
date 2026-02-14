package ca.techgarage.pantheon.status;

import ca.techgarage.pantheon.Pantheon;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEffects {

    public static final StatusEffect CONDUCTING_EFFECT =
            new Conducting(StatusEffectCategory.HARMFUL, 0xC5FF00);

    public static final RegistryEntry<StatusEffect> CONDUCTING =
            Registry.registerReference(
                    Registries.STATUS_EFFECT,
                    Identifier.of(Pantheon.MOD_ID, "conducting"),
                    CONDUCTING_EFFECT
            );

    public static void register() {
        Pantheon.log("Registering status effects...");
    }
}
