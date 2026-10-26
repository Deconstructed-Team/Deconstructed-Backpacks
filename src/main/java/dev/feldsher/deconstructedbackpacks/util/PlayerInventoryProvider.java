package dev.feldsher.deconstructedbackpacks.util;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The backpacks a player carries, by a slot number: in the player's inventory its own (0-35 the main inventory,
 * 36-39 armor, 40 the offhand); in an inventory a mod adds, as Baubles', {@link #EXTRA_INVENTORY_SLOT_SPAN} on for each,
 * plus the slot there. As in 1.21, the added inventories come first, then the chest slot, the offhand and the main inventory.
 */
public class PlayerInventoryProvider {
	public static final int CHEST_SLOT = 38;
	public static final int OFFHAND_SLOT = 40;
	public static final int EXTRA_INVENTORY_SLOT_SPAN = 1000;

	private static final List<ExtraInventory> EXTRA_INVENTORIES = new ArrayList<>();

	private PlayerInventoryProvider() {}

	/** An inventory a mod adds to players; its first slot is the number this returns. */
	public static int addExtraInventory(ExtraInventory inventory) {
		EXTRA_INVENTORIES.add(inventory);
		return EXTRA_INVENTORIES.size() * EXTRA_INVENTORY_SLOT_SPAN;
	}

	/** Runs on each backpack until the consumer says it's done. */
	public static void runOnBackpacks(EntityPlayer player, BackpackInventorySlotConsumer consumer) {
		runOnBackpacks(player, consumer, false);
	}

	/** With onlyAccessibleByAnotherPlayer, only on those another player could open: the worn ones. */
	public static void runOnBackpacks(EntityPlayer player, BackpackInventorySlotConsumer consumer, boolean onlyAccessibleByAnotherPlayer) {
		for (int i = 0; i < EXTRA_INVENTORIES.size(); i++) {
			ExtraInventory inventory = EXTRA_INVENTORIES.get(i);
			if (onlyAccessibleByAnotherPlayer && !inventory.isAccessibleByAnotherPlayer()) {
				continue;
			}
			int firstSlot = (i + 1) * EXTRA_INVENTORY_SLOT_SPAN;
			int slots = Math.min(inventory.getSlots(player), EXTRA_INVENTORY_SLOT_SPAN);
			for (int slot = 0; slot < slots; slot++) {
				if (acceptIfBackpack(inventory.getStackInSlot(player, slot), firstSlot + slot, consumer)) {
					return;
				}
			}
		}
		if (acceptIfBackpack(player, CHEST_SLOT, consumer) || onlyAccessibleByAnotherPlayer || acceptIfBackpack(player, OFFHAND_SLOT, consumer)) {
			return;
		}
		for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
			if (acceptIfBackpack(player, slot, consumer)) {
				return;
			}
		}
	}

	private static boolean acceptIfBackpack(EntityPlayer player, int slot, BackpackInventorySlotConsumer consumer) {
		return acceptIfBackpack(player.inventory.getStackInSlot(slot), slot, consumer);
	}

	private static boolean acceptIfBackpack(ItemStack stack, int slot, BackpackInventorySlotConsumer consumer) {
		return stack.getItem() instanceof BackpackItem && consumer.accept(stack, slot);
	}

	public static boolean isPlayerInventorySlot(int slot) {
		return slot < EXTRA_INVENTORY_SLOT_SPAN;
	}

	public static ItemStack getStackInSlot(EntityPlayer player, int slot) {
		if (slot < 0) {
			return ItemStack.EMPTY;
		}
		if (isPlayerInventorySlot(slot)) {
			return slot < player.inventory.getSizeInventory() ? player.inventory.getStackInSlot(slot) : ItemStack.EMPTY;
		}
		ExtraInventory inventory = getExtraInventory(slot);
		int extraSlot = slot % EXTRA_INVENTORY_SLOT_SPAN;
		return inventory != null && extraSlot < inventory.getSlots(player) ? inventory.getStackInSlot(player, extraSlot) : ItemStack.EMPTY;
	}

	public static void setStackInSlot(EntityPlayer player, int slot, ItemStack stack) {
		if (slot < 0) {
			return;
		}
		if (isPlayerInventorySlot(slot)) {
			if (slot < player.inventory.getSizeInventory()) {
				player.inventory.setInventorySlotContents(slot, stack);
			}
			return;
		}
		ExtraInventory inventory = getExtraInventory(slot);
		int extraSlot = slot % EXTRA_INVENTORY_SLOT_SPAN;
		if (inventory != null && extraSlot < inventory.getSlots(player)) {
			inventory.setStackInSlot(player, extraSlot, stack);
		}
	}

	/** The backpack drawn on the player's back: the first worn one, in an added inventory, else the chest slot's. */
	public static ItemStack getRenderedBackpack(EntityPlayer player) {
		ItemStack[] rendered = {ItemStack.EMPTY};
		runOnBackpacks(player, (backpack, slot) -> {
			rendered[0] = backpack;
			return true;
		}, true);
		return rendered[0];
	}

	private static ExtraInventory getExtraInventory(int slot) {
		int index = slot / EXTRA_INVENTORY_SLOT_SPAN - 1;
		return index >= 0 && index < EXTRA_INVENTORIES.size() ? EXTRA_INVENTORIES.get(index) : null;
	}

	public interface BackpackInventorySlotConsumer {
		boolean accept(ItemStack backpack, int slot);
	}

	/** Slots a mod adds to players, as Baubles' are; worn ones another player can open, as the chest slot's. */
	public interface ExtraInventory {
		int getSlots(EntityPlayer player);

		ItemStack getStackInSlot(EntityPlayer player, int slot);

		void setStackInSlot(EntityPlayer player, int slot, ItemStack stack);

		boolean isAccessibleByAnotherPlayer();
	}
}
