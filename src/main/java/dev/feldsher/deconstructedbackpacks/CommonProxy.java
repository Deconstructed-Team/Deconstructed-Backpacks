package dev.feldsher.deconstructedbackpacks;

import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContainer;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackSettingsContainerMenu;

import javax.annotation.Nullable;

/** What only one side does. The server's half does nothing; the client's is {@link dev.feldsher.deconstructedbackpacks.client.ClientProxy}. */
public class CommonProxy {
	public void init() {
		//noop
	}

	public void postInit() {
		//noop
	}

	@Nullable
	public Object createBackpackScreen(BackpackContainer container) {
		return null;
	}

	@Nullable
	public Object createBackpackSettingsScreen(BackpackSettingsContainerMenu container) {
		return null;
	}
}
