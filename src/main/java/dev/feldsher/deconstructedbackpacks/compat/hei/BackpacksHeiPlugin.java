package dev.feldsher.deconstructedbackpacks.compat.hei;

import dev.feldsher.deconstructedcore.compat.hei.CraftingRecipeTransferHandler;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContainer;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

/**
 * Backpacks in HEI: told apart by their colors, the dye recipe shown by examples, and the upgrades that work as a
 * crafting table, a stonecutter or a smithing table listed with those recipes; crafting recipes go into the crafting upgrade's grid. Found by HEI's annotation scan, so
 * nothing loads it without HEI.
 */
@JEIPlugin
public class BackpacksHeiPlugin implements IModPlugin {
	private static final String FUTURE_MC_STONECUTTER = "container.jei.futuremc.stonecutter.name";
	private static final String FUTURE_MC_SMITHING = "container.jei.futuremc.smithing.name";

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
		ISubtypeRegistry.ISubtypeInterpreter byColors = stack -> {
			IBackpackWrapper wrapper = BackpackWrapper.fromStack(stack);
			return "clothColor:" + wrapper.getMainColor() + ",borderColor:" + wrapper.getAccentColor();
		};
		for (BackpackItem backpack : ModItems.getBackpacks()) {
			subtypeRegistry.registerSubtypeInterpreter(backpack, byColors);
		}
	}

	@Override
	public void register(IModRegistry registry) {
		registry.addRecipes(DyeRecipesMaker.getRecipes(), VanillaRecipeCategoryUid.CRAFTING);

		registry.addRecipeCatalyst(new ItemStack(ModItems.CRAFTING_UPGRADE), VanillaRecipeCategoryUid.CRAFTING);
		registry.getRecipeTransferRegistry().addRecipeTransferHandler(
				new CraftingRecipeTransferHandler<>(BackpackContainer.class, registry.getJeiHelpers().recipeTransferHandlerHelper()), VanillaRecipeCategoryUid.CRAFTING);
		// Future MC's categories, there only with it
		if (Loader.isModLoaded("futuremc")) {
			registry.addRecipeCatalyst(new ItemStack(ModItems.STONECUTTER_UPGRADE), FUTURE_MC_STONECUTTER);
			registry.addRecipeCatalyst(new ItemStack(ModItems.SMITHING_UPGRADE), FUTURE_MC_SMITHING);
		}
	}
}
