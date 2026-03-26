package ca.techgarage.pantheon.status;

import ca.techgarage.pantheon.Pantheon;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ModEffects {

    public static final MobEffect CONDUCTING_EFFECT =
            new Conducting(MobEffectCategory.NEUTRAL, 0xC5FF00);

    public static final Holder<MobEffect> CONDUCTING =
            Registry.registerForHolder(
                    BuiltInRegistries.MOB_EFFECT,
                    Identifier.fromNamespaceAndPath(Pantheon.MOD_ID, "conducting"),
                    CONDUCTING_EFFECT
            );
    public static final MobEffect BLEED_EFFECT =
            new Bleed(MobEffectCategory.HARMFUL, 0xFF2B00);

    public static final Holder<MobEffect>  BLEED =
            Registry.registerForHolder(
                    BuiltInRegistries.MOB_EFFECT,
                    Identifier.fromNamespaceAndPath(Pantheon.MOD_ID, "bleed"),
                    BLEED_EFFECT
            );

    public static final MobEffect DROWSY_EFFECT =
            new Drowsy(MobEffectCategory.NEUTRAL, 0xFF2B00);

    public static final Holder<MobEffect> DROWSY =
            Registry.registerForHolder(
                    BuiltInRegistries.MOB_EFFECT,
                    Identifier.fromNamespaceAndPath(Pantheon.MOD_ID, "drowsy"),
                    DROWSY_EFFECT
            );

    public static final MobEffect SUN_POISONING_EFFECT =
            new SunPoisoning(MobEffectCategory.NEUTRAL, 0xFFAB00);

    public static final Holder<MobEffect> SUN_POISONING =
            Registry.registerForHolder(
                    BuiltInRegistries.MOB_EFFECT,
                    Identifier.fromNamespaceAndPath(Pantheon.MOD_ID, "sun_poisoning"),
                    SUN_POISONING_EFFECT
            );

    public static void register() {
        Pantheon.log("Registering status effects...");
    }
}