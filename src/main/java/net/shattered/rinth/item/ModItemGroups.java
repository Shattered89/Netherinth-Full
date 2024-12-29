package net.shattered.rinth.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shattered.rinth.Netherinth;
import net.shattered.rinth.block.ModBlocks;

public class ModItemGroups {
    public static final ItemGroup PINK_GARNET_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Netherinth.MOD_ID, "pink_garnet_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.PINK_GARNET))
                    .displayName(Text.translatable("itemgroup.rinth.pink_garnet_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.PINK_GARNET);
                        entries.add(ModItems.RAW_PINK_GARNET);


                        entries.add(ModItems.CHISEL);
                        entries.add(ModItems.LAVA_NUGGET);


                        entries.add(ModItems.STARLIGHT_ASHES);

                        entries.add(ModItems.PINK_GARNET_SWORD);
                        entries.add(ModItems.PINK_GARNET_PICKAXE);
                        entries.add(ModItems.PINK_GARNET_SHOVEL);
                        entries.add(ModItems.PINK_GARNET_AXE);
                        entries.add(ModItems.PINK_GARNET_HOE);

                        entries.add(ModItems.HELLFIRE_SWORD);
                        entries.add(ModItems.HELLFIRE_AXE);

                        entries.add(ModItems.QUEEN_AXE);
                        entries.add(ModItems.QUEEN_SWORD);

                        entries.add(ModItems.PINK_GARNET_HAMMER);


                    }).build());

    public static final ItemGroup PINK_GARNET_BLOCKS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Netherinth.MOD_ID, "pink_garnet_blocks"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModBlocks.PINK_GARNET_BLOCK))
                    .displayName(Text.translatable("itemgroup.rinth.pink_garnet_blocks"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.PINK_GARNET_BLOCK);
                        entries.add(ModBlocks.RAW_PINK_GARNET_BLOCK);
                        entries.add(ModBlocks.PINK_GARNET_ORE);
                        entries.add(ModBlocks.PINK_GARNET_DEEPSLATE_ORE);
                        entries.add(ModBlocks.MAGIC_BLOCK);

                        entries.add(ModBlocks.PINK_GARNET_STAIRS);
                        entries.add(ModBlocks.PINK_GARNET_SLAB);

                        entries.add(ModBlocks.PINK_GARNET_BUTTON);
                        entries.add(ModBlocks.PINK_GARNET_PRESSURE_PLATE);

                        entries.add(ModBlocks.PINK_GARNET_FENCE);
                        entries.add(ModBlocks.PINK_GARNET_FENCE_GATE);
                        entries.add(ModBlocks.PINK_GARNET_WALL);

                        entries.add(ModBlocks.PINK_GARNET_DOOR);
                        entries.add(ModBlocks.PINK_GARNET_TRAPDOOR);

                        entries.add(ModBlocks.PINK_GARNET_LAMP);
                    }).build());

    public static final ItemGroup NETHERINTH_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Netherinth.MOD_ID, "netherinth"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.STARLIGHT_ASHES))
                    .displayName(Text.translatable("itemgroup.rinth.netherinth"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.STARLIGHT_ASHES);
                        entries.add(ModItems.LAVA_NUGGET);
                        entries.add(ModItems.PITCHFORK);
                        entries.add(ModItems.UPGRADED_TRIDENT);
                        entries.add(ModItems.CONDUIT_HEART);
                        entries.add(ModItems.POWERED_CONDUIT_HEART);
                        entries.add(ModItems.GOLDEN_RING);
                    }).build());


    public static void registerModItemGroups() {
        Netherinth.LOGGER.info("Registering mod item groups for " + Netherinth.MOD_ID);
    }
}
