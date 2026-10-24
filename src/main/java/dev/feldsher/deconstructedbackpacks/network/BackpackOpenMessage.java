package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContext;
import dev.feldsher.deconstructedbackpacks.common.gui.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackSettingsContainerMenu;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Opens a backpack the player carries: the one in the given slot of the player's inventory, or with -1 the first
 * one found, in the main inventory, then the offhand, then the chest slot.
 */
public class BackpackOpenMessage implements IMessage {
	private static final int CHEST_SLOT = 38;
	private int slotIndex;

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public BackpackOpenMessage() {
		this(-1);
	}

	public BackpackOpenMessage(int slotIndex) {
		this.slotIndex = slotIndex;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		slotIndex = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(slotIndex);
	}

	public static class Handler implements IMessageHandler<BackpackOpenMessage, IMessage> {
		@Override
		public IMessage onMessage(BackpackOpenMessage message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> handle(player, message.slotIndex));
			return null;
		}

		private static void handle(EntityPlayerMP player, int slotIndex) {
			// From the backpack's settings, back to the backpack
			if (player.openContainer instanceof BackpackSettingsContainerMenu) {
				((BackpackSettingsContainerMenu) player.openContainer).getBackpackContext().open(player);
				return;
			}
			if (slotIndex >= 0) {
				if (slotIndex < player.inventory.getSizeInventory() && isBackpack(player.inventory.getStackInSlot(slotIndex))) {
					openBackpack(player, slotIndex);
				}
				return;
			}

			for (int slot = 0; slot < player.inventory.mainInventory.size(); slot++) {
				if (isBackpack(player.inventory.getStackInSlot(slot))) {
					openBackpack(player, slot);
					return;
				}
			}
			if (isBackpack(player.inventory.getStackInSlot(GuiHandler.OFFHAND_SLOT))) {
				openBackpack(player, GuiHandler.OFFHAND_SLOT);
			} else if (isBackpack(player.inventory.getStackInSlot(CHEST_SLOT))) {
				openBackpack(player, CHEST_SLOT);
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
