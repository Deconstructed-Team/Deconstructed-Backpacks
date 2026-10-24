package dev.feldsher.deconstructedbackpacks.client.gui;

import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContainer;
import dev.feldsher.deconstructedcore.client.gui.StorageScreenBase;

public class BackpackScreen extends StorageScreenBase<BackpackContainer> {
	public BackpackScreen(BackpackContainer menu) {
		super(menu, menu.getStorageWrapper().getDisplayName());
	}
}
