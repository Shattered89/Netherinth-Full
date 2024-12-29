package net.shattered.rinth.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shattered.rinth.Netherinth;

public class ModEntityTypes {
    // Changed to BasicTridentEntity which will be a concrete implementation
    public static final EntityType<BasicTridentEntity> CUSTOM_TRIDENT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Netherinth.MOD_ID, "custom_trident"),
            FabricEntityTypeBuilder.<BasicTridentEntity>create(SpawnGroup.MISC, BasicTridentEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .trackRangeBlocks(4)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<UpgradeableTridentEntity> UPGRADEABLE_TRIDENT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Netherinth.MOD_ID, "upgradeable_trident"),
            FabricEntityTypeBuilder.<UpgradeableTridentEntity>create(SpawnGroup.MISC, UpgradeableTridentEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .trackRangeBlocks(4)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static void registerModEntities() {
        System.out.println("Registering entities for " + Netherinth.MOD_ID);
    }
}