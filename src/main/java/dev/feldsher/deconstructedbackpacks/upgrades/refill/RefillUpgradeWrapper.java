package dev.feldsher.deconstructedbackpacks.upgrades.refill;

import dev.feldsher.deconstructedbackpacks.api.IBlockPickResponseUpgrade;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.InventoryHandler;
import dev.feldsher.deconstructedcore.upgrades.FilterLogic;
import dev.feldsher.deconstructedcore.upgrades.IFilteredUpgrade;
import dev.feldsher.deconstructedcore.upgrades.ITickableUpgrade;
import dev.feldsher.deconstructedcore.upgrades.UpgradeWrapperBase;
import dev.feldsher.deconstructedcore.util.InventoryHelper;
import dev.feldsher.deconstructedcore.util.StorageNbt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Every few ticks tops up to a full stack each item of its filter in the player's inventory, or in its target slot,
 * counting what the player holds on the cursor. An empty filter refills nothing. Not in block form.
 */
public class RefillUpgradeWrapper extends UpgradeWrapperBase<RefillUpgradeWrapper, RefillUpgradeItem>
		implements IFilteredUpgrade, ITickableUpgrade, IBlockPickResponseUpgrade {
	private static final int COOLDOWN = 5;
	private static final String TARGET_SLOTS_TAG = "targetSlots";
	private static final int HOTBAR_AND_MAIN_INVENTORY_SLOTS = 36;

	private final Map<Integer, TargetSlot> targetSlots = new HashMap<>();
	private final FilterLogic filterLogic;

	public RefillUpgradeWrapper(IStorageWrapper backpackWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(backpackWrapper, upgrade, upgradeSaveHandler);
		filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount(), "filter_attributes");
		loadTargetSlots();
		if (upgradeItem.allowsTargetSlotSelection()) {
			FilterLogic.ObservableFilterItemStackHandler filterHandler = filterLogic.getFilterHandler();
			filterHandler.setOnSlotChange(s -> onFilterChange(filterHandler, s));
		}
		filterLogic.setAllowByDefault(true);
	}

	private void loadTargetSlots() {
		NBTTagCompound tag = upgrade.getTagCompound();
		if (tag == null) {
			return;
		}
		NBTTagCompound slotsTag = tag.getCompoundTag(TARGET_SLOTS_TAG);
		for (String key : slotsTag.getKeySet()) {
			try {
				targetSlots.put(Integer.parseInt(key), TargetSlot.fromName(slotsTag.getString(key)));
			}
			catch (NumberFormatException ignored) {
				// not a slot of ours
			}
		}
	}

	private void onFilterChange(FilterLogic.ObservableFilterItemStackHandler filterHandler, int slot) {
		if (filterHandler.getStackInSlot(slot).isEmpty()) {
			targetSlots.remove(slot);
			saveTargetSlots();
		} else if (!targetSlots.containsKey(slot)) {
			setTargetSlot(slot, TargetSlot.ANY);
		}
	}

	public Map<Integer, TargetSlot> getTargetSlots() {
		return targetSlots;
	}

	public void setTargetSlot(int slot, TargetSlot targetSlot) {
		targetSlots.put(slot, targetSlot);
		saveTargetSlots();
	}

	private void saveTargetSlots() {
		NBTTagCompound slotsTag = new NBTTagCompound();
		targetSlots.forEach((slot, targetSlot) -> slotsTag.setString(String.valueOf(slot), targetSlot.getName()));
		StorageNbt.getOrCreateTag(upgrade).setTag(TARGET_SLOTS_TAG, slotsTag);
		save();
	}

	@Override
	public FilterLogic getFilterLogic() {
		return filterLogic;
	}

	@Override
	public void tick(@Nullable Entity entity, World world, BlockPos pos) {
		if (!(entity instanceof EntityPlayer) || isInCooldown(world)) {
			return;
		}
		EntityPlayer player = (EntityPlayer) entity;
		IItemHandler playerInvHandler = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if (playerInvHandler != null) {
			InventoryHelper.iterate(filterLogic.getFilterHandler(), (slot, filter) -> {
				if (!filter.isEmpty()) {
					tryRefillFilter(player, playerInvHandler, filter, targetSlots.getOrDefault(slot, TargetSlot.ANY));
				}
			});
		}
		setCooldown(world, COOLDOWN);
	}

	private void tryRefillFilter(EntityPlayer player, IItemHandler playerInvHandler, ItemStack filter, TargetSlot targetSlot) {
		int missingCount = targetSlot.getMissingCount(player, playerInvHandler, filter);
		ItemStack carried = player.inventory.getItemStack();
		if (ItemHandlerHelper.canItemStacksStack(carried, filter)) {
			missingCount -= Math.min(missingCount, carried.getCount());
		}
		if (missingCount <= 0) {
			return;
		}
		InventoryHandler extractFromHandler = storageWrapper.getInventoryForUpgradeProcessing();
		ItemStack extracted = InventoryHelper.extractFromInventory(ItemHandlerHelper.copyStackWithSize(filter, missingCount), extractFromHandler, true);
		if (extracted.isEmpty()) {
			return;
		}
		ItemStack remaining = targetSlot.fill(player, playerInvHandler, extracted.copy());
		if (remaining.getCount() != extracted.getCount()) {
			InventoryHelper.extractFromInventory(ItemHandlerHelper.copyStackWithSize(extracted, extracted.getCount() - remaining.getCount()), extractFromHandler, false);
		}
	}

	public boolean allowsTargetSlotSelection() {
		return upgradeItem.allowsTargetSlotSelection();
	}

	/**
	 * Takes a stack of the picked item out of the backpack into the player's hand. What the hand held goes where the
	 * picked stack was when it fits there, else into the backpack, else into the player's inventory.
	 */
	@Override
	public boolean pickBlock(EntityPlayer player, ItemStack filter) {
		if (!upgradeItem.supportsBlockPick()) {
			return false;
		}

		InventoryHandler inventoryHandler = storageWrapper.getInventoryForUpgradeProcessing();
		int stashSlot = -1;
		for (int slot = 0; slot < inventoryHandler.getSlots(); slot++) {
			if (ItemHandlerHelper.canItemStacksStack(inventoryHandler.getStackInSlot(slot), filter)) {
				stashSlot = slot;
				break;
			}
		}

		ItemStack mainHandItem = player.getHeldItemMainhand();
		ItemStack toExtract = ItemHandlerHelper.copyStackWithSize(filter, filter.getMaxStackSize());
		if (stashSlot < 0 || InventoryHelper.extractFromInventory(toExtract, inventoryHandler, true).isEmpty()) {
			return false;
		}
		if ((inventoryHandler.getStackInSlot(stashSlot).getCount() > filter.getMaxStackSize() || !inventoryHandler.isItemValid(stashSlot, mainHandItem))
				&& !inventoryHandler.insertItem(mainHandItem, true).isEmpty()) {
			if (!canMoveMainHandToInventory(player)) {
				player.sendStatusMessage(new TextComponentTranslation("gui.deconstructedbackpacks.status.no_space_for_mainhand_item"), true);
				return false;
			}
			ItemStack extracted = InventoryHelper.extractFromInventory(toExtract, inventoryHandler, false);
			player.setHeldItem(EnumHand.MAIN_HAND, extracted);
			player.inventory.addItemStackToInventory(mainHandItem);
			return true;
		}
		ItemStack extracted = InventoryHelper.extractFromInventory(toExtract, inventoryHandler, false);
		inventoryHandler.insertItem(mainHandItem, false);
		player.setHeldItem(EnumHand.MAIN_HAND, extracted);
		return true;
	}

	private static boolean canMoveMainHandToInventory(EntityPlayer player) {
		ItemStack mainHandItem = player.getHeldItemMainhand();
		int countToAdd = mainHandItem.getCount();
		for (int slot = 0; slot < HOTBAR_AND_MAIN_INVENTORY_SLOTS; slot++) {
			if (slot == player.inventory.currentItem) {
				continue;
			}
			ItemStack slotStack = player.inventory.getStackInSlot(slot);
			if (slotStack.isEmpty()) {
				return true;
			} else if (ItemHandlerHelper.canItemStacksStack(slotStack, mainHandItem)) {
				countToAdd -= slotStack.getMaxStackSize() - slotStack.getCount();
				if (countToAdd <= 0) {
					return true;
				}
			}
		}
		return false;
	}
}
