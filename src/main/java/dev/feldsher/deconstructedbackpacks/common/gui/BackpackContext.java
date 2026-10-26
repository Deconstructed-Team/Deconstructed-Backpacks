package dev.feldsher.deconstructedbackpacks.common.gui;

import dev.feldsher.deconstructedbackpacks.network.BackpackSlotSyncMessage;
import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import javax.annotation.Nullable;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.Entity;
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
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.InventoryHandler;

import java.util.Optional;

/**
 * Where an open backpack is: carried, placed, worn by another player, or in a storage slot of any of these, a
 * sub-backpack.
 */
public abstract class BackpackContext {
	public static final String SUBBACKPACK_DISPLAY_NAME_PREFIX = "... > ";

	/** The backpack this one is open from inside of, if it is. */
	public Optional<IStorageWrapper> getParentBackpackWrapper(EntityPlayer player) {
		return Optional.empty();
	}

	/** The backpack in the given storage slot of this one. */
	public abstract BackpackContext getSubBackpackContext(int subBackpackSlotIndex);

	/** The backpack this one is in, or this one itself. */
	public BackpackContext getParentBackpackContext() {
		return this;
	}

	public String getDisplayName(EntityPlayer player) {
		return getBackpackWrapper(player).getDisplayName();
	}

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

	/** A backpack the player carries, by its slot as {@link PlayerInventoryProvider} numbers them, Baubles' too. */
	public static class Item extends BackpackContext {
		protected final int backpackSlotIndex;

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
			return PlayerInventoryProvider.getStackInSlot(player, backpackSlotIndex);
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
		public BackpackContext getSubBackpackContext(int subBackpackSlotIndex) {
			return new ItemSubBackpack(backpackSlotIndex, subBackpackSlotIndex);
		}

		@Override
		protected void openScreen(EntityPlayer player, boolean settings) {
			if (player instanceof EntityPlayerMP) {
				sendBackpack((EntityPlayerMP) player);
			}
			player.openGui(DeconstructedBackpacks.instance, settings ? GuiHandler.ITEM_BACKPACK_SETTINGS : GuiHandler.ITEM_BACKPACK, player.world, backpackSlotIndex, 0, 0);
		}

		protected void sendBackpack(EntityPlayerMP player) {
			ItemStack backpack = getBackpackStack(player);
			sendContents(player, BackpackWrapper.fromStack(backpack));
			if (PlayerInventoryProvider.isPlayerInventorySlot(backpackSlotIndex)) {
				// windowId -2: a slot of the player's inventory, whichever container is open
				player.connection.sendPacket(new SPacketSetSlot(-2, backpackSlotIndex, backpack));
			} else {
				PacketHandler.INSTANCE.sendTo(new BackpackSlotSyncMessage(player.getEntityId(), backpackSlotIndex, backpack), player);
			}
		}
	}

	/** A backpack in a storage slot of a carried one. */
	public static class ItemSubBackpack extends Item {
		private final int subBackpackSlotIndex;

		public ItemSubBackpack(int backpackSlotIndex, int subBackpackSlotIndex) {
			super(backpackSlotIndex);
			this.subBackpackSlotIndex = subBackpackSlotIndex;
		}

		@Override
		public Optional<IStorageWrapper> getParentBackpackWrapper(EntityPlayer player) {
			return Optional.of(super.getBackpackWrapper(player));
		}

		@Override
		public IBackpackWrapper getBackpackWrapper(EntityPlayer player) {
			return BackpackWrapper.fromStack(getSubBackpackStack(super.getBackpackWrapper(player), subBackpackSlotIndex));
		}

		@Override
		public boolean canInteractWith(EntityPlayer player) {
			return super.canInteractWith(player) && isSubBackpack(super.getBackpackWrapper(player), subBackpackSlotIndex);
		}

		@Override
		public BackpackContext getParentBackpackContext() {
			return new Item(backpackSlotIndex);
		}

		@Override
		public String getDisplayName(EntityPlayer player) {
			return SUBBACKPACK_DISPLAY_NAME_PREFIX + super.getDisplayName(player);
		}

		@Override
		protected void openScreen(EntityPlayer player, boolean settings) {
			if (player instanceof EntityPlayerMP) {
				sendBackpack((EntityPlayerMP) player);
				sendContents((EntityPlayerMP) player, getBackpackWrapper(player));
			}
			player.openGui(DeconstructedBackpacks.instance, settings ? GuiHandler.ITEM_SUB_BACKPACK_SETTINGS : GuiHandler.ITEM_SUB_BACKPACK, player.world,
					backpackSlotIndex, subBackpackSlotIndex, 0);
		}
	}

