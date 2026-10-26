package dev.feldsher.deconstructedbackpacks.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.container.AEBaseContainer;
import appeng.util.Platform;
import dev.feldsher.deconstructedcore.api.IStashStorageItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

/**
 * Takes an item from the network of the open terminal into the held backpack, as the terminal's own shift click
 * takes it into the player's inventory: with the terminal's power and as the player, so the network's security
 * holds. What the backpack doesn't take after all goes back.
 */
final class NetworkStash {
	private NetworkStash() {}

	static void stash(EntityPlayerMP player, int windowId, ItemStack item, boolean asMuchAsFits) {
		if (!(player.openContainer instanceof AEBaseContainer) || player.openContainer.windowId != windowId || item.isEmpty()) {
			return;
		}
		ItemStack held = player.inventory.getItemStack();
		if (held.getCount() != 1 || !(held.getItem() instanceof IStashStorageItem)) {
			return;
		}
		AEBaseContainer container = (AEBaseContainer) player.openContainer;
		IEnergySource power = container.getPowerSource();
		MEStorage storage = container.getCellInventory();
		AEItemKey key = AEItemKey.of(item);
		if (power == null || storage == null || key == null) {
			return;
		}
		IStashStorageItem storageItem = (IStashStorageItem) held.getItem();

		long available = Platform.poweredExtraction(power, storage, key, asMuchAsFits ? Integer.MAX_VALUE : key.getMaxStackSize(), container.getActionSource(), Actionable.SIMULATE);
		if (available <= 0) {
			return;
		}
		int toTake = (int) Math.min(available, Integer.MAX_VALUE);
		toTake -= storageItem.stash(held, key.toStack(toTake), true).getCount();
		if (toTake <= 0) {
			return;
		}
		long extracted = Platform.poweredExtraction(power, storage, key, toTake, container.getActionSource());
		if (extracted <= 0) {
			return;
		}
		ItemStack notStashed = storageItem.stash(held, key.toStack((int) extracted), false);
		if (!notStashed.isEmpty()) {
			long returned = Platform.poweredInsert(power, storage, key, notStashed.getCount(), container.getActionSource());
			int left = notStashed.getCount() - (int) returned;
			if (left > 0) {
				ItemStack leftover = key.toStack(left);
				if (!player.inventory.addItemStackToInventory(leftover)) {
					player.dropItem(leftover, false);
				}
			}
		}
		storageItem.onStashed(player, held);
		player.updateHeldItem();
	}
}
