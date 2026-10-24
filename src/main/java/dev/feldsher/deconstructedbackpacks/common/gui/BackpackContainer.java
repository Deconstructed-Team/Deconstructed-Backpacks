package dev.feldsher.deconstructedbackpacks.common.gui;

import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedcore.common.gui.StorageContainerMenuBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class BackpackContainer extends StorageContainerMenuBase<IBackpackWrapper> {
	private final BackpackContext backpackContext;

	public BackpackContainer(EntityPlayer player, BackpackContext backpackContext) {
		super(player, backpackContext.getBackpackWrapper(player), backpackContext.getBackpackSlotIndex(), backpackContext.shouldLockBackpackSlot(player));
		this.backpackContext = backpackContext;
	}

	public BackpackContext getBackpackContext() {
		return backpackContext;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return backpackContext.canInteractWith(player);
	}

	@Override
	protected void onUpgradeChanged() {
		if (player instanceof EntityPlayerMP) {
			ContainerReopener.scheduleReopen((EntityPlayerMP) player, this);
		}
	}
}
