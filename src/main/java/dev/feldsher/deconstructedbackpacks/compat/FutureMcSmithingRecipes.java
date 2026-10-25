package dev.feldsher.deconstructedbackpacks.compat;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * The smithing table's recipes, which 1.12 has only from Future MC: an item and a material, without 1.20's templates.
 * Through reflection, so that Future MC is needed neither to build nor to run: without it there are none.
 */
public final class FutureMcSmithingRecipes {
	private static final String MOD_ID = "futuremc";
	private static final String SMITHING_RECIPES = "thedarkcolour.futuremc.recipe.smithing.SmithingRecipes";
	private static final String SIMPLE_RECIPE = "thedarkcolour.futuremc.recipe.SimpleRecipe";
	private static final ResourceLocation USE_SOUND = new ResourceLocation(MOD_ID, "smithing_table_use");

	private static boolean initialized = false;
	@Nullable
	private static Object recipesInstance = null;
	@Nullable
	private static Method getRecipe = null;
	@Nullable
	private static Method getOutput = null;

	private FutureMcSmithingRecipes() {}

	private static void init() {
		if (initialized) {
			return;
		}
		initialized = true;
		if (!Loader.isModLoaded(MOD_ID)) {
			return;
		}
		try {
			Class<?> recipesClass = Class.forName(SMITHING_RECIPES);
			recipesInstance = recipesClass.getField("INSTANCE").get(null);
			getRecipe = recipesClass.getMethod("getRecipe", ItemStack.class, ItemStack.class);
			getOutput = Class.forName(SIMPLE_RECIPE).getMethod("getOutput");
		}
		catch (ReflectiveOperationException e) {
			DeconstructedBackpacks.LOGGER.error("Couldn't read Future MC's smithing recipes: the smithing upgrade will have none", e);
			recipesInstance = null;
		}
	}

	/**
	 * What the smithing table makes of the item with the material, as Future MC's does: the item's NBT and damage
	 * carried over to it.
	 */
	public static ItemStack getResult(ItemStack input, ItemStack material) {
		init();
		if (recipesInstance == null || getRecipe == null || getOutput == null || input.isEmpty() || material.isEmpty()) {
			return ItemStack.EMPTY;
		}
		try {
			Object recipe = getRecipe.invoke(recipesInstance, input, material);
			if (recipe == null) {
				return ItemStack.EMPTY;
			}
			ItemStack result = ((ItemStack) getOutput.invoke(recipe)).copy();
			result.setTagCompound(input.getTagCompound() == null ? null : input.getTagCompound().copy());
			if (input.isItemStackDamageable() && result.isItemStackDamageable()) {
				result.setItemDamage(input.getItemDamage());
			}
			return result;
		}
		catch (ReflectiveOperationException | ClassCastException e) {
			DeconstructedBackpacks.LOGGER.debug("Couldn't get Future MC's smithing recipe for {} and {}", input, material, e);
			return ItemStack.EMPTY;
		}
	}

	@Nullable
	public static SoundEvent getUseSound() {
		return ForgeRegistries.SOUND_EVENTS.getValue(USE_SOUND);
	}
}