	/**
	 * The backpack another player wears, which the config and that player's setting let others open: the first worn one,
	 * in Baubles, else in the chest slot.
	 */
	public static class AnotherPlayer extends BackpackContext {
		private static final double MAX_DISTANCE_SQ = 8 * 8;
		protected final int otherPlayerId;

		public AnotherPlayer(int otherPlayerId) {
			this.otherPlayerId = otherPlayerId;
		}

		@Nullable
		protected EntityPlayer getOtherPlayer(EntityPlayer player) {
			Entity entity = player.world.getEntityByID(otherPlayerId);
			return entity instanceof EntityPlayer && entity != player ? (EntityPlayer) entity : null;
		}

		protected ItemStack getBackpackStack(EntityPlayer player) {
			EntityPlayer otherPlayer = getOtherPlayer(player);
			return otherPlayer == null ? ItemStack.EMPTY : PlayerInventoryProvider.getStackInSlot(otherPlayer, getWornBackpackSlot(otherPlayer));
		}

		private static int getWornBackpackSlot(EntityPlayer otherPlayer) {
			int[] wornSlot = {-1};
			PlayerInventoryProvider.runOnBackpacks(otherPlayer, (backpack, slot) -> {
				wornSlot[0] = slot;
				return true;
			}, true);
			return wornSlot[0];
		}

		@Override
		public boolean shouldLockBackpackSlot(EntityPlayer player) {
			return false;
		}

		@Override
		public IBackpackWrapper getBackpackWrapper(EntityPlayer player) {
			return BackpackWrapper.fromStack(getBackpackStack(player));
		}

		@Override
		public int getBackpackSlotIndex() {
			return -1;
		}

		@Override
		public boolean canInteractWith(EntityPlayer player) {
			EntityPlayer otherPlayer = getOtherPlayer(player);
			return otherPlayer != null && otherPlayer.isEntityAlive() && player.getDistanceSq(otherPlayer) < MAX_DISTANCE_SQ
					&& getBackpackStack(player).getItem() instanceof BackpackItem;
		}

		@Override
		public BackpackContext getSubBackpackContext(int subBackpackSlotIndex) {
			return new AnotherPlayerSubBackpack(otherPlayerId, subBackpackSlotIndex);
		}

		/**
		 * Its contents, and the stack as it is now: the client has the other player's equipment only as the tracker
		 * sends it, a tick late, and builds the container's slots from the stack, as a backpack reopened for an upgrade
		 * that takes columns needs at once.
		 */
		protected void sendBackpack(EntityPlayerMP player) {
			EntityPlayer otherPlayer = getOtherPlayer(player);
			if (otherPlayer == null) {
				return;
			}
			int slot = getWornBackpackSlot(otherPlayer);
			ItemStack backpack = PlayerInventoryProvider.getStackInSlot(otherPlayer, slot);
			sendContents(player, BackpackWrapper.fromStack(backpack));
			if (slot == PlayerInventoryProvider.CHEST_SLOT) {
				player.connection.sendPacket(new SPacketEntityEquipment(otherPlayerId, EntityEquipmentSlot.CHEST, backpack));
			} else {
				PacketHandler.INSTANCE.sendTo(new BackpackSlotSyncMessage(otherPlayerId, slot, backpack), player);
			}
		}

		@Override
		protected void openScreen(EntityPlayer player, boolean settings) {
			if (player instanceof EntityPlayerMP) {
				sendBackpack((EntityPlayerMP) player);
			}
			player.openGui(DeconstructedBackpacks.instance, settings ? GuiHandler.ANOTHER_PLAYER_BACKPACK_SETTINGS : GuiHandler.ANOTHER_PLAYER_BACKPACK, player.world,
					otherPlayerId, 0, 0);
		}
	}

	/** A backpack in a storage slot of the one another player wears. */
	public static class AnotherPlayerSubBackpack extends AnotherPlayer {
		private final int subBackpackSlotIndex;

		public AnotherPlayerSubBackpack(int otherPlayerId, int subBackpackSlotIndex) {
			super(otherPlayerId);
			this.subBackpackSlotIndex = subBackpackSlotIndex;
		}

		@Override
		public Optional<IStorageWrapper> getParentBackpackWrapper(EntityPlayer player) {
			return Optional.of(super.getBackpackWrapper(player));
		}

		@Override
		public IBackpackWrapper getBackpackWrapper(EntityPlayer player) {
			return BackpackWrapper.fromStack(getSubBackpackStack(super.getBackpackWrapper(player), subBackpackSlotIndex));
		}

		@Override
		public boolean canInteractWith(EntityPlayer player) {
			return super.canInteractWith(player) && isSubBackpack(super.getBackpackWrapper(player), subBackpackSlotIndex);
		}

