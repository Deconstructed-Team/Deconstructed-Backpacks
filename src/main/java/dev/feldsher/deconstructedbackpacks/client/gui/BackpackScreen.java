package dev.feldsher.deconstructedbackpacks.client.gui;

import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContainer;
import dev.feldsher.deconstructedbackpacks.network.BackpackOpenMessage;
import dev.feldsher.deconstructedbackpacks.network.PacketHandler;
import dev.feldsher.deconstructedcore.client.gui.StorageScreenBase;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/** Escape in a backpack open from inside another goes back to that one. */
public class BackpackScreen extends StorageScreenBase<BackpackContainer> {
	@Override
	protected String getStorageSettingsTabTooltip() {
		return SBPTranslationHelper.INSTANCE.translGui("settings.tooltip");
	}

	public BackpackScreen(BackpackContainer menu) {
		super(menu, menu.getBackpackContext().getDisplayName(Minecraft.getMinecraft().player));
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE && !getMenu().isFirstLevelStorage()) {
			PacketHandler.INSTANCE.sendToServer(BackpackOpenMessage.parent());
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}
}
