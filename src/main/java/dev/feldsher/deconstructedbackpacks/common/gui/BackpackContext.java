package dev.feldsher.deconstructedbackpacks.common.gui;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackBlockEntity;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackStorage;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedbackpacks.network.BackpackContentsMessage;
import dev.feldsher.deconstructedbackpacks.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Where an open backpack is. Sub-backpack and another player's backpack contexts come with the stages that port them.
 */
public abstract class BackpackContext {

	public abstract boolean shouldLockBackpackSlot(EntityPlayer player);

	public abstract IBackpackWrapper getBackpackWrapper(EntityPlayer player);

	public abstract int getBackpackSlotIndex();

	public abstract boolean canInteractWith(EntityPlayer player);

	/**
	 * Opens this backpack's container for the player, on the server. The client gets the backpack and its contents
	 * first, so that it builds the container with the same upgrades, and so the same slots, as the server.
	 */
	public void open(EntityPlayer player) {
		openScreen(player, false);
	}

	/** Opens this backpack's settings, as {@link #open} its container. */
	public void openSettings(EntityPlayer player) {
		openScreen(player, true);
	}

	protected abstract void openScreen(EntityPlayer player, boolean settings);

	protected static void sendContents(EntityPlayerMP player, IBackpackWrapper wrapper) {
		// Contents are created with their UUID the first time the backpack is opened
		wrapper.getInventoryHandler();
		wrapper.getContentsUuid().ifPresent(uuid ->
				PacketHandler.INSTANCE.sendTo(new BackpackContentsMessage(uuid, BackpackStorage.getOrCreateBackpackContents(uuid)), player));
	}

	/** A backpack in the player's inventory, by its slot there: 0-35 the main inventory, 36-39 armor, 40 the offhand. */
	public static class Item extends BackpackContext {
		private final int backpackSlotIndex;

		public Item(int backpackSlotIndex) {
			this.backpackSlotIndex = backpackSlotIndex;
		}

		@Override
		public boolean shouldLockBackpackSlot(EntityPlayer player) {
			// The container shows the main inventory alone
			return backpackSlotIndex < player.inventory.mainInventory.size();
		}

		@Override
		public IBackpackWrapper getBackpackWrapper(EntityPlayer player) {
			return BackpackWrapper.fromStack(getBackpackStack(player));
		}

		private ItemStack getBackpackStack(EntityPlayer player) {
			return player.inventory.getStackInSlot(backpackSlotIndex);
		}

		@Override
		public int getBackpackSlotIndex() {
			return backpackSlotIndex;
		}

		@Override
		public boolean canInteractWith(EntityPlayer player) {
			return getBackpackStack(player).getItem() instanceof BackpackItem;
		}

		@Override
		protected void openScreen(EntityPlayer player, boolean settings) {
			if (player instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;
				ItemStack backpack = getBackpackStack(player);
				sendContents(playerMP, BackpackWrapper.fromStack(backpack));
				// windowId -2: a slot of the player's inventory, whichever container is open
				playerMP.connection.sendPacket(new SPacketSetSlot(-2, backpackSlotIndex, backpack));
			}
			player.openGui(DeconstructedBackpacks.instance, settings ? GuiHandler.ITEM_BACKPACK_SETTINGS : GuiHandler.ITEM_BACKPACK, player.world, backpackSlotIndex, 0, 0);
		}
	}

	/** A placed backpack. */
	public static class Block extends BackpackContext {
		private final BlockPos pos;

		public Block(BlockPos pos) {
			this.pos = pos;
		}

		public BlockPos getPos() {
			return pos;
		}

		@Override
		public boolean shouldLockBackpackSlot(EntityPlayer player) {
			return false;
		}

		@Override
		public IBackpackWrapper getBackpackWrapper(EntityPlayer player) {
			TileEntity te = player.world.getTileEntity(pos);
			if (!(te instanceof BackpackBlockEntity)) {
				throw new IllegalStateException("No backpack at " + pos);
			}
			return ((BackpackBlockEntity) te).getBackpackWrapper();
		}

		@Override
		public int getBackpackSlotIndex() {
			return -1;
		}

		@Override
		public boolean canInteractWith(EntityPlayer player) {
			return player.world.getTileEntity(pos) instanceof BackpackBlockEntity
					&& player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
		}

		@Override
		protected void openScreen(EntityPlayer player, boolean settings) {
			TileEntity te = player.world.getTileEntity(pos);
			if (player instanceof EntityPlayerMP && te instanceof BackpackBlockEntity) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;
				sendContents(playerMP, ((BackpackBlockEntity) te).getBackpackWrapper());
				SPacketUpdateTileEntity updatePacket = te.getUpdatePacket();
				if (updatePacket != null) {
					playerMP.connection.sendPacket(updatePacket);
				}
			}
			player.openGui(DeconstructedBackpacks.instance, settings ? GuiHandler.BLOCK_BACKPACK_SETTINGS : GuiHandler.BLOCK_BACKPACK, player.world, pos.getX(), pos.getY(), pos.getZ());
		}
	}
}
