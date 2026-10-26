package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackStorage;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

/**
 * The client asks for the contents of a backpack it sees but never opened, to show what can be stashed into it. Only
 * those of a backpack the player has at hand are sent: held, or in a slot of the open screen.
 */
public class RequestBackpackContentsMessage implements IMessage {
	private UUID backpackUuid = new UUID(0, 0);

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public RequestBackpackContentsMessage() {
	}

	public RequestBackpackContentsMessage(UUID backpackUuid) {
		this.backpackUuid = backpackUuid;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		backpackUuid = new UUID(buf.readLong(), buf.readLong());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(backpackUuid.getMostSignificantBits());
		buf.writeLong(backpackUuid.getLeastSignificantBits());
	}

	public static class Handler implements IMessageHandler<RequestBackpackContentsMessage, IMessage> {
		@Override
		public IMessage onMessage(RequestBackpackContentsMessage message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> {
				if (isAtHand(player, message.backpackUuid)) {
					PacketHandler.INSTANCE.sendTo(new BackpackContentsMessage(message.backpackUuid, BackpackStorage.getOrCreateBackpackContents(message.backpackUuid)), player);
				}
			});
			return null;
		}

		private static boolean isAtHand(EntityPlayerMP player, UUID uuid) {
			if (hasUuid(player.inventory.getItemStack(), uuid)) {
				return true;
			}
			for (Slot slot : player.openContainer.inventorySlots) {
				if (hasUuid(slot.getStack(), uuid)) {
					return true;
				}
			}
			return false;
		}

		private static boolean hasUuid(ItemStack stack, UUID uuid) {
			return stack.getItem() instanceof BackpackItem && BackpackWrapper.fromStack(stack).getContentsUuid().map(uuid::equals).orElse(false);
		}
	}
}
