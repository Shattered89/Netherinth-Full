package net.shattered.rinth.entity.client;

import net.minecraft.util.Identifier;
import net.shattered.rinth.entity.Tracker;

public interface TridentRenderer {
    Identifier getTexture(Tracker entity);
}
