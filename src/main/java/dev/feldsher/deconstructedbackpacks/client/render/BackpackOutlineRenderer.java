package dev.feldsher.deconstructedbackpacks.client.render;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackBlock;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackShapes;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** A placed backpack's outline when aimed at: around its shape, as 1.21 draws it, instead of around one box. */
@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID, value = Side.CLIENT)
public final class BackpackOutlineRenderer {
	// as vanilla's selection box: lifted off the faces so that they don't hide the lines
	private static final double GROW = 0.002;

	private static final Map<EnumFacing, List<double[]>> OUTLINES = new EnumMap<>(EnumFacing.class);

	private BackpackOutlineRenderer() {}

	@SubscribeEvent
	public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
		RayTraceResult target = event.getTarget();
		if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}
		EntityPlayer player = event.getPlayer();
		BlockPos pos = target.getBlockPos();
		IBlockState state = player.world.getBlockState(pos);
		if (!(state.getBlock() instanceof BackpackBlock) || !player.world.getWorldBorder().contains(pos)) {
			return;
		}
		event.setCanceled(true);

		EnumFacing facing = state.getValue(BackpackBlock.FACING);
		List<double[]> edges = OUTLINES.computeIfAbsent(facing, f -> ShapeOutline.getEdges(BackpackShapes.getShape(f)));
		AxisAlignedBB envelope = BackpackShapes.getEnvelope(facing);
		double centerX = (envelope.minX + envelope.maxX) / 2;
		double centerY = (envelope.minY + envelope.maxY) / 2;
		double centerZ = (envelope.minZ + envelope.maxZ) / 2;

		float partialTicks = event.getPartialTicks();
		double offsetX = pos.getX() - (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
		double offsetY = pos.getY() - (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks);
		double offsetZ = pos.getZ() - (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		for (double[] edge : edges) {
			vertex(buffer, edge[0], edge[1], edge[2], centerX, centerY, centerZ, offsetX, offsetY, offsetZ);
			vertex(buffer, edge[3], edge[4], edge[5], centerX, centerY, centerZ, offsetX, offsetY, offsetZ);
		}
		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	private static void vertex(BufferBuilder buffer, double x, double y, double z, double centerX, double centerY, double centerZ,
			double offsetX, double offsetY, double offsetZ) {
		buffer.pos(x + Math.signum(x - centerX) * GROW + offsetX, y + Math.signum(y - centerY) * GROW + offsetY,
				z + Math.signum(z - centerZ) * GROW + offsetZ).color(0, 0, 0, 0.4F).endVertex();
	}
}
