package dev.feldsher.deconstructedbackpacks.upgrades.deposit;

import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.StorageScreenBase;
import dev.feldsher.deconstructedcore.client.gui.controls.ButtonDefinition;
import dev.feldsher.deconstructedcore.client.gui.controls.ButtonDefinitions;
import dev.feldsher.deconstructedcore.client.gui.controls.ToggleButton;
import dev.feldsher.deconstructedcore.client.gui.utils.Dimension;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.client.gui.utils.UV;
import dev.feldsher.deconstructedcore.upgrades.FilterLogicControl;

import java.util.EnumMap;
import java.util.Map;

import static dev.feldsher.deconstructedcore.client.gui.utils.GuiHelper.getButtonStateData;

public abstract class DepositFilterLogicControl extends FilterLogicControl<DepositFilterLogic, DepositFilterLogicContainer> {
	public static final ButtonDefinition.Toggle<DepositFilterType> DEPOSIT_FILTER_TYPE;

	static {
		Map<DepositFilterType, ToggleButton.StateData> states = new EnumMap<>(DepositFilterType.class);
		states.put(DepositFilterType.ALLOW, getButtonStateData(new UV(0, 0), SBPTranslationHelper.INSTANCE.translUpgradeButton("allow"), Dimension.SQUARE_16, new Position(1, 1)));
		states.put(DepositFilterType.BLOCK, getButtonStateData(new UV(16, 0), SBPTranslationHelper.INSTANCE.translUpgradeButton("block"), Dimension.SQUARE_16, new Position(1, 1)));
		states.put(DepositFilterType.INVENTORY, getButtonStateData(new UV(64, 16), SBPTranslationHelper.INSTANCE.translUpgradeButton("deposit_filter_type_inventory"), Dimension.SQUARE_16, new Position(1, 1)));
		DEPOSIT_FILTER_TYPE = ButtonDefinitions.createToggleButtonDefinition(states);
	}

	protected DepositFilterLogicControl(StorageScreenBase<?> screen, Position position, DepositFilterLogicContainer filterLogicContainer, int slotsPerRow, MatchButton... matchButtons) {
		super(screen, position, filterLogicContainer, slotsPerRow, true, matchButtons);
		addChild(new ToggleButton<>(new Position(x, y), DEPOSIT_FILTER_TYPE, button -> updateDepositFilterType(), container::getDepositFilterType));
	}

	private void updateDepositFilterType() {
		DepositFilterType next = container.getDepositFilterType().next();
		container.setDepositFilterType(next);

		container.getFilterSlots().forEach(slot -> slot.setEnabled(next != DepositFilterType.INVENTORY));
	}

	public static class Basic extends DepositFilterLogicControl {
		public Basic(StorageScreenBase<?> screen, Position position, DepositFilterLogicContainer filterLogicContainer, int slotsPerRow) {
			super(screen, position, filterLogicContainer, slotsPerRow);
		}
	}

	public static class Advanced extends DepositFilterLogicControl {
		public Advanced(StorageScreenBase<?> screen, Position position, DepositFilterLogicContainer filterLogicContainer, int slotsPerRow) {
			super(screen, position, filterLogicContainer, slotsPerRow, MatchButton.PRIMARY_MATCH, MatchButton.DURABILITY, MatchButton.NBT);
		}
	}
}
