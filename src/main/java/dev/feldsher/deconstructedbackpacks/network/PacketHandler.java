package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(DeconstructedBackpacks.MOD_ID);

	private PacketHandler() {}

	public static void init() {
		int id = 0;
		INSTANCE.registerMessage(BackpackOpenMessage.Handler.class, BackpackOpenMessage.class, id++, Side.SERVER);
	}
}
