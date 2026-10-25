package dev.feldsher.deconstructedbackpacks.upgrades.refill;

import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerBase;
import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerType;
import dev.feldsher.deconstructedcore.upgrades.FilterLogic;
import dev.feldsher.deconstructedcore.upgrades.FilterLogicContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class RefillUpgradeContainer extends UpgradeContainerBase<RefillUpgradeWrapper, RefillUpgradeContainer> {
	private static final String DATA_SET_TARGET_SLOT = "setTargetSlot";
	private static final String SLOT_TAG = "slot";
	private static final String TARGET_SLOT_TAG = "targetSlot";
	private final FilterLogicContainer<FilterLogic> filterLogicContainer;

	public RefillUpgradeContainer(EntityPlayer player, int containerId, RefillUpgradeWrapper wrapper, UpgradeContainerType<RefillUpgradeWrapper, RefillUpgradeContainer> type) {
		super(player, containerId, wrapper, type);
		filterLogicContainer = new FilterLogicContainer<>(() -> upgradeWrapper.getFilterLogic(), this, slots::add);
	}

	public FilterLogicContainer<FilterLogic> getFilterLogicContainer() {
		return filterLogicContainer;
	}

	public void setTargetSlot(int slot, TargetSlot targetSlot) {
		upgradeWrapper.setTargetSlot(slot, targetSlot);
		sendDataToServer(() -> {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger(SLOT_TAG, slot);
			tag.setString(TARGET_SLOT_TAG, targetSlot.getName());
			NBTTagCompound data = new NBTTagCompound();
			data.setTag(DATA_SET_TARGET_SLOT, tag);
			return data;
		});
	}

	public TargetSlot getTargetSlot(int slot) {
		return upgradeWrapper.getTargetSlots().getOrDefault(slot, TargetSlot.ANY);
	}

	@Override
	public void handlePacket(NBTTagCompound data) {
		filterLogicContainer.handlePacket(data);
		if (data.hasKey(DATA_SET_TARGET_SLOT)) {
			NBTTagCompound tag = data.getCompoundTag(DATA_SET_TARGET_SLOT);
			if (tag.hasKey(SLOT_TAG) && tag.hasKey(TARGET_SLOT_TAG)) {
				setTargetSlot(tag.getInteger(SLOT_TAG), TargetSlot.fromName(tag.getString(TARGET_SLOT_TAG)));
			}
		}
	}

	public boolean allowsTargetSlotSelection() {
		return upgradeWrapper.allowsTargetSlotSelection();
	}
}
