package dev.feldsher.deconstructedbackpacks.crafting;

import com.google.gson.JsonObject;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * A shaped recipe that turns a backpack into the next tier: the result keeps the backpack's contents and colors,
 * with the new tier's slot counts.
 */
public class BackpackUpgradeRecipe extends ShapedOreRecipe {
	public BackpackUpgradeRecipe(@Nullable ResourceLocation group, ItemStack result, CraftingHelper.ShapedPrimer primer) {
		super(group, result, primer);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack upgradedBackpack = super.getCraftingResult(inv);
		getBackpack(inv).filter(backpack -> backpack.getTagCompound() != null)
				.ifPresent(backpack -> upgradedBackpack.setTagCompound(backpack.getTagCompound().copy()));
		IBackpackWrapper wrapper = BackpackWrapper.fromStack(upgradedBackpack);

		BackpackItem backpackItem = (BackpackItem) upgradedBackpack.getItem();
		wrapper.setSlotNumbers(backpackItem.getNumberOfSlots(), backpackItem.getNumberOfUpgradeSlots());

		return upgradedBackpack;
	}

	private Optional<ItemStack> getBackpack(InventoryCrafting inv) {
		for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
			ItemStack slotStack = inv.getStackInSlot(slot);
			if (slotStack.getItem() instanceof BackpackItem) {
				return Optional.of(slotStack);
			}
		}
		return Optional.empty();
	}

	/** {@code "type": "deconstructedbackpacks:backpack_upgrade"}, the rest as {@code forge:ore_shaped}. */
	public static class Factory implements IRecipeFactory {
		@Override
		public IRecipe parse(JsonContext context, JsonObject json) {
			ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);
			CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
			primer.width = recipe.getRecipeWidth();
			primer.height = recipe.getRecipeHeight();
			primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
			primer.input = recipe.getIngredients();
			String group = JsonUtils.getString(json, "group", "");
			return new BackpackUpgradeRecipe(group.isEmpty() ? null : new ResourceLocation(group), recipe.getRecipeOutput(), primer);
		}
	}
}
