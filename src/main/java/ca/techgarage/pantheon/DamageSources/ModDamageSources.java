package ca.techgarage.pantheon.DamageSources;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;


public final class ModDamageSources {

    public static final ResourceKey<DamageType> BLEEDING =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    Identifier.fromNamespaceAndPath("pantheon", "bleeding")
            );

    public static final ResourceKey<DamageType> SUN_POISONING =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    Identifier.fromNamespaceAndPath("pantheon", "sun_poisoning")
            );

    private ModDamageSources() {}

    /**
     * Bleeding damage without attacker
     */
    public static DamageSource bleeding(ServerLevel level) {
        return level.damageSources().source(BLEEDING);
    }

    /**
     * Bleeding damage with attacker attribution
     */
    public static DamageSource bleeding(ServerLevel level, @Nullable Entity attacker) {
        return level.damageSources().source(BLEEDING, attacker);
    }

    /**
     * Sun poisoning damage without attacker
     */
    public static DamageSource sunPoisoning(ServerLevel level) {
        return level.damageSources().source(SUN_POISONING);
    }

    /**
     * Sun poisoning damage with attacker attribution
     */
    public static DamageSource sunPoisoning(ServerLevel level, @Nullable Entity attacker) {
        return level.damageSources().source(SUN_POISONING, attacker);
    }
}