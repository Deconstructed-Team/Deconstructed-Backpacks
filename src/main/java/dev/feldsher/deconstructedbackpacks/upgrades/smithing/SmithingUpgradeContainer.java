package dev.feldsher.deconstructedbackpacks.upgrades.smithing;

import dev.feldsher.deconstructedbackpacks.compat.FutureMcSmithingRecipes;
import dev.feldsher.deconstructedcore.common.gui.SlotSuppliedHandler;
import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerBase;
import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerType;
import dev.feldsher.deconstructedcore.upgrades.IUpgradeWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nonnull;

/**
 * The item and material slots and the result slot. Both sides work out the result, and taking it uses up one of each
 * input on both, as Future MC's smithing table does on the server.
 */
public class SmithingUpgradeContainer extends UpgradeContainerBase<SmithingUpgradeWrapper, SmithingUpgradeContainer> {
	private static final String DATA_SHIFT_CLICK_INTO_STORAGE = "shiftClickIntoStorage";
	private final InventoryCraftResult resultInventory = new InventoryCraftResult();
	private final Slot baseSlot;
	private final Slot additionalSlot;
	private final Slot resultSlot;
	private Runnable onResultChanged = () -> {};

	public SmithingUpgradeContainer(EntityPlayer player, int upgradeContainerId, SmithingUpgradeWrapper upgradeWrapper, UpgradeContainerType<SmithingUpgradeWrapper, SmithingUpgradeContainer> type) {
		super(player, upgradeContainerId, upgradeWrapper, type);
		baseSlot = addInputSlot(0);
		additionalSlot = addInputSlot(1);
		resultSlot = new ResultSlot();
		slots.add(resultSlot);
		updateResult();
	}

	private Slot addInputSlot(int index) {
		Slot slot = new SlotSuppliedHandler(() -> upgradeWrapper.getInventory(), index, 0, 0) {
			@Override
			public void onSlotChanged() {
				super.onSlotChanged();
				updateResult();
			}
		};
		slots.add(slot);
		return slot;
	}

	private void updateResult() {
		resultInventory.setInventorySlotContents(0, FutureMcSmithingRecipes.getResult(baseSlot.getStack(), additionalSlot.getStack()));
		onResultChanged.run();
	}

	public void setOnResultChangedHandler(Runnable onResultChanged) {
		this.onResultChanged = onResultChanged;
	}

	@Override
	public void handlePacket(NBTTagCompound data) {
		if (data.hasKey(DATA_SHIFT_CLICK_INTO_STORAGE)) {
			setShiftClickIntoStorage(data.getBoolean(DATA_SHIFT_CLICK_INTO_STORAGE));
		}
	}

	@Override
	public void setUpgradeWrapper(IUpgradeWrapper updatedUpgradeWrapper) {
		super.setUpgradeWrapper(updatedUpgradeWrapper);
		updateResult();
	}

	public boolean shouldShiftClickIntoStorage() {
		return upgradeWrapper.shouldShiftClickIntoStorage();
	}

	public void setShiftClickIntoStorage(boolean shiftClickIntoStorage) {
		upgradeWrapper.setShiftClickIntoStorage(shiftClickIntoStorage);
		sendBooleanToServer(DATA_SHIFT_CLICK_INTO_STORAGE, shiftClickIntoStorage);
	}

	@Override
	public boolean mergeIntoStorageFirst(Slot slot) {
		return slot != resultSlot || shouldShiftClickIntoStorage();
	}

	@Override
	public boolean allowsPickupAll(Slot slot) {
		return slot != resultSlot;
	}

	public Slot getBaseSlot() {
		return baseSlot;
	}

	public Slot getAdditionalSlot() {
		return additionalSlot;
	}

	public Slot getResultSlot() {
		return resultSlot;
	}

	private class ResultSlot extends Slot {
		private ResultSlot() {
			super(resultInventory, 0, 0, 0);
		}

		@Override
		public boolean isItemValid(@Nonnull ItemStack stack) {
			return false;
		}

		/**
		 * Only while the inputs make it: another player's screen of the same upgrade can have used them up, and the
		 * result this one worked out would come of nothing. Then it's worked out again, not taken.
		 */
		@Override
		public boolean canTakeStack(EntityPlayer thePlayer) {
			if (!thePlayer.world.isRemote) {
				ItemStack shown = getStack().copy();
				updateResult();
				if (!ItemStack.areItemStacksEqual(shown, getStack())) {
					return false;
				}
			}
			return super.canTakeStack(thePlayer);
		}

		@Nonnull
		@Override
		public ItemStack onTake(EntityPlayer thePlayer, @Nonnull ItemStack stack) {
			stack.onCrafting(thePlayer.world, thePlayer, stack.getCount());
			baseSlot.decrStackSize(1);
			additionalSlot.decrStackSize(1);
			updateResult();
			if (!thePlayer.world.isRemote) {
				SoundEvent sound = FutureMcSmithingRecipes.getUseSound();
				if (sound != null) {
					thePlayer.world.playSound(null, thePlayer.posX, thePlayer.posY, thePlayer.posZ, sound, SoundCategory.BLOCKS, 1.0F, thePlayer.world.rand.nextFloat() * 0.1F + 0.9F);
				}
			}
			return super.onTake(thePlayer, stack);
		}
	}
}
