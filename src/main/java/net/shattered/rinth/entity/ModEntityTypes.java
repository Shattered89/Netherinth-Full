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
    public static final EntityType<CustomTridentEntity> CUSTOM_TRIDENT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Netherinth.MOD_ID, "custom_trident"),
            FabricEntityTypeBuilder.<CustomTridentEntity>create(SpawnGroup.MISC, CustomTridentEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .trackRangeBlocks(4)
                    .trackedUpdateRate(20)
                    .forceTrackedVelocityUpdates(true)
                    .build()
    );

    public static void registerModEntities() {
        System.out.println("Registering Custom Trident Entity Type for " + Netherinth.MOD_ID);
    }
}