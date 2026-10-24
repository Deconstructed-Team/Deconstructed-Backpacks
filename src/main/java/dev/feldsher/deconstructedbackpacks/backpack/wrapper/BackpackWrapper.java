package dev.feldsher.deconstructedbackpacks.backpack.wrapper;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackStorage;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.InventoryHandler;
import dev.feldsher.deconstructedcore.inventory.StorageWrapperRepository;
import dev.feldsher.deconstructedcore.upgrades.UpgradeHandler;
import dev.feldsher.deconstructedcore.upgrades.stack.StackUpgradeItem;
import dev.feldsher.deconstructedcore.util.StorageNbt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * A backpack stack and its contents. The stack carries the contents' UUID, its slot counts and colors;
 * the contents themselves are in {@link BackpackStorage}.
 * <p>
 * Settings, fluids, energy, loot and render info come with the stages that port them.
 */
public class BackpackWrapper implements IBackpackWrapper {
	public static final int DEFAULT_MAIN_COLOR = 0xFF_CC613A;
	public static final int DEFAULT_ACCENT_COLOR = 0xFF_622E1A;

	@Nullable
	private ItemStack backpack;
	private Runnable backpackSaveHandler = () -> {
	};
	private Runnable inventorySlotChangeHandler = () -> {
	};

	@Nullable
	private InventoryHandler handler = null;
	@Nullable
	private UpgradeHandler upgradeHandler = null;
	private boolean upgradeHandlerIsDummy = false;
	private Runnable upgradeCachesInvalidatedHandler = () -> {
	};

	public BackpackWrapper(ItemStack backpackStack) {
		setBackpackStack(backpackStack);
	}

	public static IBackpackWrapper fromStack(ItemStack stack) {
		return StorageWrapperRepository.getStorageWrapper(stack, IBackpackWrapper.class, BackpackWrapper::new);
	}

	public static Optional<IBackpackWrapper> fromExistingData(ItemStack stack) {
		if (stack.getItem() instanceof BackpackItem) {
			return StorageWrapperRepository.getExistingStorageWrapper(stack, IBackpackWrapper.class);
		}
		return Optional.empty();
	}

	@Override
	public void setContentsChangeHandler(Runnable contentsChangeHandler) {
		backpackSaveHandler = contentsChangeHandler;
	}

	@Override
	public void setInventorySlotChangeHandler(Runnable slotChangeHandler) {
		inventorySlotChangeHandler = slotChangeHandler;
	}

	@Override
	public InventoryHandler getInventoryHandler() {
		if (handler == null) {
			handler = new BackpackInventoryHandler(getNumberOfInventorySlots() - (getNumberOfSlotRows() * getColumnsTaken()),
					this, getBackpackContentsNbt(), () -> {
				markBackpackContentsDirty();
				inventorySlotChangeHandler.run();
			}, StackUpgradeItem.getInventorySlotLimit(this));
		}
		return handler;
	}

	/**
	 * A backpack whose contents don't exist yet has no upgrades: it gets an empty handler, as 1.21's Noop, so that
	 * ticking every backpack in an inventory doesn't create contents for each.
	 */
	@Override
	public UpgradeHandler getUpgradeHandler() {
		if (upgradeHandler == null) {
			if (getContentsUuid().isPresent()) {
				upgradeHandlerIsDummy = false;
				upgradeHandler = new UpgradeHandler(getNumberOfUpgradeSlots(), this, getBackpackContentsNbt(), this::markBackpackContentsDirty, this::onUpgradesChanged) {
					@Override
					public boolean isItemValid(int slot, ItemStack stack) {
						return super.isItemValid(slot, stack) && (stack.isEmpty() || ModItems.isBackpackUpgrade(stack.getItem()));
					}
				};
			} else {
				upgradeHandlerIsDummy = true;
				upgradeHandler = new UpgradeHandler(0, this, new NBTTagCompound(), () -> {
				}, () -> {
				});
				upgradeHandler.setPersistent(false);
			}
		}
		return upgradeHandler;
	}

	private void onUpgradesChanged() {
		if (handler != null) {
			handler.setBaseSlotLimit(StackUpgradeItem.getInventorySlotLimit(this));
		}
		upgradeCachesInvalidatedHandler.run();
	}

	@Override
	public void setUpgradeCachesInvalidatedHandler(Runnable handler) {
		upgradeCachesInvalidatedHandler = handler;
	}

	private int getNumberOfUpgradeSlots() {
		NBTTagCompound tag = getBackpackStack().getTagCompound();
		if (tag != null && tag.hasKey(StorageNbt.NUMBER_OF_UPGRADE_SLOTS)) {
			return tag.getInteger(StorageNbt.NUMBER_OF_UPGRADE_SLOTS);
		}

		int itemUpgradeSlots = ((BackpackItem) getBackpackStack().getItem()).getNumberOfUpgradeSlots();
		setNumberOfUpgradeSlots(itemUpgradeSlots);
		return itemUpgradeSlots;
	}

	private int getNumberOfInventorySlots() {
		NBTTagCompound tag = getBackpackStack().getTagCompound();
		if (tag != null && tag.hasKey(StorageNbt.NUMBER_OF_INVENTORY_SLOTS)) {
			return tag.getInteger(StorageNbt.NUMBER_OF_INVENTORY_SLOTS);
		}

		int itemInventorySlots = ((BackpackItem) getBackpackStack().getItem()).getNumberOfSlots();
		setNumberOfInventorySlots(itemInventorySlots);
		return itemInventorySlots;
	}

	@Override
	public int getNumberOfSlotRows() {
		int itemInventorySlots = getNumberOfInventorySlots();
		return (int) Math.ceil(itemInventorySlots <= 81 ? (double) itemInventorySlots / 9 : (double) itemInventorySlots / 12);
	}

