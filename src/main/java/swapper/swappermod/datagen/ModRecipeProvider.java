package swapper.swappermod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.block.Blocks;
import swapper.swappermod.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {

    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new RecipeProvider(registries, output) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.MISC, ModBlocks.SWAPPER_BLOCK)
                        .pattern("SSS")
                        .pattern("SHS")
                        .pattern("SRS")
                        .define('S', Blocks.COBBLESTONE)
                        .define('H', Blocks.HOPPER)
                        .define('R', Blocks.REDSTONE_WIRE)
                        .unlockedBy(getHasName(ModBlocks.SWAPPER_BLOCK), has(ModBlocks.SWAPPER_BLOCK))
                        .group("Swapper")
                        .save(output);

            }
        };
    }

    @Override
    public String getName() {
        return "";
    }
}
