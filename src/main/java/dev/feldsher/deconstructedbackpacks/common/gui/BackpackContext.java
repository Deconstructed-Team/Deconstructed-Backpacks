package dev.feldsher.deconstructedbackpacks.common.gui;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Where an open backpack is. Block, sub-backpack and another player's backpack contexts come with the stages that port them.
 */
public abstract class BackpackContext {

	public abstract boolean shouldLockBackpackSlot(EntityPlayer player);

	public abstract IBackpackWrapper getBackpackWrapper(EntityPlayer player);

	public abstract int getBackpackSlotIndex();

	public abstract boolean canInteractWith(EntityPlayer player);

	/** A backpack in the player's inventory, by its slot there: 0-35 the main inventory, 36-39 armor, 40 the offhand. */
	public static class Item extends BackpackContext {
		private final int backpackSlotIndex;

		public Item(int backpackSlotIndex) {
			this.backpackSlotIndex = backpackSlotIndex;
		}

		@Override
		public boolean shouldLockBackpackSlot(EntityPlayer player) {
			// The container shows the main inventory alone
			return backpackSlotIndex < player.inventory.mainInventory.size();
		}

		@Override
		public IBackpackWrapper getBackpackWrapper(EntityPlayer player) {
			return BackpackWrapper.fromStack(getBackpackStack(player));
		}

		private ItemStack getBackpackStack(EntityPlayer player) {
			return player.inventory.getStackInSlot(backpackSlotIndex);
		}

		@Override
		public int getBackpackSlotIndex() {
			return backpackSlotIndex;
		}

		@Override
		public boolean canInteractWith(EntityPlayer player) {
			return getBackpackStack(player).getItem() instanceof BackpackItem;
		}
	}
}
