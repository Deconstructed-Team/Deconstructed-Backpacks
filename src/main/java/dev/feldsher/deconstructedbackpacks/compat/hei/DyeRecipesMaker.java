package dev.feldsher.deconstructedbackpacks.compat.hei;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import dev.feldsher.deconstructedcore.util.ColorHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Examples of the backpack dye recipe for HEI to show, which it can't from the recipe itself, as it takes any dyes in
 * any places: one per color, and one with two colors each for the cloth and the trim.
 */
public class DyeRecipesMaker {
	private DyeRecipesMaker() {}

	public static List<IRecipe> getRecipes() {
		List<IRecipe> recipes = new ArrayList<>();
		addSingleColorRecipes(recipes);
		addMultipleColorsRecipe(recipes);
		return recipes;
	}

	private static void addMultipleColorsRecipe(List<IRecipe> recipes) {
		ItemStack backpackOutput = new ItemStack(ModItems.BACKPACK);
		int clothColor = ColorHelper.calculateColor(BackpackWrapper.DEFAULT_MAIN_COLOR, BackpackWrapper.DEFAULT_MAIN_COLOR, Arrays.asList(
				EnumDyeColor.YELLOW, EnumDyeColor.LIME
		));
		int trimColor = ColorHelper.calculateColor(BackpackWrapper.DEFAULT_ACCENT_COLOR, BackpackWrapper.DEFAULT_ACCENT_COLOR, Arrays.asList(
				EnumDyeColor.BLUE, EnumDyeColor.BLACK
		));
		BackpackWrapper.fromStack(backpackOutput).setColors(clothColor, trimColor);

		recipes.add(new ShapedOreRecipe(DeconstructedBackpacks.getRL("multiple_colors"), backpackOutput, "YB ", "LUK",
				'Y', ColorHelper.getDyeOreName(EnumDyeColor.YELLOW), 'B', ModItems.BACKPACK, 'L', ColorHelper.getDyeOreName(EnumDyeColor.LIME),
				'U', ColorHelper.getDyeOreName(EnumDyeColor.BLUE), 'K', ColorHelper.getDyeOreName(EnumDyeColor.BLACK)));
	}

	private static void addSingleColorRecipes(List<IRecipe> recipes) {
		for (EnumDyeColor color : EnumDyeColor.values()) {
			ItemStack backpackOutput = new ItemStack(ModItems.BACKPACK);
			BackpackWrapper.fromStack(backpackOutput).setColors(color.getColorValue(), color.getColorValue());
			recipes.add(new ShapedOreRecipe(DeconstructedBackpacks.getRL("single_color_" + color.getName()), backpackOutput, "B", "D",
					'B', ModItems.BACKPACK, 'D', ColorHelper.getDyeOreName(color)));
		}
	}
}
