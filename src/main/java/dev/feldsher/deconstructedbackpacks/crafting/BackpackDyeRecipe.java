package dev.feldsher.deconstructedbackpacks.crafting;

import com.google.gson.JsonObject;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedcore.crafting.StorageDyeRecipeBase;
import dev.feldsher.deconstructedcore.util.ColorHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.List;

/** Dyes a backpack: its cloth by the dyes left of it and in its column, its trim by those right of it and in its column. */
public class BackpackDyeRecipe extends StorageDyeRecipeBase {
	@Override
	protected boolean isDyeableStorageItem(ItemStack stack) {
		return stack.getItem() instanceof BackpackItem;
	}

	@Override
	protected void applyColors(ItemStack coloredStorage, List<EnumDyeColor> mainDyes, List<EnumDyeColor> trimDyes) {
		IBackpackWrapper coloredWrapper = BackpackWrapper.fromStack(coloredStorage);
		coloredWrapper.setColors(ColorHelper.calculateColor(coloredWrapper.getMainColor(), BackpackWrapper.DEFAULT_MAIN_COLOR, mainDyes),
				ColorHelper.calculateColor(coloredWrapper.getAccentColor(), BackpackWrapper.DEFAULT_ACCENT_COLOR, trimDyes));
	}

	/** {@code "type": "deconstructedbackpacks:backpack_dye"}, nothing else. */
	public static class Factory implements IRecipeFactory {
		@Override
		public IRecipe parse(JsonContext context, JsonObject json) {
			return new BackpackDyeRecipe();
		}
	}
}
