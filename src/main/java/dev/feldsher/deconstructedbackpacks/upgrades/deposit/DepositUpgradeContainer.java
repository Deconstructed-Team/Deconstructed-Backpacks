package dev.feldsher.deconstructedbackpacks.upgrades.deposit;

import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerBase;
import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class DepositUpgradeContainer extends UpgradeContainerBase<DepositUpgradeWrapper, DepositUpgradeContainer> {
	private final DepositFilterLogicContainer filterLogicContainer;

	public DepositUpgradeContainer(EntityPlayer player, int containerId, DepositUpgradeWrapper wrapper, UpgradeContainerType<DepositUpgradeWrapper, DepositUpgradeContainer> type) {
		super(player, containerId, wrapper, type);
		filterLogicContainer = new DepositFilterLogicContainer(() -> upgradeWrapper.getFilterLogic(), this, slots::add);
	}

	public DepositFilterLogicContainer getFilterLogicContainer() {
		return filterLogicContainer;
	}

	@Override
	public void handlePacket(NBTTagCompound data) {
		filterLogicContainer.handlePacket(data);
	}
}
