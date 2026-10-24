package dev.feldsher.deconstructedbackpacks.client;

import dev.feldsher.deconstructedbackpacks.CommonProxy;
import dev.feldsher.deconstructedbackpacks.client.gui.BackpackScreen;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContainer;

@SuppressWarnings("unused") // instantiated by @SidedProxy
public class ClientProxy extends CommonProxy {
	@Override
	public void init() {
		KeybindHandler.register();
	}

	@Override
	public Object createBackpackScreen(BackpackContainer container) {
		return new BackpackScreen(container);
	}
}
