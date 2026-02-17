package ca.techgarage.pantheon.DamageSources;


import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class ModDamageSources {

    public static final RegistryKey<DamageType> BLEEDING =
            RegistryKey.of(
                    RegistryKeys.DAMAGE_TYPE,
                    Identifier.of("pantheon", "bleeding")
            );

    private ModDamageSources() {}

    /**
     * Bleeding damage without attacker
     */
    public static DamageSource bleeding(ServerWorld world) {
        return world.getDamageSources().create(BLEEDING);
    }

    /**
     * Bleeding damage with attacker attribution
     */
    public static DamageSource bleeding(ServerWorld world, Entity attacker) {
        return world.getDamageSources().create(BLEEDING, attacker);
    }

    public static final RegistryKey<DamageType> SUN_POISONING  =
            RegistryKey.of(
                    RegistryKeys.DAMAGE_TYPE,
                    Identifier.of("pantheon", "sun_poisoning")
            );

    /**
     * Bleeding damage without attacker
     */
    public static DamageSource sunPoisoning(ServerWorld world) {
        return world.getDamageSources().create(SUN_POISONING);
    }

    /**
     * Bleeding damage with attacker attribution
     */
    public static DamageSource sunPoisoning(ServerWorld world, Entity attacker) {
        return world.getDamageSources().create(SUN_POISONING, attacker);
    }

}
