package dev.feldsher.deconstructedbackpacks.util;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * The backpacks a player carries, by their slot in the player's inventory: the main inventory, then the offhand,
 * then the chest slot, as in 1.21's PlayerInventoryProvider. Baubles comes with compat.
 */
public class PlayerInventoryProvider {
	public static final int CHEST_SLOT = 38;
	public static final int OFFHAND_SLOT = 40;

	private PlayerInventoryProvider() {}

	/** Runs on each backpack until the consumer says it's done. */
	public static void runOnBackpacks(EntityPlayer player, BackpackInventorySlotConsumer consumer) {
		for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
			if (acceptIfBackpack(player, slot, consumer)) {
				return;
			}
		}
		if (acceptIfBackpack(player, OFFHAND_SLOT, consumer)) {
			return;
		}
		acceptIfBackpack(player, CHEST_SLOT, consumer);
	}

	private static boolean acceptIfBackpack(EntityPlayer player, int slot, BackpackInventorySlotConsumer consumer) {
		ItemStack stack = player.inventory.getStackInSlot(slot);
		return stack.getItem() instanceof BackpackItem && consumer.accept(stack, slot);
	}

	public interface BackpackInventorySlotConsumer {
		boolean accept(ItemStack backpack, int slot);
	}
}
