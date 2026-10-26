package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContainer;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackSettingsContainerMenu;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Opens a backpack the player carries: the one in the given slot of the player's inventory, or with -1 the first
 * one found, in the main inventory, then the offhand, then the chest slot. From an open backpack, it also opens the
 * backpack in one of its storage slots, or goes back from such a one to the backpack it's in.
 */
public class BackpackOpenMessage implements IMessage {
	private static final byte INVENTORY = 0;
	private static final byte SUB_BACKPACK = 1;
	private static final byte PARENT = 2;
	private int slotIndex;
	private byte target;

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public BackpackOpenMessage() {
		this(-1);
	}

	public BackpackOpenMessage(int slotIndex) {
		this(slotIndex, INVENTORY);
	}

	private BackpackOpenMessage(int slotIndex, byte target) {
		this.slotIndex = slotIndex;
		this.target = target;
	}

	/** The backpack in the open backpack's storage slot, by the slot's index in the container. */
	public static BackpackOpenMessage subBackpack(int containerSlotIndex) {
		return new BackpackOpenMessage(containerSlotIndex, SUB_BACKPACK);
	}

	/** The backpack the open sub-backpack is in. */
	public static BackpackOpenMessage parent() {
		return new BackpackOpenMessage(-1, PARENT);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		slotIndex = buf.readInt();
		target = buf.readByte();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(slotIndex);
		buf.writeByte(target);
	}

	public static class Handler implements IMessageHandler<BackpackOpenMessage, IMessage> {
		@Override
		public IMessage onMessage(BackpackOpenMessage message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> handle(player, message));
			return null;
		}

		private static void handle(EntityPlayerMP player, BackpackOpenMessage message) {
			if (message.target != INVENTORY) {
				handleInBackpack(player, message);
				return;
			}
			// From the backpack's settings, back to the backpack
			if (player.openContainer instanceof BackpackSettingsContainerMenu) {
				((BackpackSettingsContainerMenu) player.openContainer).getBackpackContext().open(player);
				return;
			}
			int slotIndex = message.slotIndex;
			if (slotIndex >= 0) {
				if (slotIndex < player.inventory.getSizeInventory() && isBackpack(player.inventory.getStackInSlot(slotIndex))) {
					openBackpack(player, slotIndex);
				}
				return;
			}

			// The first one carried, worn ones first, as PlayerInventoryProvider orders them
			PlayerInventoryProvider.runOnBackpacks(player, (backpack, slot) -> {
				openBackpack(player, slot);
				return true;
			});
		}

		/** Only one level deep, as the inception upgrade works: a sub-backpack's own sub-backpacks aren't opened. */
		private static void handleInBackpack(EntityPlayerMP player, BackpackOpenMessage message) {
			if (!(player.openContainer instanceof BackpackContainer)) {
				return;
			}
			BackpackContainer container = (BackpackContainer) player.openContainer;
			BackpackContext context;
			if (message.target == PARENT) {
				context = container.getBackpackContext().getParentBackpackContext();
			} else if (container.isFirstLevelStorage() && container.isStorageInventorySlot(message.slotIndex)) {
				context = container.getBackpackContext().getSubBackpackContext(message.slotIndex);
			} else {
				return;
			}
			if (context.canInteractWith(player)) {
				context.open(player);
			}
		}

		private static boolean isBackpack(ItemStack stack) {
			return stack.getItem() instanceof BackpackItem;
		}

		private static void openBackpack(EntityPlayerMP player, int slot) {
			new BackpackContext.Item(slot).open(player);
		}
	}
}
