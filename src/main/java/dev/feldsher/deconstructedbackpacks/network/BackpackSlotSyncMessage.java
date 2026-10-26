package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * A player's backpack as it is now, in a slot of an inventory a mod adds, as Baubles': that mod syncs it only at the end
 * of the tick, after the backpack's container has opened, which builds its slots from the stack.
 */
public class BackpackSlotSyncMessage implements IMessage {
	private int playerId;
	private int slot;
	private ItemStack backpack = ItemStack.EMPTY;

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public BackpackSlotSyncMessage() {
	}

	public BackpackSlotSyncMessage(int playerId, int slot, ItemStack backpack) {
		this.playerId = playerId;
		this.slot = slot;
		this.backpack = backpack;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		playerId = buf.readInt();
		slot = buf.readInt();
		backpack = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(playerId);
		buf.writeInt(slot);
		ByteBufUtils.writeItemStack(buf, backpack);
	}

	public static class Handler implements IMessageHandler<BackpackSlotSyncMessage, IMessage> {
		@Override
		public IMessage onMessage(BackpackSlotSyncMessage message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
				World world = DeconstructedBackpacks.proxy.getClientWorld();
				Entity entity = world == null ? null : world.getEntityByID(message.playerId);
				if (entity instanceof EntityPlayer) {
					PlayerInventoryProvider.setStackInSlot((EntityPlayer) entity, message.slot, message.backpack);
				}
			});
			return null;
		}
	}
}
