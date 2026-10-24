package dev.feldsher.deconstructedbackpacks.client;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID, value = Side.CLIENT)
public class ClientEventHandler {
	private ClientEventHandler() {}

	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		for (Item backpack : ModItems.BACKPACKS) {
			ModelLoader.setCustomModelResourceLocation(backpack, 0, new ModelResourceLocation(Objects.requireNonNull(backpack.getRegistryName()), "inventory"));
		}
	}

	/** Tint 0 is the cloth, in the main color; tint 1 the border, in the accent color. */
	@SubscribeEvent
	public static void registerItemColors(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
			if (tintIndex < 0 || tintIndex > 1 || stack.isEmpty()) {
				return -1;
			}
			IBackpackWrapper backpackWrapper = BackpackWrapper.fromStack(stack);
			return tintIndex == 0 ? backpackWrapper.getMainColor() : backpackWrapper.getAccentColor();
		}, ModItems.BACKPACKS.toArray(new Item[0]));
	}
}
