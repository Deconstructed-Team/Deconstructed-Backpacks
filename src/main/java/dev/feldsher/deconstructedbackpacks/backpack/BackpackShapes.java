package dev.feldsher.deconstructedbackpacks.backpack;

import dev.feldsher.deconstructedcore.renderdata.TankPosition;
import dev.feldsher.deconstructedcore.renderdata.RenderInfo;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.state.IBlockState;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A placed backpack's shape: the boxes of its body and of what's on its sides and front, pouches or the tanks and the
 * battery in their place, facing south as defined, turned to the backpack's facing. 1.12 has no voxel shapes: the
 * block collides and is aimed at by these boxes, and its outline is drawn around them.
 */
public final class BackpackShapes {
	private BackpackShapes() {}

	private static final double[][] BODY = {
			{5.25, 0, 4, 5.75, 12.5, 5},
			{4.25, 0, 4, 5.25, 12.5, 5},
			{3.75, 0, 4, 4.25, 12.5, 5},
			{10.25, 0, 4, 10.75, 12.5, 5},
			{10.75, 0, 4, 11.75, 12.5, 5},
			{11.75, 0, 4, 12.25, 12.5, 5},
			{4.25, 8.25, 11, 11.75, 9.25, 11.25},
			{11.75, 8.25, 5.25, 12.75, 13.25, 11.25},
			{4.25, 9.25, 5.25, 11.75, 13.25, 11.25},
			{3.25, 8.25, 5.25, 4.25, 13.25, 11.25},
			{5.75, 13.25, 7.25, 6.5, 14, 8.25},
			{5.75, 14, 7.25, 10.25, 14.5, 8.25},
			{9.5, 13.25, 7.25, 10.25, 14, 8.25},
			{4.5, 9, 4.75, 5.5, 13.5, 11.5},
			{4.5, 7, 10.75, 5.5, 9, 11.5},
			{10.5, 9, 4.75, 11.5, 13.5, 11.5},
			{10.5, 7, 10.75, 11.5, 9, 11.5},
			{3, 0, 5, 13, 13, 11},
			{4, 1, 4.5, 12, 12, 5}
	};

	private static final double[][] LEFT_POUCHES = {
			{1, 2, 5.5, 3, 6, 10.5},
			{1, 1, 5.5, 3, 2, 10.5},
			{1, 0, 5.5, 3, 1, 10.5},
			{0.75, 3, 7.5, 1, 5, 8.5},
			{1, 4, 5.5, 3, 5, 10.5},
			{2, 7, 5.5, 3, 11, 10.5},
			{1.75, 8, 7.5, 2, 10, 8.5},
			{2, 9, 5.5, 4, 10, 10.5}
	};

	private static final double[][] RIGHT_POUCHES = {
			{13, 2, 5.5, 15, 6, 10.5},
			{13, 1, 5.5, 15, 2, 10.5},
			{13, 0, 5.5, 15, 1, 10.5},
			{15, 3, 7.5, 15.25, 5, 8.5},
			{13, 4, 5.5, 15, 5, 10.5},
			{13, 7, 5.5, 14, 11, 10.5},
			{14, 8, 7.5, 14.25, 10, 8.5},
			{12, 9, 5.5, 14, 10, 10.5}
	};

	private static final double[][] FRONT_POUCH = {
			{4, 2, 11, 12, 6, 13},
			{4, 1, 11, 12, 2, 13},
			{4, 0, 11, 12, 1, 13},
			{5, 3, 13, 6, 5, 13.25},
			{10, 3, 13, 11, 5, 13.25},
			{4, 4, 11, 12, 5, 13}
	};

	private static final double[][] BATTERY = {
			{4, 0, 11, 12, 6, 14},
			{6, 5.25, 11.5, 7, 6.25, 12.5},
			{7.25, 5.25, 11.5, 8.25, 6.25, 12.5},
			{6, 7, 10.25, 7, 8, 11.25},
			{7.25, 7, 10.25, 8.25, 8, 11.25},
			{6.2, 5.6, 11.7, 6.8, 7.2, 12.3},
			{6.2, 7.2, 10.7, 6.8, 7.8, 12.3},
			{7.45, 5.6, 11.7, 8.05, 7.2, 12.3},
			{7.45, 7.2, 10.7, 8.05, 7.8, 12.3},
			{8.8, 4.05, 12.95, 10.2, 5.45, 14.35},
			{8.8, 0.05, 12.95, 10.2, 1.45, 14.35},
			{11.25, 4.25, 10.25, 12.25, 5.25, 14.25},
			{4.5, 4.25, 13.25, 11.5, 5.25, 14.25},
			{3.75, 4.25, 10.25, 4.75, 5.25, 14.25},
			{11.25, 0.25, 10.25, 12.25, 1.25, 14.25},
			{4.5, 0.25, 13.25, 11.5, 1.25, 14.25},
			{3.75, 0.25, 10.25, 4.75, 1.25, 14.25}
	};

