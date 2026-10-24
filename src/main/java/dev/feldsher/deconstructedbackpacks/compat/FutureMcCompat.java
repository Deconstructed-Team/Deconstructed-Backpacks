package dev.feldsher.deconstructedbackpacks.compat;

import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.registries.IForgeRegistryModifiable;

import java.util.List;

/**
 * Future MC's smithing table makes the netherite backpack, as 1.21's does, from a diamond backpack and any netherite
 * ingot of the ore dictionary; the table keeps the backpack's NBT, and with it its contents. It takes the place of the
 * crafting table recipe.
 * <p>
 * Through reflection, so that Future MC is needed neither to build nor to run.
 */
public final class FutureMcCompat {
	private static final String MOD_ID = "futuremc";
	private static final ResourceLocation SMITHING_TABLE = new ResourceLocation(MOD_ID, "smithing_table");
	private static final String SMITHING_RECIPES = "thedarkcolour.futuremc.recipe.smithing.SmithingRecipes";
	private static final String SMITHING_RECIPE = "thedarkcolour.futuremc.recipe.smithing.SmithingRecipe";

	private FutureMcCompat() {}

	public static void addSmithingRecipes() {
		if (!Config.SERVER.netheriteTierEnabled || !Config.SERVER.futureMcSmithingTable || !Loader.isModLoaded(MOD_ID)) {
			return;
		}
		// Future MC's config may leave the table out
		if (!ForgeRegistries.BLOCKS.containsKey(SMITHING_TABLE)) {
			return;
		}
		ResourceLocation craftingRecipe = DeconstructedBackpacks.getRL("netherite_backpack");
		IForgeRegistryModifiable<IRecipe> recipes = (IForgeRegistryModifiable<IRecipe>) ForgeRegistries.RECIPES;
		// Left without one when no mod adds netherite: then nothing to make it from on the smithing table either
		if (!recipes.containsKey(craftingRecipe)) {
			return;
		}

		try {
			Object smithingRecipes = Class.forName(SMITHING_RECIPES).getField("INSTANCE").get(null);
			@SuppressWarnings("unchecked")
			List<Object> recipeList = (List<Object>) smithingRecipes.getClass().getMethod("getRecipes").invoke(smithingRecipes);
			Object recipe = Class.forName(SMITHING_RECIPE).getConstructor(Ingredient.class, Ingredient.class, ItemStack.class)
					.newInstance(Ingredient.fromItem(ModItems.DIAMOND_BACKPACK), new OreIngredient("ingotNetherite"), new ItemStack(ModItems.NETHERITE_BACKPACK));
			recipeList.add(recipe);
		} catch (ReflectiveOperationException | ClassCastException e) {
			DeconstructedBackpacks.LOGGER.error("Couldn't add the netherite backpack to Future MC's smithing table: it stays on the crafting table", e);
			return;
		}
		recipes.remove(craftingRecipe);
		DeconstructedBackpacks.LOGGER.info("The netherite backpack is made on Future MC's smithing table");
	}
}
