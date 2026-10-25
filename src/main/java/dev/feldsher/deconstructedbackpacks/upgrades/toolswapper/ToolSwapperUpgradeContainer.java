package dev.feldsher.deconstructedbackpacks.upgrades.toolswapper;

import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerBase;
import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerType;
import dev.feldsher.deconstructedcore.upgrades.FilterLogic;
import dev.feldsher.deconstructedcore.upgrades.FilterLogicContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class ToolSwapperUpgradeContainer extends UpgradeContainerBase<ToolSwapperUpgradeWrapper, ToolSwapperUpgradeContainer> {
	private static final String DATA_SHOULD_SWAP_WEAPON = "shouldSwapWeapon";
	private static final String DATA_TOOL_SWAP_MODE = "toolSwapMode";
	private final FilterLogicContainer<FilterLogic> filterLogicContainer;

	public ToolSwapperUpgradeContainer(EntityPlayer player, int containerId, ToolSwapperUpgradeWrapper wrapper, UpgradeContainerType<ToolSwapperUpgradeWrapper, ToolSwapperUpgradeContainer> type) {
		super(player, containerId, wrapper, type);
		filterLogicContainer = new FilterLogicContainer<>(() -> upgradeWrapper.getFilterLogic(), this, slots::add);
	}

	@Override
	public void handlePacket(NBTTagCompound data) {
		if (data.hasKey(DATA_SHOULD_SWAP_WEAPON)) {
			setSwapWeapon(data.getBoolean(DATA_SHOULD_SWAP_WEAPON));
		} else if (data.hasKey(DATA_TOOL_SWAP_MODE)) {
			setToolSwapMode(ToolSwapMode.fromName(data.getString(DATA_TOOL_SWAP_MODE)));
		} else {
			filterLogicContainer.handlePacket(data);
		}
	}

	public FilterLogicContainer<FilterLogic> getFilterLogicContainer() {
		return filterLogicContainer;
	}

	public void setSwapWeapon(boolean shouldSwapWeapon) {
		upgradeWrapper.setSwapWeapon(shouldSwapWeapon);
		sendBooleanToServer(DATA_SHOULD_SWAP_WEAPON, shouldSwapWeapon);
	}

	public boolean shouldSwapWeapon() {
		return upgradeWrapper.shouldSwapWeapon();
	}

	public void setToolSwapMode(ToolSwapMode toolSwapMode) {
		upgradeWrapper.setToolSwapMode(toolSwapMode);
		sendDataToServer(() -> {
			NBTTagCompound data = new NBTTagCompound();
			data.setString(DATA_TOOL_SWAP_MODE, toolSwapMode.getName());
			return data;
		});
	}

	public ToolSwapMode getToolSwapMode() {
		return upgradeWrapper.getToolSwapMode();
	}
}
