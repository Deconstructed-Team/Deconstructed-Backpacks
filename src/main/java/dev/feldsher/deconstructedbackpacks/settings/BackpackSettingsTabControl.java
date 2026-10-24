package dev.feldsher.deconstructedbackpacks.settings;

import dev.feldsher.deconstructedcore.client.gui.SettingsScreen;
import dev.feldsher.deconstructedcore.client.gui.Tab;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.settings.StorageSettingsTabControlBase;
import dev.feldsher.deconstructedcore.settings.main.MainSettingsContainer;
import dev.feldsher.deconstructedcore.settings.memory.MemorySettingsCategory;
import dev.feldsher.deconstructedcore.settings.memory.MemorySettingsContainer;
import dev.feldsher.deconstructedcore.settings.memory.MemorySettingsTab;
import dev.feldsher.deconstructedcore.settings.nosort.NoSortSettingsCategory;
import dev.feldsher.deconstructedcore.settings.nosort.NoSortSettingsContainer;
import dev.feldsher.deconstructedcore.settings.nosort.NoSortSettingsTab;

import java.util.HashMap;
import java.util.Map;

/** The item display tab comes with the step that ports it. */
public class BackpackSettingsTabControl extends StorageSettingsTabControlBase {
	private static final Map<String, ISettingsTabFactory<?, ?>> SETTINGS_TAB_FACTORIES = new HashMap<>();

	static {
		SETTINGS_TAB_FACTORIES.put(BackpackMainSettingsCategory.NAME, (ISettingsTabFactory<MainSettingsContainer, BackpackMainSettingsTab>) BackpackMainSettingsTab::new);
		SETTINGS_TAB_FACTORIES.put(NoSortSettingsCategory.NAME, (ISettingsTabFactory<NoSortSettingsContainer, NoSortSettingsTab>) NoSortSettingsTab::new);
		SETTINGS_TAB_FACTORIES.put(MemorySettingsCategory.NAME, (ISettingsTabFactory<MemorySettingsContainer, MemorySettingsTab>) MemorySettingsTab::new);
	}

	public BackpackSettingsTabControl(SettingsScreen screen, Position position) {
		super(screen, position);
	}

	@Override
	protected Tab instantiateReturnBackTab() {
		return new BackToBackpackTab(new Position(x, getTopY()));
	}

	@Override
	protected Map<String, ISettingsTabFactory<?, ?>> getSettingsTabFactories() {
		return SETTINGS_TAB_FACTORIES;
	}
}
