package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.util.InventoryInteractionHelper;
import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** The interaction key on a block: the first backpack the player carries runs its interaction upgrades on it. */
public class InventoryInteractionMessage implements IMessage {
	private static final double MAX_DISTANCE_SQ = 64;
	private BlockPos pos = BlockPos.ORIGIN;
	private EnumFacing face = EnumFacing.UP;

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public InventoryInteractionMessage() {
	}

	public InventoryInteractionMessage(BlockPos pos, EnumFacing face) {
		this.pos = pos;
		this.face = face;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong(buf.readLong());
		face = EnumFacing.byIndex(buf.readByte());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
		buf.writeByte(face.getIndex());
	}

	public static class Handler implements IMessageHandler<InventoryInteractionMessage, IMessage> {
		@Override
		public IMessage onMessage(InventoryInteractionMessage message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> {
				if (!player.world.isBlockLoaded(message.pos) || player.getDistanceSqToCenter(message.pos) > MAX_DISTANCE_SQ) {
					return;
				}
				PlayerInventoryProvider.runOnBackpacks(player, (backpack, slot) -> {
					InventoryInteractionHelper.tryInventoryInteraction(message.pos, player.world, backpack, message.face, player);
					player.swingArm(EnumHand.MAIN_HAND);
					return true;
				});
			});
			return null;
		}
	}
}
