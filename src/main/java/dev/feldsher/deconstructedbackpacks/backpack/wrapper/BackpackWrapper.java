package dev.feldsher.deconstructedbackpacks.backpack.wrapper;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackStorage;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.InventoryHandler;
import dev.feldsher.deconstructedcore.inventory.StorageWrapperRepository;
import dev.feldsher.deconstructedcore.storage.StorageContents;
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
 * the contents themselves are in {@link BackpackStorage}. The inventory and upgrade handlers live with the contents,
 * not here: every stack with the same UUID works on the same ones, and so sees the same backpack.
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

	private static final String INVENTORY_HANDLER = "inventory";
	private static final String UPGRADE_HANDLER = "upgrades";

	@Nullable
	private UpgradeHandler dummyUpgradeHandler = null;
	// kept here: the contents hold it weakly, so that it goes with this wrapper
	private final Runnable slotChangeListener = () -> inventorySlotChangeHandler.run();
	@Nullable
	private StorageContents listenedContents = null;
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
		StorageContents contents = getContents();
		if (listenedContents != contents) {
			contents.addSlotChangeListener(this, slotChangeListener);
			listenedContents = contents;
		}
		return contents.getShared(INVENTORY_HANDLER, () -> new BackpackInventoryHandler(getNumberOfInventorySlots() - (getNumberOfSlotRows() * getColumnsTaken()),
				this, contents.getNbt(), contents::onSlotChanged, StackUpgradeItem.getInventorySlotLimit(this)));
	}

	/**
	 * A backpack whose contents don't exist yet has no upgrades: it gets an empty handler, as 1.21's Noop, so that
	 * ticking every backpack in an inventory doesn't create contents for each.
	 */
	@Override
	public UpgradeHandler getUpgradeHandler() {
		if (!getContentsUuid().isPresent()) {
			if (dummyUpgradeHandler == null) {
				dummyUpgradeHandler = new UpgradeHandler(0, this, new NBTTagCompound(), () -> {
				}, () -> {
				});
				dummyUpgradeHandler.setPersistent(false);
			}
			return dummyUpgradeHandler;
		}
		dummyUpgradeHandler = null;
		StorageContents contents = getContents();
		return contents.getShared(UPGRADE_HANDLER, () -> new UpgradeHandler(getNumberOfUpgradeSlots(), this, contents.getNbt(), contents::markDirty, this::onUpgradesChanged) {
			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return super.isItemValid(slot, stack) && (stack.isEmpty() || ModItems.isBackpackUpgrade(stack.getItem()));
			}
		});
	}

	private void onUpgradesChanged() {
		getInventoryHandler().setBaseSlotLimit(StackUpgradeItem.getInventorySlotLimit(this));
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

	private StorageContents getContents() {
		return BackpackStorage.getContents(getOrCreateContentsUuid());
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
		getContentsUuid().ifPresent(BackpackStorage::removeBackpackContents);
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

	/** The client's contents and their handlers were replaced as a whole when the server sent them: nothing to drop here. */
	@Override
	public void onContentsNbtUpdated() {
		//noop
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
