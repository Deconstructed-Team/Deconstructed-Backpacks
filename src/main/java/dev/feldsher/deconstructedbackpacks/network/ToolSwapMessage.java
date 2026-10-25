package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.api.IBlockToolSwapUpgrade;
import dev.feldsher.deconstructedbackpacks.api.IEntityToolSwapUpgrade;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The tool swap key on a block or an entity: the backpacks' tool swap upgrades, in turn, swap in a tool that works on
 * it, until one does.
 */
public class ToolSwapMessage implements IMessage {
	private static final double MAX_DISTANCE_SQ = 64;
	private boolean onEntity;
	private BlockPos pos = BlockPos.ORIGIN;
	private int entityId;

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public ToolSwapMessage() {
	}

	public static ToolSwapMessage block(BlockPos pos) {
		ToolSwapMessage message = new ToolSwapMessage();
		message.pos = pos;
		return message;
	}

	public static ToolSwapMessage entity(int entityId) {
		ToolSwapMessage message = new ToolSwapMessage();
		message.onEntity = true;
		message.entityId = entityId;
		return message;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		onEntity = buf.readBoolean();
		if (onEntity) {
			entityId = buf.readInt();
		} else {
			pos = BlockPos.fromLong(buf.readLong());
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(onEntity);
		if (onEntity) {
			buf.writeInt(entityId);
		} else {
			buf.writeLong(pos.toLong());
		}
	}

	public static class Handler implements IMessageHandler<ToolSwapMessage, IMessage> {
		@Override
		public IMessage onMessage(ToolSwapMessage message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> {
				if (message.onEntity) {
					swapForEntity(player, message.entityId);
				} else {
					swapForBlock(player, message.pos);
				}
			});
			return null;
		}

		private static void swapForBlock(EntityPlayerMP player, BlockPos pos) {
			if (!player.world.isBlockLoaded(pos) || player.getDistanceSqToCenter(pos) > MAX_DISTANCE_SQ) {
				return;
			}
			AtomicBoolean result = new AtomicBoolean(false);
			AtomicBoolean anyUpgradeCanInteract = new AtomicBoolean(false);
			PlayerInventoryProvider.runOnBackpacks(player, (backpack, slot) -> {
				for (IBlockToolSwapUpgrade upgrade : BackpackWrapper.fromStack(backpack).getUpgradeHandler().getWrappersThatImplement(IBlockToolSwapUpgrade.class)) {
					if (upgrade.canProcessBlockInteract() && !result.get()) {
						anyUpgradeCanInteract.set(true);
						result.set(upgrade.onBlockInteract(player.world, pos, player.world.getBlockState(pos), player));
					}
				}
				return result.get();
			});
			sendResult(player, anyUpgradeCanInteract.get(), result.get(), "no_tool_found_for_block");
		}

		private static void swapForEntity(EntityPlayerMP player, int entityId) {
			Entity entity = player.world.getEntityByID(entityId);
			if (entity == null || player.getDistanceSq(entity) > MAX_DISTANCE_SQ) {
				return;
			}
			AtomicBoolean result = new AtomicBoolean(false);
			AtomicBoolean anyUpgradeCanInteract = new AtomicBoolean(false);
			PlayerInventoryProvider.runOnBackpacks(player, (backpack, slot) -> {
				for (IEntityToolSwapUpgrade upgrade : BackpackWrapper.fromStack(backpack).getUpgradeHandler().getWrappersThatImplement(IEntityToolSwapUpgrade.class)) {
					if (upgrade.canProcessEntityInteract() && !result.get()) {
						anyUpgradeCanInteract.set(true);
						result.set(upgrade.onEntityInteract(player.world, entity, player));
					}
				}
				return result.get();
			});
			sendResult(player, anyUpgradeCanInteract.get(), result.get(), "no_tool_found_for_entity");
		}

		private static void sendResult(EntityPlayerMP player, boolean anyUpgradeCanInteract, boolean swapped, String noToolFoundKey) {
			if (!anyUpgradeCanInteract) {
				player.sendStatusMessage(new TextComponentTranslation("gui.deconstructedbackpacks.status.no_tool_swap_upgrade_present"), true);
			} else if (!swapped) {
				player.sendStatusMessage(new TextComponentTranslation("gui.deconstructedbackpacks.status." + noToolFoundKey), true);
			}
		}
	}
}
