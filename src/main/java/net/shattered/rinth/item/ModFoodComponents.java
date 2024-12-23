package net.shattered.rinth.item;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class ModFoodComponents {
    public static final FoodComponent LAVANUGGET = new FoodComponent.Builder().nutrition(8).saturationModifier(1.1f)
            .statusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 200, 1), .05f).build();
}
