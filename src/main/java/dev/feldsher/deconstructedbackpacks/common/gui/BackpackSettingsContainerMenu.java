package dev.feldsher.deconstructedbackpacks.common.gui;

import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedbackpacks.settings.BackpackMainSettingsCategory;
import dev.feldsher.deconstructedcore.common.gui.SettingsContainerMenu;
import dev.feldsher.deconstructedcore.settings.main.MainSettingsContainer;
import net.minecraft.entity.player.EntityPlayer;

public class BackpackSettingsContainerMenu extends SettingsContainerMenu<IBackpackWrapper> {
	static {
		addFactory(BackpackMainSettingsCategory.NAME, MainSettingsContainer::new);
	}

	private final BackpackContext backpackContext;

	public BackpackSettingsContainerMenu(EntityPlayer player, BackpackContext backpackContext) {
		super(player, backpackContext.getBackpackWrapper(player));
		this.backpackContext = backpackContext;
	}

	public BackpackContext getBackpackContext() {
		return backpackContext;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return backpackContext.canInteractWith(player);
	}
}
