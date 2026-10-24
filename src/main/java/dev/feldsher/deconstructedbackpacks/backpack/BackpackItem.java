package dev.feldsher.deconstructedbackpacks.backpack;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContainer;
import dev.feldsher.deconstructedbackpacks.common.gui.GuiHandler;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import dev.feldsher.deconstructedcore.util.StorageNbt;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * A backpack: a storage carried as an item, opened from the hand.
 * Placing it as a block, wearing it, stashing items into it from a slot and the upgrades' ticking come with the
 * stages that port them.
 */
public class BackpackItem extends Item {
	private final IntSupplier numberOfSlots;
	private final IntSupplier numberOfUpgradeSlots;

	public BackpackItem(String name, IntSupplier numberOfSlots, IntSupplier numberOfUpgradeSlots) {
		this.numberOfSlots = numberOfSlots;
		this.numberOfUpgradeSlots = numberOfUpgradeSlots;
		setRegistryName(DeconstructedBackpacks.getRL(name));
		setTranslationKey(DeconstructedBackpacks.MOD_ID + "." + name);
		setMaxStackSize(1);
		setCreativeTab(ModItems.CREATIVE_TAB);
	}

	public static void setColors(ItemStack backpackStack, int mainColor, int accentColor) {
		StorageNbt.getOrCreateTag(backpackStack).setInteger(StorageNbt.MAIN_COLOR, mainColor);
		StorageNbt.getOrCreateTag(backpackStack).setInteger(StorageNbt.ACCENT_COLOR, accentColor);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (!isInCreativeTab(tab)) {
			return;
		}
		items.add(new ItemStack(this));

		if (this != ModItems.BACKPACK) {
			return;
		}

		for (EnumDyeColor color : EnumDyeColor.values()) {
			ItemStack stack = new ItemStack(this);
			setColors(stack, color.getColorValue(), color.getColorValue());
			items.add(stack);
		}
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		super.addInformation(stack, world, tooltip, flag);
		if (flag.isAdvanced()) {
			BackpackWrapper.fromStack(stack).getContentsUuid()
					.ifPresent(uuid -> tooltip.add(TextFormatting.DARK_GRAY + "UUID: " + uuid));
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		if (!world.isRemote) {
			int slot = hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : GuiHandler.OFFHAND_SLOT;
			player.openGui(DeconstructedBackpacks.instance, GuiHandler.ITEM_BACKPACK, world, slot, 0, 0);
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	public int getNumberOfSlots() {
		return numberOfSlots.getAsInt();
	}

	public int getNumberOfUpgradeSlots() {
		return numberOfUpgradeSlots.getAsInt();
	}

	@Nullable
	@Override
	public EntityEquipmentSlot getEquipmentSlot(ItemStack stack) {
		return EntityEquipmentSlot.CHEST;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged;
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
		return !(player.openContainer instanceof BackpackContainer && ((BackpackContainer) player.openContainer).getStorageWrapper().getBackpack() == item);
	}
}
