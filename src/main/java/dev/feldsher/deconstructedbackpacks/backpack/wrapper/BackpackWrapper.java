package dev.feldsher.deconstructedbackpacks.backpack.wrapper;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackStorage;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.common.gui.SortBy;
import dev.feldsher.deconstructedcore.util.InventorySorter;
import dev.feldsher.deconstructedcore.inventory.InventoryHandler;
import dev.feldsher.deconstructedcore.inventory.StorageWrapperRepository;
import dev.feldsher.deconstructedcore.settings.SettingsHandler;
import dev.feldsher.deconstructedcore.renderdata.RenderInfo;
import dev.feldsher.deconstructedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import dev.feldsher.deconstructedcore.settings.memory.MemorySettingsCategory;
import dev.feldsher.deconstructedcore.settings.nosort.NoSortSettingsCategory;
import dev.feldsher.deconstructedcore.storage.StorageContents;
import dev.feldsher.deconstructedcore.upgrades.UpgradeHandler;
import dev.feldsher.deconstructedcore.upgrades.stack.StackUpgradeItem;
import dev.feldsher.deconstructedcore.util.StorageNbt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A backpack stack and its contents. The stack carries the contents' UUID, its slot counts and colors;
 * the contents themselves are in {@link BackpackStorage}. The inventory and upgrade handlers live with the contents,
 * not here: every stack with the same UUID works on the same ones, and so sees the same backpack.
 * <p>
 * The stack also carries what clients draw it with, its render info, which each stack's wrapper refreshes from the
 * shared contents. Fluids, energy and loot come with the stages that port them.
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
	private static final String SETTINGS_HANDLER = "settings";

	@Nullable
	private UpgradeHandler dummyUpgradeHandler = null;
	// kept here: the contents hold it weakly, so that it goes with this wrapper
	private final Runnable slotChangeListener = () -> inventorySlotChangeHandler.run();
	@Nullable
	private StorageContents listenedContents = null;
	private Runnable upgradeCachesInvalidatedHandler = () -> {
	};
	@Nullable
	private NBTTagCompound parsedRenderInfoTag = null;
	private RenderInfo renderInfo = RenderInfo.EMPTY;

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

	@Override
	public SettingsHandler getSettingsHandler() {
		StorageContents contents = getContents();
		return contents.getShared(SETTINGS_HANDLER, () -> new BackpackSettingsHandler(contents.getNbt(), contents::markDirty, this::getInventoryHandler));
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

	/** Read again only when the stack got another tag, as each sync from the server gives it. */
	@Override
	public RenderInfo getRenderInfo() {
		NBTTagCompound tag = getBackpackStack().getTagCompound();
		NBTTagCompound renderInfoTag = tag != null && tag.hasKey(RenderInfo.RENDER_INFO_TAG) ? tag.getCompoundTag(RenderInfo.RENDER_INFO_TAG) : null;
		if (renderInfoTag != parsedRenderInfoTag) {
			parsedRenderInfoTag = renderInfoTag;
			renderInfo = renderInfoTag == null ? RenderInfo.EMPTY : RenderInfo.deserialize(renderInfoTag);
		}
		return renderInfo;
	}

	/** A backpack never opened has no contents, and nothing to show: its contents aren't made for this. */
	@Override
	public void refreshRenderInfo() {
		if (!getContentsUuid().isPresent()) {
			return;
		}
		ItemDisplaySettingsCategory itemDisplay = getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class);
		NBTTagCompound renderInfoTag = new RenderInfo(itemDisplay.buildItemDisplayRenderInfo()).serialize();
		NBTTagCompound tag = getBackpackStack().getTagCompound();
		NBTTagCompound currentTag = tag != null ? tag.getCompoundTag(RenderInfo.RENDER_INFO_TAG) : new NBTTagCompound();
		if (renderInfoTag.equals(currentTag)) {
			return;
		}
		if (renderInfoTag.isEmpty()) {
			if (tag != null) {
				tag.removeTag(RenderInfo.RENDER_INFO_TAG);
			}
		} else {
			StorageNbt.getOrCreateTag(getBackpackStack()).setTag(RenderInfo.RENDER_INFO_TAG, renderInfoTag);
		}
		backpackSaveHandler.run();
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
		boolean changed = getNumberOfInventorySlots() != numberOfInventorySlots || getNumberOfUpgradeSlots() != numberOfUpgradeSlots;
		setNumberOfInventorySlots(numberOfInventorySlots);
		setNumberOfUpgradeSlots(numberOfUpgradeSlots);
		if (changed) {
			// The shared handlers have the old slot counts: they're built again, from the contents, with the new ones
			getContentsUuid().map(BackpackStorage::getContents).ifPresent(contents -> {
				contents.removeShared(INVENTORY_HANDLER);
				contents.removeShared(UPGRADE_HANDLER);
			});
		}
	}

	@Override
	public void ensureSlotNumbersAtLeast(int numberOfInventorySlots, int numberOfUpgradeSlots) {
		int inventorySlots = Math.max(getNumberOfInventorySlots(), numberOfInventorySlots);
		int upgradeSlots = Math.max(getNumberOfUpgradeSlots(), numberOfUpgradeSlots);
		if (inventorySlots != getNumberOfInventorySlots() || upgradeSlots != getNumberOfUpgradeSlots()) {
			setSlotNumbers(inventorySlots, upgradeSlots);
		}
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
	public SortBy getSortBy() {
		NBTTagCompound tag = getBackpackStack().getTagCompound();
		return tag != null && tag.hasKey(StorageNbt.SORT_BY) ? SortBy.fromName(tag.getString(StorageNbt.SORT_BY)) : SortBy.NAME;
	}

	@Override
	public void setSortBy(SortBy sortBy) {
		StorageNbt.getOrCreateTag(getBackpackStack()).setString(StorageNbt.SORT_BY, sortBy.getSerializedName());
		backpackSaveHandler.run();
	}

	/**
	 * Around the slots that settings leave out of sorting, the memorized slots, which keep what they remember, and the
	 * infinite ones, whose items can't be moved.
	 */
	@Override
	public void sort() {
		Set<Integer> slotIndexesExcludedFromSort = new HashSet<>(getSettingsHandler().getTypeCategory(NoSortSettingsCategory.class).getNoSortSlots());
		slotIndexesExcludedFromSort.addAll(getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).getSlotIndexes());
		InventoryHandler inventoryHandler = getInventoryHandler();
		slotIndexesExcludedFromSort.addAll(inventoryHandler.getInventoryPartitioner().getNoSortSlots());
		for (int slot = 0; slot < inventoryHandler.getSlots(); slot++) {
			if (inventoryHandler.isInfinite(slot)) {
				slotIndexesExcludedFromSort.add(slot);
			}
		}
		InventorySorter.sortHandler(getInventoryHandler(), InventorySorter.getComparator(getSortBy()), slotIndexesExcludedFromSort);
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
