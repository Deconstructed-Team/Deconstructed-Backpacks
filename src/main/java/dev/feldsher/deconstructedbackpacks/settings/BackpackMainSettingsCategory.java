package dev.feldsher.deconstructedbackpacks.settings;

import dev.feldsher.deconstructedcore.settings.MainSetting;
import dev.feldsher.deconstructedcore.settings.SettingsManager;
import dev.feldsher.deconstructedcore.settings.main.MainSettingsCategory;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Consumer;

/**
 * A backpack's main settings, with whether another player may open it while it's worn. The player's own ones are kept
 * in the persisted part of the player's data, which outlives death in 1.12, so nothing copies them over.
 */
public class BackpackMainSettingsCategory extends MainSettingsCategory<BackpackMainSettingsCategory> {
	public static final String PLAYER_SETTINGS_TAG = "deconstructedBackpacksSettings";
	public static final String NAME = "backpackGlobal";
	public static final MainSetting<Boolean> ANOTHER_PLAYER_CAN_OPEN = MainSetting.ofBoolean("anotherPlayerCanOpen", true);

	static {
		SettingsManager.addSetting(ANOTHER_PLAYER_CAN_OPEN);
	}

	public BackpackMainSettingsCategory(NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt) {
		super(categoryNbt, saveNbt, PLAYER_SETTINGS_TAG);
	}
}
