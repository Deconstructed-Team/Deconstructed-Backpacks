package dev.feldsher.deconstructedbackpacks.upgrades.deposit;

import dev.feldsher.deconstructedcore.common.gui.IServerUpdater;
import dev.feldsher.deconstructedcore.upgrades.FilterLogicContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** With the inventory type, the filter slots are off: the target inventory is the filter. */
public class DepositFilterLogicContainer extends FilterLogicContainer<DepositFilterLogic> {
	private static final String DATA_DEPOSIT_FILTER_TYPE = "depositFilterType";

	public DepositFilterLogicContainer(Supplier<DepositFilterLogic> filterLogic, IServerUpdater serverUpdater, Consumer<Slot> addSlot) {
		super(filterLogic, serverUpdater, addSlot);
		if (getDepositFilterType() == DepositFilterType.INVENTORY) {
			getFilterSlots().forEach(s -> s.setEnabled(false));
		}
	}

	public void setDepositFilterType(DepositFilterType depositFilterType) {
		filterLogic.get().setDepositFilterType(depositFilterType);
		serverUpdater.sendDataToServer(() -> {
			NBTTagCompound data = new NBTTagCompound();
			data.setString(DATA_DEPOSIT_FILTER_TYPE, depositFilterType.getSerializedName());
			return data;
		});
	}

	@Override
	public boolean handlePacket(NBTTagCompound data) {
		if (data.hasKey(DATA_DEPOSIT_FILTER_TYPE)) {
			setDepositFilterType(DepositFilterType.fromName(data.getString(DATA_DEPOSIT_FILTER_TYPE)));
			return true;
		}
		return super.handlePacket(data);
	}

	public DepositFilterType getDepositFilterType() {
		return filterLogic.get().getDepositFilterType();
	}
}
