package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.api.IBlockPickResponseUpgrade;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** The player picked a block whose item they don't have: their backpacks' block pick upgrades, in turn, try to give it. */
public class BlockPickMessage implements IMessage {
	private ItemStack filter = ItemStack.EMPTY;

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public BlockPickMessage() {
	}

	public BlockPickMessage(ItemStack filter) {
		this.filter = filter;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		filter = ByteBufUtils.readItemStack(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeItemStack(buf, filter);
	}

	public static class Handler implements IMessageHandler<BlockPickMessage, IMessage> {
		@Override
		public IMessage onMessage(BlockPickMessage message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> {
				if (message.filter.isEmpty()) {
					return;
				}
				PlayerInventoryProvider.runOnBackpacks(player, (backpack, slot) -> {
					for (IBlockPickResponseUpgrade upgrade : BackpackWrapper.fromStack(backpack).getUpgradeHandler().getWrappersThatImplement(IBlockPickResponseUpgrade.class)) {
						if (upgrade.pickBlock(player, message.filter)) {
							return true;
						}
					}
					return false;
				});
			});
			return null;
		}
	}
}
