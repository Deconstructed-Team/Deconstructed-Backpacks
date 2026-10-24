package dev.feldsher.deconstructedbackpacks.crafting;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryModifiable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Recipes made of metals vanilla 1.12 doesn't have, copper and netherite, which come from other mods through the ore
 * dictionary. In a pack where no mod adds one, the recipes that need it are taken out once every mod has registered
 * its ores, and the items are left for the pack to give recipes of its own.
 */
public final class OreDependentRecipes {
	private static final Map<String, String> RECIPE_ORES = new LinkedHashMap<>();

	static {
		RECIPE_ORES.put("copper_backpack", "ingotCopper");
		RECIPE_ORES.put("stack_upgrade_starter_tier", "blockCopper");
		RECIPE_ORES.put("netherite_backpack", "ingotNetherite");
		RECIPE_ORES.put("stack_upgrade_tier_4", "blockNetherite");
	}

	private OreDependentRecipes() {}

	public static void removeRecipesWithoutOres() {
		IForgeRegistryModifiable<IRecipe> recipes = (IForgeRegistryModifiable<IRecipe>) ForgeRegistries.RECIPES;
		for (Map.Entry<String, String> entry : RECIPE_ORES.entrySet()) {
			ResourceLocation recipeName = DeconstructedBackpacks.getRL(entry.getKey());
			String ore = entry.getValue();
			if (recipes.containsKey(recipeName) && OreDictionary.getOres(ore, false).isEmpty()) {
				recipes.remove(recipeName);
				DeconstructedBackpacks.LOGGER.warn("No mod adds {} to the ore dictionary: {} is left without a recipe", ore, recipeName);
			}
		}
	}
}
