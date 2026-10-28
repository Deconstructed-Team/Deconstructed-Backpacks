package dev.feldsher.deconstructedbackpacks.upgrades.anvil;

import dev.feldsher.deconstructedcore.common.gui.SlotSuppliedHandler;
import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerBase;
import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerType;
import dev.feldsher.deconstructedcore.upgrades.IUpgradeWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

/**
 * Vanilla's anvil works the upgrade's two slots: its container is kept aside, with its inputs swapped for the upgrade's,
 * and its result slot shown in the tab. Both sides work out the result and its cost; the client sends the typed name.
 */
public class AnvilUpgradeContainer extends UpgradeContainerBase<AnvilUpgradeWrapper, AnvilUpgradeContainer> {
	private static final String DATA_SHIFT_CLICK_INTO_STORAGE = "shiftClickIntoStorage";
	private static final String DATA_ITEM_NAME = "itemName";
	public static final int MAX_ITEM_NAME_LENGTH = 50;
	private static final String INPUT_SLOTS_FIELD = "field_82853_g";
	private final InputInventory inputInventory = new InputInventory();
	private final PersistableAnvilMenu anvilMenuDelegate;
	private final Slot resultSlot;
	private Runnable nameChangeListener = () -> {};

	public AnvilUpgradeContainer(EntityPlayer player, int upgradeContainerId, AnvilUpgradeWrapper upgradeWrapper, UpgradeContainerType<AnvilUpgradeWrapper, AnvilUpgradeContainer> type) {
		super(player, upgradeContainerId, upgradeWrapper, type);
		anvilMenuDelegate = new PersistableAnvilMenu(player);
		for (int slot = 0; slot < 2; slot++) {
			slots.add(new SlotSuppliedHandler(() -> this.upgradeWrapper.getInventory(), slot, 0, 0) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					anvilMenuDelegate.onCraftMatrixChanged(inputInventory);
				}
			});
		}
		resultSlot = new ResultSlot(anvilMenuDelegate.getSlot(2));
		slots.add(resultSlot);
	}

	public void setNameChangeListener(Runnable nameChangeListener) {
		this.nameChangeListener = nameChangeListener;
	}

	@Override
	public void handlePacket(NBTTagCompound data) {
		if (data.hasKey(DATA_SHIFT_CLICK_INTO_STORAGE)) {
			setShiftClickIntoStorage(data.getBoolean(DATA_SHIFT_CLICK_INTO_STORAGE));
		} else if (data.hasKey(DATA_ITEM_NAME)) {
			String name = ChatAllowedCharacters.filterAllowedCharacters(data.getString(DATA_ITEM_NAME));
			if (name.length() <= MAX_ITEM_NAME_LENGTH) {
				setItemName(name);
			}
		}
	}

	@Override
	public void setUpgradeWrapper(IUpgradeWrapper updatedUpgradeWrapper) {
		super.setUpgradeWrapper(updatedUpgradeWrapper);
		anvilMenuDelegate.updateItemName(upgradeWrapper.getItemName());
		nameChangeListener.run();
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

	public void setItemName(String name) {
		upgradeWrapper.setItemName(name);
		anvilMenuDelegate.updateItemName(name);
		sendDataToServer(() -> {
			NBTTagCompound data = new NBTTagCompound();
			data.setString(DATA_ITEM_NAME, name);
			return data;
		});
	}

	public String getItemName() {
		return upgradeWrapper.getItemName();
	}

	public int getCost() {
		return anvilMenuDelegate.maximumCost;
	}

	/** The upgrade's two slots, as vanilla's anvil takes its inputs. */
	private class InputInventory extends InventoryBasic {
		private InputInventory() {
			super("Anvil", false, 2);
		}

		private ItemStackHandler handler() {
			return upgradeWrapper.getInventory();
		}

		@Override
		public ItemStack getStackInSlot(int index) {
			return handler().getStackInSlot(index);
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			handler().setStackInSlot(index, stack);
			markDirty();
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {
			ItemStack stack = handler().getStackInSlot(index).copy();
			ItemStack taken = stack.splitStack(count);
			if (!taken.isEmpty()) {
				handler().setStackInSlot(index, stack);
				markDirty();
			}
			return taken;
		}

		@Override
		public ItemStack removeStackFromSlot(int index) {
			ItemStack stack = handler().getStackInSlot(index);
			handler().setStackInSlot(index, ItemStack.EMPTY);
			return stack;
		}

		@Override
		public boolean isEmpty() {
			return handler().getStackInSlot(0).isEmpty() && handler().getStackInSlot(1).isEmpty();
		}

		@Override
		public void clear() {
			handler().setStackInSlot(0, ItemStack.EMPTY);
			handler().setStackInSlot(1, ItemStack.EMPTY);
		}

		@Override
		public void markDirty() {
			super.markDirty();
			anvilMenuDelegate.onCraftMatrixChanged(this);
		}
	}

	/**
	 * Vanilla's result slot, taken only while the inputs make it: another player's screen of the same upgrade can have
	 * used them up, and the result this one worked out would come of nothing. Then it's worked out again, not taken.
	 */
	private class ResultSlot extends Slot {
		private final Slot anvilResultSlot;

		private ResultSlot(Slot anvilResultSlot) {
			super(anvilResultSlot.inventory, anvilResultSlot.getSlotIndex(), anvilResultSlot.xPos, anvilResultSlot.yPos);
			this.anvilResultSlot = anvilResultSlot;
		}

		@Override
		public boolean isItemValid(@Nonnull ItemStack stack) {
			return false;
		}

		@Override
		public boolean canTakeStack(EntityPlayer player) {
			if (!player.world.isRemote) {
				ItemStack shown = getStack().copy();
				anvilMenuDelegate.updateRepairOutput();
				if (!ItemStack.areItemStacksEqual(shown, getStack())) {
					return false;
				}
			}
			return anvilResultSlot.canTakeStack(player);
		}

		@Nonnull
		@Override
		public ItemStack onTake(EntityPlayer player, @Nonnull ItemStack stack) {
			return anvilResultSlot.onTake(player, stack);
		}
	}

	private class PersistableAnvilMenu extends ContainerRepair {
		private PersistableAnvilMenu(EntityPlayer player) {
			super(new InventoryPlayer(player), player.world, new BlockPos(player), player);
			ObfuscationReflectionHelper.setPrivateValue(ContainerRepair.class, this, inputInventory, INPUT_SLOTS_FIELD);
			updateItemName(upgradeWrapper.getItemName());
		}

		/** The name follows the first slot: its item's name when an item comes into it, none once it's empty. */
		@Override
		public void onCraftMatrixChanged(@Nonnull IInventory inventory) {
			ItemStack first = inputInventory.getStackInSlot(0);
			String itemName = upgradeWrapper.getItemName();
			if (itemName.isEmpty() != first.isEmpty()) {
				String newItemName = first.isEmpty() ? "" : first.getDisplayName();
				upgradeWrapper.setItemName(newItemName);
				updateItemName(newItemName);
				nameChangeListener.run();
				return;
			}
			super.onCraftMatrixChanged(inventory);
		}
	}
}