		@Override
		public BackpackContext getParentBackpackContext() {
			return new AnotherPlayer(otherPlayerId);
		}

		@Override
		public String getDisplayName(EntityPlayer player) {
			return SUBBACKPACK_DISPLAY_NAME_PREFIX + super.getDisplayName(player);
		}

		@Override
		protected void openScreen(EntityPlayer player, boolean settings) {
			if (player instanceof EntityPlayerMP) {
				super.sendBackpack((EntityPlayerMP) player);
				sendContents((EntityPlayerMP) player, getBackpackWrapper(player));
			}
			player.openGui(DeconstructedBackpacks.instance, settings ? GuiHandler.ANOTHER_PLAYER_SUB_BACKPACK_SETTINGS : GuiHandler.ANOTHER_PLAYER_SUB_BACKPACK, player.world,
					otherPlayerId, subBackpackSlotIndex, 0);
		}
	}

	private static boolean isSubBackpack(IStorageWrapper parent, int subBackpackSlotIndex) {
		if (!parent.getContentsUuid().isPresent()) {
			return false;
		}
		InventoryHandler inventory = parent.getInventoryHandler();
		return subBackpackSlotIndex >= 0 && subBackpackSlotIndex < inventory.getSlots() && inventory.getStackInSlot(subBackpackSlotIndex).getItem() instanceof BackpackItem;
	}

	private static ItemStack getSubBackpackStack(IStorageWrapper parent, int subBackpackSlotIndex) {
		if (!isSubBackpack(parent, subBackpackSlotIndex)) {
			throw new IllegalStateException("No backpack in slot " + subBackpackSlotIndex);
		}
		return parent.getInventoryHandler().getStackInSlot(subBackpackSlotIndex);
	}

	/** A placed backpack. */
	public static class Block extends BackpackContext {
		protected final BlockPos pos;

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
		public BackpackContext getSubBackpackContext(int subBackpackSlotIndex) {
			return new BlockSubBackpack(pos, subBackpackSlotIndex);
		}

		@Override
		protected void openScreen(EntityPlayer player, boolean settings) {
			if (player instanceof EntityPlayerMP) {
				sendBackpack((EntityPlayerMP) player);
			}
			player.openGui(DeconstructedBackpacks.instance, settings ? GuiHandler.BLOCK_BACKPACK_SETTINGS : GuiHandler.BLOCK_BACKPACK, player.world, pos.getX(), pos.getY(), pos.getZ());
		}

		protected void sendBackpack(EntityPlayerMP player) {
			TileEntity te = player.world.getTileEntity(pos);
			if (te instanceof BackpackBlockEntity) {
				sendContents(player, ((BackpackBlockEntity) te).getBackpackWrapper());
				SPacketUpdateTileEntity updatePacket = te.getUpdatePacket();
				if (updatePacket != null) {
					player.connection.sendPacket(updatePacket);
				}
			}
		}
	}

	/** A backpack in a storage slot of a placed one. */
	public static class BlockSubBackpack extends Block {
		private final int subBackpackSlotIndex;

		public BlockSubBackpack(BlockPos pos, int subBackpackSlotIndex) {
			super(pos);
			this.subBackpackSlotIndex = subBackpackSlotIndex;
		}

		@Override
		public Optional<IStorageWrapper> getParentBackpackWrapper(EntityPlayer player) {
			return Optional.of(super.getBackpackWrapper(player));
		}

		@Override
		public IBackpackWrapper getBackpackWrapper(EntityPlayer player) {
			return BackpackWrapper.fromStack(getSubBackpackStack(super.getBackpackWrapper(player), subBackpackSlotIndex));
		}

		@Override
		public boolean canInteractWith(EntityPlayer player) {
			return super.canInteractWith(player) && isSubBackpack(super.getBackpackWrapper(player), subBackpackSlotIndex);
		}

		@Override
		public BackpackContext getParentBackpackContext() {
			return new Block(pos);
		}

		@Override
		public String getDisplayName(EntityPlayer player) {
			return SUBBACKPACK_DISPLAY_NAME_PREFIX + super.getDisplayName(player);
		}

		@Override
		protected void openScreen(EntityPlayer player, boolean settings) {
			if (player instanceof EntityPlayerMP) {
				sendBackpack((EntityPlayerMP) player);
				sendContents((EntityPlayerMP) player, getBackpackWrapper(player));
			}
			// 1.12's world height fits in 8 bits: the slot goes above it
			player.openGui(DeconstructedBackpacks.instance, settings ? GuiHandler.BLOCK_SUB_BACKPACK_SETTINGS : GuiHandler.BLOCK_SUB_BACKPACK, player.world,
					pos.getX(), pos.getY() | subBackpackSlotIndex << 8, pos.getZ());
		}
	}
}
