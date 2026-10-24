package dev.feldsher.deconstructedbackpacks.client.gui;

import dev.feldsher.deconstructedbackpacks.common.gui.BackpackSettingsContainerMenu;
import dev.feldsher.deconstructedbackpacks.network.BackpackOpenMessage;
import dev.feldsher.deconstructedbackpacks.network.PacketHandler;
import dev.feldsher.deconstructedbackpacks.settings.BackpackSettingsTabControl;
import dev.feldsher.deconstructedcore.client.gui.SettingsScreen;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.settings.StorageSettingsTabControlBase;

public class BackpackSettingsScreen extends SettingsScreen {
	public BackpackSettingsScreen(BackpackSettingsContainerMenu container) {
		super(container, container.getStorageWrapper().getDisplayName());
	}

	@Override
	protected StorageSettingsTabControlBase initializeTabControl() {
		return new BackpackSettingsTabControl(this, new Position(guiLeft + xSize, guiTop + 4));
	}

	@Override
	protected void sendStorageInventoryScreenOpenMessage() {
		PacketHandler.INSTANCE.sendToServer(new BackpackOpenMessage());
	}
}
