package dev.feldsher.deconstructedbackpacks.common.gui;

import dev.feldsher.deconstructedbackpacks.settings.BackpackMainSettingsContainer;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedbackpacks.settings.BackpackMainSettingsCategory;
import dev.feldsher.deconstructedcore.common.gui.SettingsContainerMenu;
import net.minecraft.entity.player.EntityPlayer;

public class BackpackSettingsContainerMenu extends SettingsContainerMenu<IBackpackWrapper> {
	static {
		addFactory(BackpackMainSettingsCategory.NAME, BackpackMainSettingsContainer::new);
	}

	private final BackpackContext backpackContext;

	public BackpackSettingsContainerMenu(EntityPlayer player, BackpackContext backpackContext) {
		super(player, backpackContext.getBackpackWrapper(player));
		this.backpackContext = backpackContext;
	}

	public BackpackContext getBackpackContext() {
		return backpackContext;
	}

	/** As the backpack's container: still the same backpack, not another put where it was. */
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return backpackContext.canInteractWith(player) && (player.world.isRemote
				|| backpackContext.getBackpackWrapper(player).getContentsUuid().equals(getStorageWrapper().getContentsUuid()));
	}
}
