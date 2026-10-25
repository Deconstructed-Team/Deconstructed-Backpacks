package dev.feldsher.deconstructedbackpacks.client.render;

import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * A backpack stack that shows an item: its own model, tinted as usual, with the item on its front. Only such stacks
 * come here, through {@link BackpackItemModel}; the rest draw as plain models.
 */
public class BackpackItemStackRenderer extends TileEntityItemStackRenderer {
	@Override
	public void renderByItem(ItemStack stack, float partialTicks) {
		Minecraft minecraft = Minecraft.getMinecraft();
		IBakedModel model = minecraft.getRenderItem().getItemModelMesher().getItemModel(stack);
		if (model instanceof BackpackItemModel) {
			renderModel(((BackpackItemModel) model).getOriginalModel(), stack, minecraft.getItemColors());
		}
		BackpackWrapper.fromStack(stack).getRenderInfo().getItemDisplayRenderInfo().getDisplayItem().ifPresent(displayItem -> {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.5, 0.6, 0.25);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.rotate(displayItem.getRotation(), 0, 0, 1);
			minecraft.getRenderItem().renderItem(displayItem.getItem(), ItemCameraTransforms.TransformType.FIXED);
			GlStateManager.popMatrix();
			minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		});
	}

	/** As vanilla draws an item's model, with its tints. */
	private static void renderModel(IBakedModel model, ItemStack stack, ItemColors itemColors) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		for (EnumFacing facing : EnumFacing.values()) {
			renderQuads(buffer, model.getQuads(null, facing, 0), stack, itemColors);
		}
		renderQuads(buffer, model.getQuads(null, null, 0), stack, itemColors);
		tessellator.draw();
	}

	private static void renderQuads(BufferBuilder buffer, List<BakedQuad> quads, ItemStack stack, ItemColors itemColors) {
		for (BakedQuad quad : quads) {
			int color = -1;
			if (quad.hasTintIndex()) {
				color = itemColors.colorMultiplier(stack, quad.getTintIndex()) | 0xFF000000;
			}
			LightUtil.renderQuadColor(buffer, quad, color);
		}
	}
}
