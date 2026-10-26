package dev.feldsher.deconstructedbackpacks.client.render;

import dev.feldsher.deconstructedcore.client.render.UpgradeRenderRegistry;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedcore.renderdata.RenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * A backpack in the chest slot, on the back of whoever wears it: players and every living entity, as mobs that spawn with one.
 * Its displayed item is on its front, its working upgrades' particles come off its back.
 */
public class BackpackLayerRenderer implements LayerRenderer<EntityLivingBase> {
	private static final BackpackModel MODEL = new BackpackModel();
	private final RenderLivingBase<?> renderer;

	public BackpackLayerRenderer(RenderLivingBase<?> renderer) {
		this.renderer = renderer;
	}

	/** Adds the layer to the players' renderers and to every living entity's renderer. */
	public static void addToRenderers(RenderManager renderManager) {
		for (RenderPlayer playerRenderer : renderManager.getSkinMap().values()) {
			playerRenderer.addLayer(new BackpackLayerRenderer(playerRenderer));
		}
		for (Render<?> render : renderManager.entityRenderMap.values()) {
			if (render instanceof RenderLivingBase) {
				addLayer((RenderLivingBase<?>) render);
			}
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void addLayer(RenderLivingBase render) {
		render.addLayer(new BackpackLayerRenderer(render));
	}

	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack chestStack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (!(chestStack.getItem() instanceof BackpackItem)) {
			return;
		}

		GlStateManager.pushMatrix();
		// on a model with no body to follow, as a creeper's, it's where a body would be
		if (renderer.getMainModel() instanceof ModelBiped) {
			// ModelBiped moves itself down when sneaking, and so does every layer that follows its body
			if (entity.isSneaking()) {
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
			}
			((ModelBiped) renderer.getMainModel()).bipedBody.postRender(scale);
		}
		BackpackModel.translateRotateAndScale(entity, false);

		IBackpackWrapper wrapper = BackpackWrapper.fromStack(chestStack);
		MODEL.render(wrapper.getMainColor(), wrapper.getAccentColor(), chestStack.getItem(), wrapper.getRenderInfo());
		renderItemShown(wrapper.getRenderInfo());
		GlStateManager.popMatrix();
		renderUpgrades(entity, wrapper.getRenderInfo());
	}

	/** What its working upgrades show, now and then as it's drawn, from the middle of its back face, as the wearer turns and sneaks. */
	private static void renderUpgrades(EntityLivingBase entity, RenderInfo renderInfo) {
		if (renderInfo.getUpgradeRenderData().isEmpty() || Minecraft.getMinecraft().isGamePaused() || entity.world.rand.nextInt(32) != 0) {
			return;
		}
		boolean sneaking = entity.isSneaking();
		float pitch = (float) Math.toRadians(sneaking ? 25 : 0);
		float yaw = (float) Math.toRadians(entity.renderYawOffset - 180);
		UpgradeRenderRegistry.render(renderInfo.getUpgradeRenderData(), entity.world, entity.world.rand,
				offset -> offset.rotatePitch(-pitch).add(0, 0.8, sneaking ? 0.9 : 0.7).rotateYaw(-yaw).add(entity.posX, entity.posY, entity.posZ));
	}

	private static void renderItemShown(RenderInfo renderInfo) {
		renderInfo.getItemDisplayRenderInfo().getDisplayItem().ifPresent(displayItem -> {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0.9, -0.25);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.rotate(180F + displayItem.getRotation(), 0, 0, 1);
			GlStateManager.enableRescaleNormal();
			Minecraft.getMinecraft().getRenderItem().renderItem(displayItem.getItem(), ItemCameraTransforms.TransformType.FIXED);
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
		});
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}
