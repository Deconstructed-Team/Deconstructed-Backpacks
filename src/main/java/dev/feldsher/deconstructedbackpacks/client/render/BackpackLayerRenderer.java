package dev.feldsher.deconstructedbackpacks.client.render;

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
 * A backpack in the chest slot, on the back of whoever wears it: players and every mob drawn with a biped model.
 * Its displayed item is on its front; upgrade particles come with the upgrades that make them.
 */
public class BackpackLayerRenderer implements LayerRenderer<EntityLivingBase> {
	private static final BackpackModel MODEL = new BackpackModel();
	private final RenderLivingBase<?> renderer;

	public BackpackLayerRenderer(RenderLivingBase<?> renderer) {
		this.renderer = renderer;
	}

	/** Adds the layer to the players' renderers and to every living entity's renderer with a biped model. */
	public static void addToRenderers(RenderManager renderManager) {
		for (RenderPlayer playerRenderer : renderManager.getSkinMap().values()) {
			playerRenderer.addLayer(new BackpackLayerRenderer(playerRenderer));
		}
		for (Render<?> render : renderManager.entityRenderMap.values()) {
			if (render instanceof RenderLivingBase && ((RenderLivingBase<?>) render).getMainModel() instanceof ModelBiped) {
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
		if (!(chestStack.getItem() instanceof BackpackItem) || !(renderer.getMainModel() instanceof ModelBiped)) {
			return;
		}

		GlStateManager.pushMatrix();
		// ModelBiped moves itself down when sneaking, and so does every layer that follows its body
		if (entity.isSneaking()) {
			GlStateManager.translate(0.0F, 0.2F, 0.0F);
		}
		((ModelBiped) renderer.getMainModel()).bipedBody.postRender(scale);
		BackpackModel.translateRotateAndScale(entity, false);

		IBackpackWrapper wrapper = BackpackWrapper.fromStack(chestStack);
		MODEL.render(wrapper.getMainColor(), wrapper.getAccentColor(), chestStack.getItem());
		renderItemShown(wrapper.getRenderInfo());
		GlStateManager.popMatrix();
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
