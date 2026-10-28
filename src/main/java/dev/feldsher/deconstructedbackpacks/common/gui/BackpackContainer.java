package dev.feldsher.deconstructedbackpacks.common.gui;

import dev.feldsher.deconstructedbackpacks.backpack.UUIDDeduplicator;
import net.minecraft.item.ItemStack;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackAccessLogger;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedcore.common.gui.StorageContainerMenuBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class BackpackContainer extends StorageContainerMenuBase<IBackpackWrapper> {
	private final BackpackContext backpackContext;

	public BackpackContainer(EntityPlayer player, BackpackContext backpackContext) {
		super(player, backpackContext.getBackpackWrapper(player), backpackContext.getParentBackpackWrapper(player).orElse(null),
				backpackContext.getBackpackSlotIndex(), backpackContext.shouldLockBackpackSlot(player));
		this.backpackContext = backpackContext;

		storageWrapper.getContentsUuid().ifPresent(backpackUuid -> {
			ItemStack backpack = storageWrapper.getBackpack();
			BackpackAccessLogger.logPlayerAccess(player, backpack.getItem(), backpackUuid, backpack.getDisplayName(),
					storageWrapper.getMainColor(), storageWrapper.getAccentColor(), storageWrapper.getColumnsTaken());

			if (!player.world.isRemote) {
				UUIDDeduplicator.checkForDuplicateBackpacksAndRemoveTheirUUID(player, backpackUuid, storageWrapper.getBackpack());
			}
		});
	}

	public BackpackContext getBackpackContext() {
		return backpackContext;
	}

	/**
	 * Still the backpack it was opened on: a mod that moves stacks around without asking the slots, as Bogosorter's
	 * hotbar swap, could put another backpack where this one was, and this one in a slot that isn't locked.
	 */
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return backpackContext.canInteractWith(player) && (player.world.isRemote
				|| backpackContext.getBackpackWrapper(player).getContentsUuid().equals(storageWrapper.getContentsUuid()));
	}

	@Override
	public Optional<BlockPos> getBlockPosition() {
		return backpackContext instanceof BackpackContext.Block ? Optional.of(((BackpackContext.Block) backpackContext).getPos()) : Optional.empty();
	}

	@Override
	protected void openSettingsScreen() {
		backpackContext.openSettings(player);
	}

	/** Another player's backpack is only looked into: its settings are its owner's. */
	@Override
	public boolean canOpenSettings() {
		return !(backpackContext instanceof BackpackContext.AnotherPlayer);
	}

	@Override
	protected void onUpgradeChanged() {
		if (player instanceof EntityPlayerMP) {
			ContainerReopener.scheduleReopen((EntityPlayerMP) player, this);
		}
	}
}
