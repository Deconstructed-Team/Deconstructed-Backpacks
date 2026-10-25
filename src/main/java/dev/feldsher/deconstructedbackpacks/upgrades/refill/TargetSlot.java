package dev.feldsher.deconstructedbackpacks.upgrades.refill;

import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import dev.feldsher.deconstructedcore.util.InventoryHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.function.ToIntFunction;

/** Where the refill upgrade keeps a filter's item: anywhere in the player's inventory, or in one slot of it. */
public enum TargetSlot implements IStringSerializable {
	ANY("any", null),
	MAIN_HAND("main_hand", player -> player.inventory.currentItem),
	OFF_HAND("off_hand", player -> PlayerInventoryProvider.OFFHAND_SLOT),
	TOOLBAR_1("toolbar_1", player -> 0),
	TOOLBAR_2("toolbar_2", player -> 1),
	TOOLBAR_3("toolbar_3", player -> 2),
	TOOLBAR_4("toolbar_4", player -> 3),
	TOOLBAR_5("toolbar_5", player -> 4),
	TOOLBAR_6("toolbar_6", player -> 5),
	TOOLBAR_7("toolbar_7", player -> 6),
	TOOLBAR_8("toolbar_8", player -> 7),
	TOOLBAR_9("toolbar_9", player -> 8);

	private static final String HOTBAR_TOOLTIP = "refill.target_slot.hotbar.tooltip";
	private static final TargetSlot[] VALUES = values();

	private final String name;
	/** The slot of the player's inventory, or none for anywhere in it. */
	private final ToIntFunction<EntityPlayer> getSlot;

	TargetSlot(String name, ToIntFunction<EntityPlayer> getSlot) {
		this.name = name;
		this.getSlot = getSlot;
	}

	@Override
	public String getName() {
		return name;
	}

	public TargetSlot next() {
		return VALUES[(ordinal() + 1) % VALUES.length];
	}

	public TargetSlot previous() {
		return VALUES[Math.floorMod(ordinal() - 1, VALUES.length)];
	}

	public static TargetSlot fromName(String name) {
		for (TargetSlot value : VALUES) {
			if (value.name.equals(name)) {
				return value;
			}
		}
		return ANY;
	}

	private boolean isHotbarSlot() {
		return ordinal() >= TOOLBAR_1.ordinal();
	}

	private int getHotbarNumber() {
		return ordinal() - TOOLBAR_1.ordinal() + 1;
	}

	/** The letter or number drawn over the filter slot. */
	public ITextComponent getAcronym() {
		return isHotbarSlot() ? new TextComponentString(String.valueOf(getHotbarNumber())) : SBPTranslationHelper.INSTANCE.translUpgrade("refill.target_slot." + name);
	}

	public ITextComponent getDescription() {
		ITextComponent description = isHotbarSlot() ? SBPTranslationHelper.INSTANCE.translUpgrade(HOTBAR_TOOLTIP, getHotbarNumber())
				: new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translUpgradeKey("refill.target_slot." + name) + ".tooltip");
		description.getStyle().setColor(TextFormatting.DARK_GREEN);
		return description;
	}

	/** How many of the filter's item the target lacks to a full stack. */
	int getMissingCount(EntityPlayer player, IItemHandler playerInventory, ItemStack filter) {
		if (getSlot == null) {
			return InventoryHelper.getCountMissingInHandler(playerInventory, filter, filter.getMaxStackSize());
		}
		ItemStack stack = player.inventory.getStackInSlot(getSlot.applyAsInt(player));
		return ItemHandlerHelper.canItemStacksStack(stack, filter) ? filter.getMaxStackSize() - stack.getCount() : filter.getMaxStackSize();
	}

	/** Puts what it can of the stack into the target; the rest is returned. */
	ItemStack fill(EntityPlayer player, IItemHandler playerInventory, ItemStack stackToAdd) {
		if (getSlot == null) {
			return refillAnywhereInInventory(playerInventory, stackToAdd);
		}
		int slot = getSlot.applyAsInt(player);
		ItemStack contents = player.inventory.getStackInSlot(slot);
		if (contents.isEmpty()) {
			player.inventory.setInventorySlotContents(slot, stackToAdd);
			return ItemStack.EMPTY;
		}
		if (ItemHandlerHelper.canItemStacksStack(contents, stackToAdd)) {
			contents.grow(stackToAdd.getCount());
			return ItemStack.EMPTY;
		}
		return stackToAdd;
	}

	/** Onto the stacks of the item the player has first, then wherever it fits. */
	private static ItemStack refillAnywhereInInventory(IItemHandler playerInventory, ItemStack extracted) {
		ItemStack remaining = extracted;
		for (int slot = 0; slot < playerInventory.getSlots() && !remaining.isEmpty(); slot++) {
			if (ItemHandlerHelper.canItemStacksStack(playerInventory.getStackInSlot(slot), remaining)) {
				remaining = playerInventory.insertItem(slot, remaining, false);
			}
		}
		if (remaining.isEmpty()) {
			return remaining;
		}
		return InventoryHelper.insertIntoInventory(remaining, playerInventory, false);
	}
}
