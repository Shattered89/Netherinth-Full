package net.shattered.rinth.event;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.biome.Biome;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.HugeMushroomFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class NetherSpreadEvent {
    private static final int RADIUS = 50;

    public static void spread(ServerWorld world, BlockPos portalPos) {
        Random random = world.getRandom();

        // First convert biomes
        convertBiomes(world, portalPos);

        // Then handle water to lava conversion
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    BlockPos currentPos = portalPos.add(x, y, z);

                    if (portalPos.getSquaredDistance(currentPos) <= RADIUS * RADIUS) {
                        BlockState state = world.getBlockState(currentPos);
                        if (state.isOf(Blocks.WATER)) {
                            world.setBlockState(currentPos, Blocks.LAVA.getDefaultState());
                        }
                    }
                }
            }
        }

        // Finally transform blocks
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -RADIUS; y <= RADIUS; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    BlockPos currentPos = portalPos.add(x, y, z);

                    if (portalPos.getSquaredDistance(currentPos) <= RADIUS * RADIUS) {
                        transformBlock(world, currentPos, random);
                    }
                }
            }
        }
    }

    private static void transformBlock(ServerWorld world, BlockPos pos, Random random) {
        BlockState currentState = world.getBlockState(pos);

        if (currentState.isAir() ||
                currentState.isOf(Blocks.NETHER_PORTAL) ||
                isNetherBlock(currentState.getBlock())) {
            return;
        }

        // Check if this is a tree base
        if (isWoodType(currentState.getBlock()) && isTreeBase(world, pos)) {
            replaceWithNetherTree(world, pos, random);
            return;
        }

        // Only transform if not part of a tree that will be replaced
        if (!isPartOfTree(world, pos)) {
            RegistryEntry<Biome> biome = world.getBiome(pos);
            BlockState netherVariant = getNetherVariant(currentState, biome, random);
            if (netherVariant != null) {
                world.setBlockState(pos, netherVariant);
                if (random.nextFloat() < 0.1f) {
                    world.spawnParticles(ParticleTypes.FLAME,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    private static boolean isTreeBase(ServerWorld world, BlockPos pos) {
        Block below = world.getBlockState(pos.down()).getBlock();
        return (below == Blocks.GRASS_BLOCK || below == Blocks.DIRT) &&
                isConnectedToLeaves(world, pos);
    }

    private static boolean isConnectedToLeaves(ServerWorld world, BlockPos start) {
        BlockPos.Mutable checkPos = start.mutableCopy();
        for(int y = 0; y < 15; y++) {
            checkPos.move(0, 1, 0);
            BlockState state = world.getBlockState(checkPos);
            if(isLeaves(state.getBlock())) return true;
            if(!isWoodType(state.getBlock())) break;
        }
        return false;
    }

    private static boolean isPartOfTree(ServerWorld world, BlockPos pos) {
        if(isWoodType(world.getBlockState(pos).getBlock()) ||
                isLeaves(world.getBlockState(pos).getBlock())) {
            BlockPos.Mutable checkPos = pos.mutableCopy();
            for(int y = 0; y > -15; y--) {
                checkPos.move(0, -1, 0);
                Block below = world.getBlockState(checkPos).getBlock();
                if(below == Blocks.GRASS_BLOCK || below == Blocks.DIRT) {
                    return isWoodType(world.getBlockState(checkPos.up()).getBlock());
                }
                if(!isWoodType(below)) break;
            }
        }
        return false;
    }

    private static void replaceWithNetherTree(ServerWorld world, BlockPos pos, Random random) {
        clearExistingTree(world, pos);

        RegistryEntry<Biome> biome = world.getBiome(pos);
        boolean isWarped = biome.matchesKey(BiomeKeys.WARPED_FOREST);

        if (isWarped) {
            world.setBlockState(pos.down(), Blocks.WARPED_NYLIUM.getDefaultState());
            generateWarpedTree(world, pos, random);
        } else {
            world.setBlockState(pos.down(), Blocks.CRIMSON_NYLIUM.getDefaultState());
            generateCrimsonTree(world, pos, random);
        }
    }

    private static void clearExistingTree(ServerWorld world, BlockPos start) {
        int radius = 4;
        int height = 15;

        for(int x = -radius; x <= radius; x++) {
            for(int y = 0; y <= height; y++) {
                for(int z = -radius; z <= radius; z++) {
                    BlockPos pos = start.add(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if(isWoodType(state.getBlock()) || isLeaves(state.getBlock())) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    private static void generateWarpedTree(ServerWorld world, BlockPos pos, Random random) {
        // Generate stem
        int height = 4 + random.nextInt(3);  // Height between 4-6 blocks
        for(int y = 0; y < height; y++) {
            world.setBlockState(pos.up(y), Blocks.WARPED_STEM.getDefaultState());
        }

        // Generate wart blocks for canopy
        for(int x = -2; x <= 2; x++) {
            for(int z = -2; z <= 2; z++) {
                if(Math.abs(x) == 2 && Math.abs(z) == 2) continue; // Skip corners
                for(int y = height-2; y <= height; y++) {
                    if(random.nextFloat() < 0.8f) { // 80% chance to place a block
                        world.setBlockState(pos.add(x, y, z), Blocks.WARPED_WART_BLOCK.getDefaultState());
                    }
                }
            }
        }

        // Add Shroomlights
        int shroomlights = 1 + random.nextInt(3);
        for(int i = 0; i < shroomlights; i++) {
            int y = height - 1 - random.nextInt(2);
            int x = -1 + random.nextInt(3);
            int z = -1 + random.nextInt(3);
            world.setBlockState(pos.add(x, y, z), Blocks.SHROOMLIGHT.getDefaultState());
        }
    }

    private static void generateCrimsonTree(ServerWorld world, BlockPos pos, Random random) {
        // Generate stem
        int height = 4 + random.nextInt(3);  // Height between 4-6 blocks
        for(int y = 0; y < height; y++) {
            world.setBlockState(pos.up(y), Blocks.CRIMSON_STEM.getDefaultState());
        }

        // Generate wart blocks for canopy
        for(int x = -2; x <= 2; x++) {
            for(int z = -2; z <= 2; z++) {
                if(Math.abs(x) == 2 && Math.abs(z) == 2) continue; // Skip corners
                for(int y = height-2; y <= height; y++) {
                    if(random.nextFloat() < 0.8f) { // 80% chance to place a block
                        world.setBlockState(pos.add(x, y, z), Blocks.NETHER_WART_BLOCK.getDefaultState());
                    }
                }
            }
        }

        // Add Shroomlights
        int shroomlights = 1 + random.nextInt(3);
        for(int i = 0; i < shroomlights; i++) {
            int y = height - 1 - random.nextInt(2);
            int x = -1 + random.nextInt(3);
            int z = -1 + random.nextInt(3);
            world.setBlockState(pos.add(x, y, z), Blocks.SHROOMLIGHT.getDefaultState());
        }

        // Add some vines
        if(random.nextFloat() < 0.6f) { // 60% chance for vines
            for(int i = 0; i < 3; i++) {
                int x = -2 + random.nextInt(5);
                int z = -2 + random.nextInt(5);
                int vineLength = 1 + random.nextInt(3);
                for(int y = height-1; y > height-1-vineLength; y--) {
                    world.setBlockState(pos.add(x, y, z), Blocks.WEEPING_VINES.getDefaultState());
                }
            }
        }
    }

    private static boolean isNetherBlock(Block block) {
        return block == Blocks.NETHERRACK ||
                block == Blocks.CRIMSON_NYLIUM ||
                block == Blocks.WARPED_NYLIUM ||
                block == Blocks.SOUL_SAND ||
                block == Blocks.SOUL_SOIL ||
                block == Blocks.MAGMA_BLOCK ||
                block == Blocks.BLACKSTONE ||
                block == Blocks.BASALT ||
                block == Blocks.LAVA ||
                block == Blocks.NETHER_WART_BLOCK ||
                block == Blocks.WARPED_WART_BLOCK ||
                block == Blocks.NETHER_BRICKS ||
                block == Blocks.BONE_BLOCK;
    }

    private static BlockState getNetherVariant(BlockState original, RegistryEntry<Biome> biome, Random random) {
        Block block = original.getBlock();

        // Crimson Forest conversions
        if (biome.matchesKey(BiomeKeys.CRIMSON_FOREST)) {
            if (block == Blocks.GRASS_BLOCK) return Blocks.CRIMSON_NYLIUM.getDefaultState();
            if (block == Blocks.DIRT) return Blocks.NETHERRACK.getDefaultState();
            if (block == Blocks.STONE) return Blocks.NETHERRACK.getDefaultState();
            if (block == Blocks.SHORT_GRASS) return Blocks.CRIMSON_ROOTS.getDefaultState();
            if (block == Blocks.VINE) return Blocks.WEEPING_VINES.getDefaultState();
            if (block == Blocks.DIRT_PATH || block == Blocks.FARMLAND) return Blocks.CRIMSON_NYLIUM.getDefaultState();
        }
        // Warped Forest conversions
        else if (biome.matchesKey(BiomeKeys.WARPED_FOREST)) {
            if (block == Blocks.GRASS_BLOCK) return Blocks.WARPED_NYLIUM.getDefaultState();
            if (block == Blocks.DIRT) return Blocks.NETHERRACK.getDefaultState();
            if (block == Blocks.STONE) return Blocks.NETHERRACK.getDefaultState();
            if (block == Blocks.SHORT_GRASS) return Blocks.WARPED_ROOTS.getDefaultState();
            if (block == Blocks.VINE) return Blocks.TWISTING_VINES.getDefaultState();
            if (block == Blocks.DIRT_PATH || block == Blocks.FARMLAND) return Blocks.WARPED_NYLIUM.getDefaultState();
        }
        // Soul Sand Valley conversions
        else if (biome.matchesKey(BiomeKeys.SOUL_SAND_VALLEY)) {
            if (block == Blocks.SAND || block == Blocks.RED_SAND) return Blocks.SOUL_SAND.getDefaultState();
            if (block == Blocks.SANDSTONE) return Blocks.SOUL_SOIL.getDefaultState();
            if (block == Blocks.STONE) return Blocks.BASALT.getDefaultState();
            if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK) return Blocks.SOUL_SOIL.getDefaultState();
            if (isWoodType(block)) return Blocks.BONE_BLOCK.getDefaultState();
            if (isLeaves(block)) return Blocks.SOUL_SAND.getDefaultState();
            if (block == Blocks.DIRT_PATH || block == Blocks.FARMLAND) return Blocks.SOUL_SOIL.getDefaultState();
        }
        // Basalt Deltas conversions
        else if (biome.matchesKey(BiomeKeys.BASALT_DELTAS)) {
            if (block == Blocks.STONE) return Blocks.BASALT.getDefaultState();
            if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK) return Blocks.BLACKSTONE.getDefaultState();
            if (isWoodType(block)) return Blocks.BASALT.getDefaultState();
            if (isLeaves(block)) return Blocks.MAGMA_BLOCK.getDefaultState();
            if (block == Blocks.GRAVEL) return Blocks.BLACKSTONE.getDefaultState();
            if (block == Blocks.COBBLESTONE) return Blocks.BLACKSTONE.getDefaultState();
            if (block == Blocks.DIRT_PATH || block == Blocks.FARMLAND) return Blocks.BASALT.getDefaultState();
        }

        // Default conversions (Nether Wastes)
        if (block == Blocks.STONE) return Blocks.NETHERRACK.getDefaultState();
        if (block == Blocks.GRASS_BLOCK) return Blocks.NETHERRACK.getDefaultState();
        if (block == Blocks.DIRT) return Blocks.SOUL_SOIL.getDefaultState();
        if (block == Blocks.SAND) return Blocks.SOUL_SAND.getDefaultState();
        if (block == Blocks.RED_SAND) return Blocks.MAGMA_BLOCK.getDefaultState();
        if (block == Blocks.STONE_BRICKS) return Blocks.NETHER_BRICKS.getDefaultState();
        if (block == Blocks.COBBLESTONE) return Blocks.BLACKSTONE.getDefaultState();
        if (block == Blocks.GRAVEL) return Blocks.SOUL_SOIL.getDefaultState();
        if (block == Blocks.DIRT_PATH || block == Blocks.FARMLAND) return Blocks.NETHERRACK.getDefaultState();

        return null;
    }

    private static boolean isWoodType(Block block) {
        return block == Blocks.OAK_LOG ||
                block == Blocks.BIRCH_LOG ||
                block == Blocks.SPRUCE_LOG ||
                block == Blocks.JUNGLE_LOG ||
                block == Blocks.ACACIA_LOG ||
                block == Blocks.DARK_OAK_LOG ||
                block == Blocks.CHERRY_LOG ||
                block == Blocks.MANGROVE_LOG ||
                block == Blocks.BAMBOO ||
                block == Blocks.BAMBOO_BLOCK;
    }

    private static boolean isLeaves(Block block) {
        return block == Blocks.OAK_LEAVES ||
                block == Blocks.BIRCH_LEAVES ||
                block == Blocks.SPRUCE_LEAVES ||
                block == Blocks.JUNGLE_LEAVES ||
                block == Blocks.ACACIA_LEAVES ||
                block == Blocks.DARK_OAK_LEAVES ||
                block == Blocks.CHERRY_LEAVES ||
                block == Blocks.MANGROVE_LEAVES;
    }

    // Temporarily keeping the old biome conversion method until we implement the new one
    private static void convertBiomes(ServerWorld world, BlockPos center) {
        // We'll implement the new biome conversion here
    }
}