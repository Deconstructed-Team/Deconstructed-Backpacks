package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.Config;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * The server's config values that the client shows things by, as its own config file may say otherwise: whether
 * players can open each other's backpacks, which the backpack settings show a toggle for only when they can.
 */
public class ServerConfigSyncMessage implements IMessage {
	private static boolean serverAllowsOpeningOtherPlayerBackpacks = true;

	private boolean allowOpeningOtherPlayerBackpacks;

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public ServerConfigSyncMessage() {
	}

	private ServerConfigSyncMessage(boolean allowOpeningOtherPlayerBackpacks) {
		this.allowOpeningOtherPlayerBackpacks = allowOpeningOtherPlayerBackpacks;
	}

	/** On the client, as the server it's on has it. */
	public static boolean allowsOpeningOtherPlayerBackpacks() {
		return serverAllowsOpeningOtherPlayerBackpacks;
	}

	public static void sendTo(EntityPlayerMP player) {
		PacketHandler.INSTANCE.sendTo(new ServerConfigSyncMessage(Config.SERVER.allowOpeningOtherPlayerBackpacks), player);
	}

	/** After the config changed in game, on a server this game runs. */
	public static void sendToAll() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (server != null) {
			server.addScheduledTask(() -> server.getPlayerList().getPlayers().forEach(ServerConfigSyncMessage::sendTo));
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		allowOpeningOtherPlayerBackpacks = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(allowOpeningOtherPlayerBackpacks);
	}

	public static class Handler implements IMessageHandler<ServerConfigSyncMessage, IMessage> {
		@Override
		public IMessage onMessage(ServerConfigSyncMessage message, MessageContext ctx) {
			serverAllowsOpeningOtherPlayerBackpacks = message.allowOpeningOtherPlayerBackpacks;
			return null;
		}
	}
}
