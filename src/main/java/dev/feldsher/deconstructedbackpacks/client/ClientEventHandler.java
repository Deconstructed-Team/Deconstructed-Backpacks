package dev.feldsher.deconstructedbackpacks.client;

import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumActionResult;
import net.minecraft.entity.player.EntityPlayer;
import dev.feldsher.deconstructedbackpacks.network.PacketHandler;
import dev.feldsher.deconstructedbackpacks.network.AnotherPlayerBackpackOpenMessage;
import dev.feldsher.deconstructedbackpacks.network.ServerConfigSyncMessage;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraft.util.EnumFacing;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackBlock;
import dev.feldsher.deconstructedbackpacks.client.render.BackpackBlockModel;
import dev.feldsher.deconstructedbackpacks.client.render.BackpackParts;
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

	/**
	 * A right click on another player's back, at the height of a backpack worn there, asks the server to open it, as
	 * the config and that player's setting allow.
	 */
	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
		if (!event.getWorld().isRemote || !ServerConfigSyncMessage.allowsOpeningOtherPlayerBackpacks() || !(event.getTarget() instanceof EntityPlayer)) {
			return;
		}
		EntityPlayer targetPlayer = (EntityPlayer) event.getTarget();
		if (PlayerInventoryProvider.getRenderedBackpack(targetPlayer).isEmpty()) {
			return;
		}
		EntityPlayer sourcePlayer = event.getEntityPlayer();
		Vec3d targetViewVector = Vec3d.fromPitchYaw(targetPlayer.rotationPitch, targetPlayer.renderYawOffset);
		Vec3d hitVector = event.getLocalPos();
		Vec3d toTarget = targetPlayer.getPositionVector().subtract(sourcePlayer.getPositionVector()).normalize();
		toTarget = new Vec3d(toTarget.x, 0, toTarget.z);
		boolean isPointingAtBody = hitVector.y >= 0.9D && hitVector.y < 1.6D;
		boolean isPointingAtBack = toTarget.dotProduct(targetViewVector) > 0.0D;
		if (isPointingAtBody && isPointingAtBack) {
			PacketHandler.INSTANCE.sendToServer(new AnotherPlayerBackpackOpenMessage(targetPlayer.getEntityId()));
			event.setCanceled(true);
			event.setCancellationResult(EnumActionResult.SUCCESS);
		}
	}

	/** The tanks and the battery: their parts are baked from code, so nothing else asks for the texture. */
	@SubscribeEvent
	public static void onTextureStitch(TextureStitchEvent.Pre event) {
		event.getMap().registerSprite(BackpackParts.MODULES_TEXTURE);
	}

	/**
	 * Placed backpacks and backpack stacks get the parts their render info calls for, tanks and battery in place of
	 * pouches; stacks that show an item draw it on their front.
	 */
	@SubscribeEvent
	public static void onModelBake(ModelBakeEvent event) {
		BackpackParts.bake();
		for (Item backpack : ModItems.getBackpacks()) {
			ModelResourceLocation location = getInventoryModelLocation(backpack);
			IBakedModel model = event.getModelRegistry().getObject(location);
			if (model != null && !(model instanceof BackpackItemModel)) {
				event.getModelRegistry().putObject(location, new BackpackItemModel(model, backpack));
			}
		}
		for (BackpackBlock block : ModBlocks.getBackpacks()) {
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				ModelResourceLocation location = new ModelResourceLocation(Objects.requireNonNull(block.getRegistryName()), "facing=" + facing.getName());
				IBakedModel model = event.getModelRegistry().getObject(location);
				if (model != null && !(model instanceof BackpackBlockModel)) {
					event.getModelRegistry().putObject(location, new BackpackBlockModel(model, block.getBackpackItem(), facing));
				}
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
