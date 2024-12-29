package net.shattered.rinth;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.shattered.rinth.block.ModBlocks;
import net.shattered.rinth.entity.ModEntityTypes;
import net.shattered.rinth.entity.client.CustomTridentRenderer;
import net.shattered.rinth.entity.client.UpgradeableTridentRenderer;

public class NetherinthClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntityTypes.CUSTOM_TRIDENT, CustomTridentRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.UPGRADEABLE_TRIDENT, UpgradeableTridentRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PINK_GARNET_DOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PINK_GARNET_TRAPDOOR, RenderLayer.getCutout());
    }
}