	private void setNumberOfInventorySlots(int itemInventorySlots) {
		StorageNbt.getOrCreateTag(getBackpackStack()).setInteger(StorageNbt.NUMBER_OF_INVENTORY_SLOTS, itemInventorySlots);
	}

	private void setNumberOfUpgradeSlots(int numberOfUpgradeSlots) {
		StorageNbt.getOrCreateTag(getBackpackStack()).setInteger(StorageNbt.NUMBER_OF_UPGRADE_SLOTS, numberOfUpgradeSlots);
	}

	private NBTTagCompound getBackpackContentsNbt() {
		return BackpackStorage.get().getOrCreateBackpackContents(getOrCreateContentsUuid());
	}

	private void markBackpackContentsDirty() {
		BackpackStorage.get().markDirty();
	}

	@Override
	public void copyDataTo(IStorageWrapper otherStorageWrapper) {
		getContentsUuid().ifPresent(originalUuid -> {
			getInventoryHandler().copyStacksTo(otherStorageWrapper.getInventoryHandler());
			getUpgradeHandler().copyTo(otherStorageWrapper.getUpgradeHandler());
		});
	}

	@Override
	public IBackpackWrapper setBackpackStack(ItemStack backpack) {
		this.backpack = backpack;
		return this;
	}

	@Override
	public Optional<UUID> getContentsUuid() {
		return StorageNbt.getStorageUuid(getBackpackStack());
	}

	private UUID getOrCreateContentsUuid() {
		Optional<UUID> contentsUuid = getContentsUuid();
		if (contentsUuid.isPresent()) {
			return contentsUuid.get();
		}
		if (upgradeHandlerIsDummy) {
			upgradeHandler = null;
		}
		UUID newUuid = UUID.randomUUID();
		setContentsUuid(newUuid);
		return newUuid;
	}

	@Override
	public Optional<Integer> getOpenTabId() {
		NBTTagCompound tag = getBackpackStack().getTagCompound();
		return tag != null && tag.hasKey(StorageNbt.OPEN_TAB_ID) ? Optional.of(tag.getInteger(StorageNbt.OPEN_TAB_ID)) : Optional.empty();
	}

	@Override
	public void setOpenTabId(int openTabId) {
		StorageNbt.getOrCreateTag(getBackpackStack()).setInteger(StorageNbt.OPEN_TAB_ID, openTabId);
		backpackSaveHandler.run();
	}

	@Override
	public void removeOpenTabId() {
		NBTTagCompound tag = getBackpackStack().getTagCompound();
		if (tag != null) {
			tag.removeTag(StorageNbt.OPEN_TAB_ID);
		}
		backpackSaveHandler.run();
	}

	@Override
	public int getMainColor() {
		return StorageNbt.getInt(getBackpackStack(), StorageNbt.MAIN_COLOR, DEFAULT_MAIN_COLOR);
	}

	@Override
	public int getAccentColor() {
		return StorageNbt.getInt(getBackpackStack(), StorageNbt.ACCENT_COLOR, DEFAULT_ACCENT_COLOR);
	}

	@Override
	public void setColors(int mainColor, int accentColor) {
		BackpackItem.setColors(getBackpackStack(), mainColor, accentColor);
		backpackSaveHandler.run();
	}

	@Override
	public ItemStack getBackpack() {
		return getBackpackStack();
	}

	@Override
	public ItemStack cloneBackpack() {
		ItemStack backpackCopy = getBackpack().copy();
		StorageNbt.removeStorageUuid(backpackCopy);
		IBackpackWrapper wrapperCopy = BackpackWrapper.fromStack(backpackCopy);
		copyDataTo(wrapperCopy);
		return wrapperCopy.getBackpack();
	}

	@Override
	public void setPersistent(boolean persistent) {
		getInventoryHandler().setPersistent(persistent);
		getUpgradeHandler().setPersistent(persistent);
	}

	@Override
	public void setSlotNumbers(int numberOfInventorySlots, int numberOfUpgradeSlots) {
		setNumberOfInventorySlots(numberOfInventorySlots);
		setNumberOfUpgradeSlots(numberOfUpgradeSlots);
	}

	@Override
	public void setContentsUuid(UUID storageUuid) {
		StorageNbt.setStorageUuid(getBackpackStack(), storageUuid);
	}

	@Override
	public void removeContentsUuid() {
		getContentsUuid().ifPresent(BackpackStorage.get()::removeBackpackContents);
		removeContentsUUIDTag();
	}

	@Override
	public void removeContentsUUIDTag() {
		StorageNbt.removeStorageUuid(getBackpackStack());
	}

	private ItemStack getBackpackStack() {
		if (backpack == null) {
			throw new IllegalStateException("Backpack stack not set");
		}
		return backpack;
	}

	@Override
	public void setColumnsTaken(int columnsTaken, boolean hasChanged) {
		StorageNbt.getOrCreateTag(getBackpackStack()).setInteger(StorageNbt.COLUMNS_TAKEN, columnsTaken);
		backpackSaveHandler.run();
	}

	@Override
	public int getColumnsTaken() {
		return StorageNbt.getInt(getBackpackStack(), StorageNbt.COLUMNS_TAKEN, 0);
	}

	@Override
	public void onContentsNbtUpdated() {
		handler = null;
		upgradeHandler = null;
	}

	@Override
	public ItemStack getWrappedStorageStack() {
		return getBackpack();
	}

	@Override
	public String getStorageType() {
		return "backpack";
	}

	@Override
	public String getDisplayName() {
		return getBackpack().getDisplayName();
	}
}
