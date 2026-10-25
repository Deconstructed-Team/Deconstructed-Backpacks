package dev.feldsher.deconstructedbackpacks.client.gui;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import java.util.List;
import java.util.ArrayList;
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
	public static final ButtonDefinition.Toggle<Boolean> SHIFT_CLICK_TARGET = ButtonDefinitions.createToggleButtonDefinition(
			ButtonDefinitions.getBooleanStateData(
					getButtonStateData(new UV(32, 48), Dimension.SQUARE_16, new Position(1, 1), lines("shift_click_into_backpack")),
					getButtonStateData(new UV(48, 48), Dimension.SQUARE_16, new Position(1, 1), lines("shift_click_into_inventory"))
			));

	static {
		Map<ContentsFilterType, ToggleButton.StateData> states = new HashMap<>();
		states.put(ContentsFilterType.ALLOW, getButtonStateData(new UV(0, 0), SBPTranslationHelper.INSTANCE.translUpgradeButton("allow"), Dimension.SQUARE_16, new Position(1, 1)));
		states.put(ContentsFilterType.BLOCK, getButtonStateData(new UV(16, 0), SBPTranslationHelper.INSTANCE.translUpgradeButton("block"), Dimension.SQUARE_16, new Position(1, 1)));
		states.put(ContentsFilterType.STORAGE, getButtonStateData(new UV(80, 16), SBPTranslationHelper.INSTANCE.translUpgradeButton("match_backpack_contents"), Dimension.SQUARE_16, new Position(1, 1)));
		BACKPACK_CONTENTS_FILTER_TYPE = ButtonDefinitions.createToggleButtonDefinition(states);
	}

	private static List<ITextComponent> lines(String buttonName) {
		List<ITextComponent> ret = new ArrayList<>();
		for (String line : SBPTranslationHelper.INSTANCE.getTranslatedLines(SBPTranslationHelper.INSTANCE.translUpgradeButton(buttonName))) {
			ret.add(new TextComponentString(line));
		}
		return ret;
	}

	private SBPButtonDefinitions() {}
}
