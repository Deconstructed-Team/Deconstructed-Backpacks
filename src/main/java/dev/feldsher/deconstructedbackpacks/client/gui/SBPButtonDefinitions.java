package dev.feldsher.deconstructedbackpacks.client.gui;

import dev.feldsher.deconstructedcore.client.gui.controls.ButtonDefinition;
import dev.feldsher.deconstructedcore.client.gui.controls.ButtonDefinitions;
import dev.feldsher.deconstructedcore.client.gui.controls.ToggleButton;
import dev.feldsher.deconstructedcore.client.gui.utils.Dimension;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.client.gui.utils.UV;
import dev.feldsher.deconstructedcore.upgrades.ContentsFilterType;

import java.util.HashMap;
import java.util.Map;

import static dev.feldsher.deconstructedcore.client.gui.utils.GuiHelper.getButtonStateData;

public class SBPButtonDefinitions {
	public static final ButtonDefinition.Toggle<ContentsFilterType> BACKPACK_CONTENTS_FILTER_TYPE;

	static {
		Map<ContentsFilterType, ToggleButton.StateData> states = new HashMap<>();
		states.put(ContentsFilterType.ALLOW, getButtonStateData(new UV(0, 0), SBPTranslationHelper.INSTANCE.translUpgradeButton("allow"), Dimension.SQUARE_16, new Position(1, 1)));
		states.put(ContentsFilterType.BLOCK, getButtonStateData(new UV(16, 0), SBPTranslationHelper.INSTANCE.translUpgradeButton("block"), Dimension.SQUARE_16, new Position(1, 1)));
		states.put(ContentsFilterType.STORAGE, getButtonStateData(new UV(80, 16), SBPTranslationHelper.INSTANCE.translUpgradeButton("match_backpack_contents"), Dimension.SQUARE_16, new Position(1, 1)));
		BACKPACK_CONTENTS_FILTER_TYPE = ButtonDefinitions.createToggleButtonDefinition(states);
	}

	private SBPButtonDefinitions() {}
}
