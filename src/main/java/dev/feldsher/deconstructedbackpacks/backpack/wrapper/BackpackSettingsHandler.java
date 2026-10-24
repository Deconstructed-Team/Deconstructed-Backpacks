package dev.feldsher.deconstructedbackpacks.backpack.wrapper;

import dev.feldsher.deconstructedbackpacks.settings.BackpackMainSettingsCategory;
import dev.feldsher.deconstructedcore.settings.ISettingsCategory;
import dev.feldsher.deconstructedcore.settings.SettingsHandler;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Consumer;

/** A backpack's settings, under {@link #SETTINGS_TAG} in its contents. */
public class BackpackSettingsHandler extends SettingsHandler {
	public static final String SETTINGS_TAG = "settings";

	public BackpackSettingsHandler(NBTTagCompound backpackContentsNbt, Runnable markBackpackContentsDirty) {
		super(backpackContentsNbt, markBackpackContentsDirty);
	}

	@Override
	protected NBTTagCompound getSettingsNbtFromContentsNbt(NBTTagCompound contentsNbt) {
		return contentsNbt.getCompoundTag(SETTINGS_TAG);
	}

	@Override
	protected void saveCategoryNbt(NBTTagCompound settingsNbt, String categoryName, NBTTagCompound tag) {
		settingsNbt.setTag(categoryName, tag);
		contentsNbt.setTag(SETTINGS_TAG, settingsNbt);
	}

	@Override
	public String getGlobalSettingsCategoryName() {
		return BackpackMainSettingsCategory.NAME;
	}

	@Override
	public ISettingsCategory<?> instantiateGlobalSettingsCategory(NBTTagCompound categoryNbt, Consumer<NBTTagCompound> saveNbt) {
		return new BackpackMainSettingsCategory(categoryNbt, saveNbt);
	}
}
