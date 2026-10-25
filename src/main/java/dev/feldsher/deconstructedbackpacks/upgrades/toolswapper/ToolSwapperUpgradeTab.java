package dev.feldsher.deconstructedbackpacks.upgrades.toolswapper;

import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.StorageScreenBase;
import dev.feldsher.deconstructedcore.client.gui.UpgradeSettingsTab;
import dev.feldsher.deconstructedcore.client.gui.controls.ButtonDefinition;
import dev.feldsher.deconstructedcore.client.gui.controls.ButtonDefinitions;
import dev.feldsher.deconstructedcore.client.gui.controls.ToggleButton;
import dev.feldsher.deconstructedcore.client.gui.utils.Dimension;
import dev.feldsher.deconstructedcore.client.gui.utils.GuiHelper;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.client.gui.utils.TranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.utils.UV;
import dev.feldsher.deconstructedcore.upgrades.FilterLogic;
import dev.feldsher.deconstructedcore.upgrades.FilterLogicContainer;
import dev.feldsher.deconstructedcore.upgrades.FilterLogicControl;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.Map;

public class ToolSwapperUpgradeTab extends UpgradeSettingsTab<ToolSwapperUpgradeContainer> {
	private static final ButtonDefinition.Toggle<Boolean> SWAP_WEAPON = ButtonDefinitions.createToggleButtonDefinition(
			ButtonDefinitions.getBooleanStateData(
					state(new UV(32, 64), "swap_weapon"),
					state(new UV(48, 64), "do_not_swap_weapon")
			));

	private static final ButtonDefinition.Toggle<ToolSwapMode> SWAP_TOOLS;

	static {
		Map<ToolSwapMode, ToggleButton.StateData> states = new HashMap<>();
		states.put(ToolSwapMode.NO_SWAP, state(new UV(96, 64), "do_not_swap_tools"));
		states.put(ToolSwapMode.ONLY_TOOLS, state(new UV(80, 64), "only_swap_for_tools"));
		states.put(ToolSwapMode.ANY, state(new UV(64, 64), "swap_tools"));
		SWAP_TOOLS = ButtonDefinitions.createToggleButtonDefinition(states);
	}

	protected final FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>> filterLogicControl;

	public ToolSwapperUpgradeTab(ToolSwapperUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen, int slotsPerRow) {
		super(upgradeContainer, position, screen, SBPTranslationHelper.INSTANCE.translUpgrade("advanced_tool_swapper"),
				new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translUpgradeKey("advanced_tool_swapper") + TranslationHelper.TOOLTIP_SUFFIX));
		addHideableChild(new ToggleButton<>(new Position(x + 3, y + 24), SWAP_WEAPON, button -> getContainer().setSwapWeapon(!getContainer().shouldSwapWeapon()),
				() -> getContainer().shouldSwapWeapon()));
		addHideableChild(new ToggleButton<>(new Position(x + 21, y + 24), SWAP_TOOLS, button -> getContainer().setToolSwapMode(getContainer().getToolSwapMode().next()),
				() -> getContainer().getToolSwapMode()));

		filterLogicControl = addHideableChild(new FilterLogicControl.Advanced(screen, new Position(x + 3, y + 44), getContainer().getFilterLogicContainer(), slotsPerRow));
	}

	private static ToggleButton.StateData state(UV uv, String buttonName) {
		String key = SBPTranslationHelper.INSTANCE.translUpgradeButton(buttonName);
		ITextComponent detail = new TextComponentTranslation(key + ".detail");
		detail.getStyle().setColor(TextFormatting.GRAY);
		return GuiHelper.getButtonStateData(uv, Dimension.SQUARE_16, new Position(1, 1), new TextComponentTranslation(key), detail);
	}

	@Override
	protected void moveSlotsToTab() {
		filterLogicControl.moveSlotsToView();
	}
}
