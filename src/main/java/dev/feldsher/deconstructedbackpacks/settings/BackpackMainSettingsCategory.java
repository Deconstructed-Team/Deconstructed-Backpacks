package dev.feldsher.deconstructedbackpacks.settings;

import dev.feldsher.deconstructedcore.settings.main.MainSettingsCategory;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Consumer;

/**
 * A backpack's main settings. Whether another player may open the backpack comes with opening other players'
 * backpacks.
 */
public class BackpackMainSettingsCategory extends MainSettingsCategory<BackpackMainSettingsCategory> {
	public static final String PLAYER_SETTINGS_TAG = "deconstructedBackpacksSettings";
	public static final String NAME = "backpackGlobal";

	public BackpackMainSettingsCategory(NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt) {
		super(categoryNbt, saveNbt, PLAYER_SETTINGS_TAG);
	}
}
