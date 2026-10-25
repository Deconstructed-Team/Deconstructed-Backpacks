package dev.feldsher.deconstructedbackpacks.client;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackBlockEntity;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedbackpacks.init.ModBlocks;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import net.minecraft.block.Block;
import dev.feldsher.deconstructedbackpacks.client.render.BackpackItemModel;
import dev.feldsher.deconstructedbackpacks.client.render.BackpackItemStackRenderer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
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
		for (Item item : ModItems.getAllItems()) {
			ModelLoader.setCustomModelResourceLocation(item, 0, getInventoryModelLocation(item));
		}
		BackpackItemStackRenderer itemStackRenderer = new BackpackItemStackRenderer();
		ModItems.getBackpacks().forEach(backpack -> backpack.setTileEntityItemStackRenderer(itemStackRenderer));
	}

	/** Backpack stacks that show an item draw it on their front. */
	@SubscribeEvent
	public static void onModelBake(ModelBakeEvent event) {
		for (Item backpack : ModItems.getBackpacks()) {
			ModelResourceLocation location = getInventoryModelLocation(backpack);
			IBakedModel model = event.getModelRegistry().getObject(location);
			if (model != null && !(model instanceof BackpackItemModel)) {
				event.getModelRegistry().putObject(location, new BackpackItemModel(model));
			}
		}
	}

	private static ModelResourceLocation getInventoryModelLocation(Item item) {
		return new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory");
	}

	/** Tint 0 is the cloth, in the main color; tint 1 the border, in the accent color. */
	@SubscribeEvent
	public static void registerItemColors(ColorHandlerEvent.Item event) {
		event.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
			if (tintIndex < 0 || tintIndex > 1 || stack.isEmpty()) {
				return -1;
			}
			return getColor(BackpackWrapper.fromStack(stack), tintIndex);
		}, ModItems.getBackpacks().toArray(new Item[0]));
	}

	@SubscribeEvent
	public static void registerBlockColors(ColorHandlerEvent.Block event) {
		event.getBlockColors().registerBlockColorHandler((state, world, pos, tintIndex) -> {
			if (tintIndex < 0 || tintIndex > 1 || world == null || pos == null) {
				return -1;
			}
			TileEntity te = world.getTileEntity(pos);
			return te instanceof BackpackBlockEntity ? getColor(((BackpackBlockEntity) te).getBackpackWrapper(), tintIndex) : -1;
		}, ModBlocks.getBackpacks().toArray(new Block[0]));
	}

	private static int getColor(IBackpackWrapper backpackWrapper, int tintIndex) {
		return tintIndex == 0 ? backpackWrapper.getMainColor() : backpackWrapper.getAccentColor();
	}
}
