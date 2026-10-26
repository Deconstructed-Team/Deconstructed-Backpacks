package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import dev.feldsher.deconstructedbackpacks.compat.ae2.NetworkStashMessage;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(DeconstructedBackpacks.MOD_ID);

	private PacketHandler() {}

	public static void init() {
		int id = 0;
		INSTANCE.registerMessage(BackpackOpenMessage.Handler.class, BackpackOpenMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(BackpackContentsMessage.Handler.class, BackpackContentsMessage.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(InventoryInteractionMessage.Handler.class, InventoryInteractionMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(ToolSwapMessage.Handler.class, ToolSwapMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(BlockPickMessage.Handler.class, BlockPickMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(UpgradeToggleMessage.Handler.class, UpgradeToggleMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(RequestBackpackContentsMessage.Handler.class, RequestBackpackContentsMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(NetworkStashMessage.Handler.class, NetworkStashMessage.class, id++, Side.SERVER);
		INSTANCE.registerMessage(AnotherPlayerBackpackOpenMessage.Handler.class, AnotherPlayerBackpackOpenMessage.class, id++, Side.SERVER);
	}
}
