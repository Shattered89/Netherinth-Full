package net.shattered.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.*;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.shattered.rinth.Netherinth;
import net.shattered.rinth.block.ModBlocks;
import net.shattered.rinth.item.ModItems;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.ibm.icu.impl.CurrencyData.provider;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter recipeExporter) {
        List<ItemConvertible> PINK_GARNET_SMELTABLES = List.of(ModItems.RAW_PINK_GARNET, ModBlocks.PINK_GARNET_ORE, ModBlocks.PINK_GARNET_DEEPSLATE_ORE);

        offerSmelting(recipeExporter, PINK_GARNET_SMELTABLES, RecipeCategory.MISC, ModItems.PINK_GARNET, 0.7F, 200, "pink_garnet");
        offerBlasting(recipeExporter, PINK_GARNET_SMELTABLES, RecipeCategory.MISC, ModItems.PINK_GARNET, 0.7F, 100, "pink_garnet");


        offerReversibleCompactingRecipes(recipeExporter, RecipeCategory.BUILDING_BLOCKS, ModItems.PINK_GARNET, RecipeCategory.DECORATIONS, ModBlocks.PINK_GARNET_BLOCK);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.RAW_PINK_GARNET_BLOCK)
                .pattern("RRR")
                .pattern("RRR")
                .pattern("RRR")
                .input('R', ModItems.RAW_PINK_GARNET)
                .criterion(hasItem(ModItems.RAW_PINK_GARNET), conditionsFromItem(ModItems.RAW_PINK_GARNET))
                .offerTo(recipeExporter);


        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.RAW_PINK_GARNET, 9)
                .input(ModBlocks.RAW_PINK_GARNET_BLOCK)
                .criterion(hasItem(ModBlocks.RAW_PINK_GARNET_BLOCK), conditionsFromItem(ModBlocks.RAW_PINK_GARNET_BLOCK))
                .offerTo(recipeExporter, Identifier.of("raw_pink_garnet_from_block"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.HELLFIRE_SWORD)
                .pattern(" S ")
                .pattern(" S ")
                .pattern(" R ")
                .input('S', ModItems.STARLIGHT_ASHES)
                .input('R', Items.STICK)
                .criterion(hasItem(ModItems.STARLIGHT_ASHES), conditionsFromItem(ModItems.STARLIGHT_ASHES))
                .criterion(hasItem(Items.STICK), conditionsFromItem(Items.STICK))
                .offerTo(recipeExporter);

        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), // template
                        Ingredient.ofItems(ModItems.PITCHFORK), // base
                        Ingredient.ofItems(Items.NETHER_STAR), // addition
                        RecipeCategory.COMBAT,
                        ModItems.UPGRADED_TRIDENT) // result
                .criterion(RecipeProvider.hasItem(Items.NETHER_STAR),
                        RecipeProvider.conditionsFromItem(Items.NETHER_STAR))
                .criterion(RecipeProvider.hasItem(ModItems.PITCHFORK),
                        RecipeProvider.conditionsFromItem(ModItems.PITCHFORK))
                .offerTo(recipeExporter, Identifier.of(Netherinth.MOD_ID, "upgraded_trident_smithing"));

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.CONDUIT_HEART_FRAGMENT)
                .pattern(" I ")
                .pattern("I I")
                .pattern(" N ")
                .input('I', Items.IRON_INGOT)
                .input('N', Items.IRON_NUGGET)
                .criterion(hasItem(ModItems.CONDUIT_HEART), conditionsFromItem(ModItems.CONDUIT_HEART))
                .offerTo(recipeExporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.CONDUIT_HEART_SHARD)
                .pattern(" T ")
                .pattern("TST")
                .pattern(" T ")
                .input('T', Items.TURTLE_SCUTE)
                .input('S', Items.HEART_OF_THE_SEA)
                .criterion(hasItem(ModItems.CONDUIT_HEART), conditionsFromItem(ModItems.CONDUIT_HEART))
                .offerTo(recipeExporter);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.CONDUIT_HEART, 1
        ).input(ModItems.CONDUIT_HEART_FRAGMENT
        ).input(ModItems.CONDUIT_HEART_SHARD
        ).criterion(hasItem(ModItems.CONDUIT_HEART_FRAGMENT), conditionsFromItem(ModItems.CONDUIT_HEART_FRAGMENT)
        ).criterion(hasItem(ModItems.CONDUIT_HEART_SHARD), conditionsFromItem(ModItems.CONDUIT_HEART_SHARD)
        ).offerTo(recipeExporter);






    }
}
