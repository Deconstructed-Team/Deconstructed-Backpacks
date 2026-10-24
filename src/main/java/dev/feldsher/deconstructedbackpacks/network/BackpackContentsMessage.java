package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A backpack's contents, for the client's copy of the backpack storage. Sent before the backpack's container opens,
 * so that the client builds it with the same upgrades, and so the same slots, as the server.
 */
public class BackpackContentsMessage implements IMessage {
	@Nullable
	private UUID backpackUuid;
	@Nullable
	private NBTTagCompound backpackContents;

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public BackpackContentsMessage() {
	}

	public BackpackContentsMessage(UUID backpackUuid, NBTTagCompound backpackContents) {
		this.backpackUuid = backpackUuid;
		this.backpackContents = backpackContents;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		backpackUuid = new UUID(buf.readLong(), buf.readLong());
		backpackContents = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (backpackUuid == null) {
			throw new IllegalStateException("No backpack UUID to send");
		}
		buf.writeLong(backpackUuid.getMostSignificantBits());
		buf.writeLong(backpackUuid.getLeastSignificantBits());
		ByteBufUtils.writeTag(buf, backpackContents);
	}

	public static class Handler implements IMessageHandler<BackpackContentsMessage, IMessage> {
		@Override
		public IMessage onMessage(BackpackContentsMessage message, MessageContext ctx) {
			UUID uuid = message.backpackUuid;
			NBTTagCompound contents = message.backpackContents;
			if (uuid != null && contents != null) {
				FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> BackpackStorage.get().setBackpackContents(uuid, contents));
			}
			return null;
		}
	}
}
