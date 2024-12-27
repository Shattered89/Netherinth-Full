package net.shattered.rinth.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BasicTridentEntity extends CustomTridentEntity {

    public BasicTridentEntity(EntityType<? extends CustomTridentEntity> entityType, World world) {
        super(entityType, world);
    }

    public BasicTridentEntity(World world, LivingEntity owner, ItemStack stack) {
        super(world, owner, stack);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
    }

    @Override
    protected void initDataTracker() {

    }
}