package dev.feldsher.deconstructedbackpacks.util;

import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.api.IItemHandlerInteractionUpgrade;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/** The backpack's inventory interaction upgrades, run on the inventory of a block the player uses it on. */
public class InventoryInteractionHelper {
	private InventoryInteractionHelper() {
	}

	/** Whether the block has an inventory the upgrades can work with; the client only asks, the server runs them. */
	public static boolean tryInventoryInteraction(BlockPos pos, World world, ItemStack backpack, EnumFacing face, EntityPlayer player) {
		if (Config.SERVER.isBlockInteractionDisallowed(world.getBlockState(pos).getBlock())) {
			return false;
		}
		IItemHandler itemHandler = getItemHandler(world, pos, face);
		if (itemHandler == null) {
			return false;
		}
		return world.isRemote || tryRunningInteractionWrappers(itemHandler, BackpackWrapper.fromStack(backpack), player);
	}

	public static boolean hasItemHandler(World world, BlockPos pos, EnumFacing face) {
		return getItemHandler(world, pos, face) != null;
	}

	private static IItemHandler getItemHandler(World world, BlockPos pos, EnumFacing face) {
		TileEntity te = world.getTileEntity(pos);
		if (te == null || !te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face)) {
			return null;
		}
		return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
	}

	private static boolean tryRunningInteractionWrappers(IItemHandler itemHandler, IStorageWrapper wrapper, EntityPlayer player) {
		List<IItemHandlerInteractionUpgrade> wrappers = wrapper.getUpgradeHandler().getWrappersThatImplement(IItemHandlerInteractionUpgrade.class);
		if (wrappers.isEmpty()) {
			return false;
		}
		wrappers.forEach(upgrade -> upgrade.onHandlerInteract(itemHandler, player));
		return true;
	}
}
