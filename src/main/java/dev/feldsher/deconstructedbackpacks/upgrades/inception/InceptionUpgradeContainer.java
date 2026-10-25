package dev.feldsher.deconstructedbackpacks.upgrades.inception;

import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerBase;
import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class InceptionUpgradeContainer extends UpgradeContainerBase<InceptionUpgradeWrapper, InceptionUpgradeContainer> {
	private static final String DATA_INVENTORY_ORDER = "inventoryOrder";

	public InceptionUpgradeContainer(EntityPlayer player, int containerId, InceptionUpgradeWrapper wrapper, UpgradeContainerType<InceptionUpgradeWrapper, InceptionUpgradeContainer> type) {
		super(player, containerId, wrapper, type);
	}

	@Override
	public void handlePacket(NBTTagCompound data) {
		if (data.hasKey(DATA_INVENTORY_ORDER)) {
			setInventoryOrder(InventoryOrder.fromName(data.getString(DATA_INVENTORY_ORDER)));
		}
	}

	public InventoryOrder getInventoryOrder() {
		return upgradeWrapper.getInventoryOrder();
	}

	public void setInventoryOrder(InventoryOrder inventoryOrder) {
		upgradeWrapper.setInventoryOrder(inventoryOrder);
		sendDataToServer(() -> {
			NBTTagCompound data = new NBTTagCompound();
			data.setString(DATA_INVENTORY_ORDER, inventoryOrder.getName());
			return data;
		});
	}
}
