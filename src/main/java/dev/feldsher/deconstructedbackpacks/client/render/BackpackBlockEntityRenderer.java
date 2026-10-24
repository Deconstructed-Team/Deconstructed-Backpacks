package dev.feldsher.deconstructedbackpacks.client.render;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackBlock;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackBlockEntity;
import dev.feldsher.deconstructedcore.renderdata.RenderInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

/**
 * A placed backpack's displayed item, on its front. The backpack itself is its block model; the tanks and the battery
 * charge come with the upgrades that show them.
 */
public class BackpackBlockEntityRenderer extends TileEntitySpecialRenderer<BackpackBlockEntity> {
	@Override
	public void render(BackpackBlockEntity backpackBlockEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		RenderInfo renderInfo = backpackBlockEntity.getBackpackWrapper().getRenderInfo();
		renderInfo.getItemDisplayRenderInfo().getDisplayItem().ifPresent(displayItem -> {
			IBlockState state = backpackBlockEntity.getWorld().getBlockState(backpackBlockEntity.getPos());
			if (!(state.getBlock() instanceof BackpackBlock)) {
				return;
			}
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 0.5, y, z + 0.5);
			GlStateManager.rotate(-state.getValue(BackpackBlock.FACING).getHorizontalAngle(), 0, 1, 0);
			GlStateManager.translate(0, 0.6, 0.25);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.rotate(-180, 1, 0, 0);
			GlStateManager.rotate(180F + displayItem.getRotation(), 0, 0, 1);
			GlStateManager.enableRescaleNormal();
			Minecraft.getMinecraft().getRenderItem().renderItem(displayItem.getItem(), ItemCameraTransforms.TransformType.FIXED);
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
		});
	}
}
