package dev.feldsher.deconstructedbackpacks.upgrades.inception;

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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InceptionUpgradeTab extends UpgradeSettingsTab<InceptionUpgradeContainer> {
	private static final ButtonDefinition.Toggle<InventoryOrder> INVENTORY_ORDER;

	static {
		Map<InventoryOrder, ToggleButton.StateData> states = new HashMap<>();
		states.put(InventoryOrder.MAIN_FIRST, GuiHelper.getButtonStateData(new UV(48, 32), Dimension.SQUARE_16, new Position(1, 1), lines("inventory_order_main_first")));
		states.put(InventoryOrder.INCEPTED_FIRST, GuiHelper.getButtonStateData(new UV(64, 32), Dimension.SQUARE_16, new Position(1, 1), lines("inventory_order_incepted_first")));
		INVENTORY_ORDER = ButtonDefinitions.createToggleButtonDefinition(states);
	}

	public InceptionUpgradeTab(InceptionUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen) {
		super(upgradeContainer, position, screen, SBPTranslationHelper.INSTANCE.translUpgrade("inception"),
				new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translUpgradeKey("inception") + TranslationHelper.TOOLTIP_SUFFIX));
		addHideableChild(new ToggleButton<>(new Position(x + 3, y + 24), INVENTORY_ORDER, button -> getContainer().setInventoryOrder(getContainer().getInventoryOrder().next()),
				() -> getContainer().getInventoryOrder()));
	}

	private static List<ITextComponent> lines(String buttonName) {
		List<ITextComponent> ret = new ArrayList<>();
		for (String line : SBPTranslationHelper.INSTANCE.getTranslatedLines(SBPTranslationHelper.INSTANCE.translUpgradeButton(buttonName))) {
			ret.add(new TextComponentString(line));
		}
		return ret;
	}

	@Override
	protected void moveSlotsToTab() {
		//noop
	}
}
