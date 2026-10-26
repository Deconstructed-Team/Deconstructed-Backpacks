package dev.feldsher.deconstructedbackpacks.compat.ae2;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * A right click with a backpack held onto an item of the network in a terminal: a stack of it, or with shift as much
 * as fits, goes from the network into the backpack. Registered whether or not AE2 is there, so message ids match.
 */
public class NetworkStashMessage implements IMessage {
	private int windowId;
	private ItemStack item = ItemStack.EMPTY;
	private boolean asMuchAsFits;

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public NetworkStashMessage() {
	}

	public NetworkStashMessage(int windowId, ItemStack item, boolean asMuchAsFits) {
		this.windowId = windowId;
		this.item = item;
		this.asMuchAsFits = asMuchAsFits;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		windowId = buf.readInt();
		item = ByteBufUtils.readItemStack(buf);
		asMuchAsFits = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(windowId);
		ByteBufUtils.writeItemStack(buf, item);
		buf.writeBoolean(asMuchAsFits);
	}

	public static class Handler implements IMessageHandler<NetworkStashMessage, IMessage> {
		@Override
		public IMessage onMessage(NetworkStashMessage message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			if (Ae2Compat.isAvailable()) {
				player.getServerWorld().addScheduledTask(() -> NetworkStash.stash(player, message.windowId, message.item, message.asMuchAsFits));
			}
			return null;
		}
	}
}
