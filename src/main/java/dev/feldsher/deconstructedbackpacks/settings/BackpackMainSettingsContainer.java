package dev.feldsher.deconstructedbackpacks.settings;

import dev.feldsher.deconstructedcore.common.gui.SettingsContainerMenu;
import dev.feldsher.deconstructedcore.settings.main.MainSettingsCategory;
import dev.feldsher.deconstructedcore.settings.main.MainSettingsContainer;

public class BackpackMainSettingsContainer extends MainSettingsContainer {
	public BackpackMainSettingsContainer(SettingsContainerMenu<?> settingsContainer, String categoryName, MainSettingsCategory<?> category) {
		super(settingsContainer, categoryName, category);
	}

	public void toggleAnotherPlayerCanOpen() {
		toggleBooleanSetting(BackpackMainSettingsCategory.ANOTHER_PLAYER_CAN_OPEN);
	}

	public boolean canAnotherPlayerOpen() {
		return getSettingValue(BackpackMainSettingsCategory.ANOTHER_PLAYER_CAN_OPEN);
	}
}
