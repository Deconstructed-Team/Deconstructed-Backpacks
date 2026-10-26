package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContext;
import dev.feldsher.deconstructedbackpacks.settings.BackpackMainSettingsCategory;
import dev.feldsher.deconstructedcore.settings.SettingsManager;
import dev.feldsher.deconstructedcore.settings.main.MainSettingsCategory;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * A right click on the back of another player who wears a backpack: it opens, if the config allows it and that
 * player's backpack setting lets others open it.
 */
public class AnotherPlayerBackpackOpenMessage implements IMessage {
	private int anotherPlayerId;

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public AnotherPlayerBackpackOpenMessage() {
	}

	public AnotherPlayerBackpackOpenMessage(int anotherPlayerId) {
		this.anotherPlayerId = anotherPlayerId;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		anotherPlayerId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(anotherPlayerId);
	}

	public static class Handler implements IMessageHandler<AnotherPlayerBackpackOpenMessage, IMessage> {
		@Override
		public IMessage onMessage(AnotherPlayerBackpackOpenMessage message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> handle(player, message.anotherPlayerId));
			return null;
		}

		private static void handle(EntityPlayerMP player, int anotherPlayerId) {
			if (!Config.SERVER.allowOpeningOtherPlayerBackpacks) {
				return;
			}
			Entity entity = player.world.getEntityByID(anotherPlayerId);
			if (!(entity instanceof EntityPlayer) || entity == player) {
				return;
			}
			EntityPlayer anotherPlayer = (EntityPlayer) entity;
			ItemStack backpack = PlayerInventoryProvider.getRenderedBackpack(anotherPlayer);
			if (!(backpack.getItem() instanceof BackpackItem)) {
				return;
			}
			if (!canAnotherPlayerOpen(anotherPlayer, backpack)) {
				player.sendStatusMessage(new TextComponentTranslation("gui.deconstructedbackpacks.status.backpack_cannot_be_open_by_another_player"), true);
				return;
			}
			BackpackContext context = new BackpackContext.AnotherPlayer(anotherPlayerId);
			if (context.canInteractWith(player)) {
				context.open(player);
			}
		}

		private static boolean canAnotherPlayerOpen(EntityPlayer anotherPlayer, ItemStack backpack) {
			MainSettingsCategory<?> category = BackpackWrapper.fromStack(backpack).getSettingsHandler().getGlobalSettingsCategory();
			return SettingsManager.getSettingValue(anotherPlayer, category.getPlayerSettingsTagName(), category, BackpackMainSettingsCategory.ANOTHER_PLAYER_CAN_OPEN);
		}
	}
}