	private static final double[][] LEFT_TANK = {
			{2.5, 1.5, 6, 3.5, 7.5, 10},
			{0.5, 10.5, 6.5, 3.5, 11.5, 9.5},
			{0, 9.5, 6, 3, 10.5, 10},
			{0, 8.5, 6, 3, 9.5, 10},
			{0, 7.5, 6, 3, 8.5, 10},
			{0, 0.5, 6, 3, 1.5, 10},
			{0.5, 1.5, 6.5, 2.5, 7.5, 9.5}
	};

	private static final double[][] RIGHT_TANK = {
			{12.5, 1.5, 6, 13.5, 7.5, 10},
			{12.5, 10.5, 6.5, 15.5, 11.5, 9.5},
			{13, 9.5, 6, 16, 10.5, 10},
			{13, 8.5, 6, 16, 9.5, 10},
			{13, 7.5, 6, 16, 8.5, 10},
			{13, 0.5, 6, 16, 1.5, 10},
			{13.5, 1.5, 6.5, 15.5, 7.5, 9.5}
	};

	private static final Map<Integer, List<AxisAlignedBB>> SHAPES = new ConcurrentHashMap<>();
	private static final Map<Integer, AxisAlignedBB> ENVELOPES = new ConcurrentHashMap<>();

	/** The shape of a placed backpack as its render info shows it, tanks and battery in place of pouches. */
	public static List<AxisAlignedBB> getShape(IBlockAccess world, BlockPos pos, IBlockState state) {
		RenderInfo renderInfo = getRenderInfo(world, pos);
		return getShape(state.getValue(BackpackBlock.FACING), renderInfo.getTankRenderInfos().containsKey(TankPosition.LEFT),
				renderInfo.getTankRenderInfos().containsKey(TankPosition.RIGHT), renderInfo.getBatteryRenderInfo().isPresent());
	}

	public static AxisAlignedBB getEnvelope(IBlockAccess world, BlockPos pos, IBlockState state) {
		RenderInfo renderInfo = getRenderInfo(world, pos);
		return getEnvelope(state.getValue(BackpackBlock.FACING), renderInfo.getTankRenderInfos().containsKey(TankPosition.LEFT),
				renderInfo.getTankRenderInfos().containsKey(TankPosition.RIGHT), renderInfo.getBatteryRenderInfo().isPresent());
	}

	private static RenderInfo getRenderInfo(IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		return te instanceof BackpackBlockEntity ? ((BackpackBlockEntity) te).getBackpackWrapper().getRenderInfo() : RenderInfo.EMPTY;
	}

	public static List<AxisAlignedBB> getShape(EnumFacing facing, boolean leftTank, boolean rightTank, boolean battery) {
		return SHAPES.computeIfAbsent(getKey(facing, leftTank, rightTank, battery), key -> composeShape(facing, leftTank, rightTank, battery));
	}

	/** The one box around all of the shape, which 1.12 wants for a block's bounds. */
	public static AxisAlignedBB getEnvelope(EnumFacing facing, boolean leftTank, boolean rightTank, boolean battery) {
		return ENVELOPES.computeIfAbsent(getKey(facing, leftTank, rightTank, battery), key -> {
			List<AxisAlignedBB> boxes = getShape(facing, leftTank, rightTank, battery);
			AxisAlignedBB envelope = boxes.get(0);
			for (AxisAlignedBB box : boxes) {
				envelope = envelope.union(box);
			}
			return envelope;
		});
	}

	public static int getKey(EnumFacing facing, boolean leftTank, boolean rightTank, boolean battery) {
		return facing.getHorizontalIndex() << 3 | (leftTank ? 4 : 0) | (rightTank ? 2 : 0) | (battery ? 1 : 0);
	}

	private static List<AxisAlignedBB> composeShape(EnumFacing facing, boolean leftTank, boolean rightTank, boolean battery) {
		List<AxisAlignedBB> boxes = new ArrayList<>();
		for (double[][] part : new double[][][] {BODY, leftTank ? LEFT_TANK : LEFT_POUCHES, rightTank ? RIGHT_TANK : RIGHT_POUCHES, battery ? BATTERY : FRONT_POUCH}) {
			for (double[] box : part) {
				boxes.add(rotate(box, facing));
			}
		}
		return Collections.unmodifiableList(boxes);
	}

	private static AxisAlignedBB rotate(double[] box, EnumFacing facing) {
		double minX = box[0] / 16, minY = box[1] / 16, minZ = box[2] / 16, maxX = box[3] / 16, maxY = box[4] / 16, maxZ = box[5] / 16;
		switch (facing) {
			case NORTH:
				return new AxisAlignedBB(1 - minX, minY, 1 - minZ, 1 - maxX, maxY, 1 - maxZ);
			case WEST:
				return new AxisAlignedBB(1 - minZ, minY, minX, 1 - maxZ, maxY, maxX);
			case EAST:
				return new AxisAlignedBB(minZ, minY, 1 - minX, maxZ, maxY, 1 - maxX);
			default:
				return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
		}
	}
}
