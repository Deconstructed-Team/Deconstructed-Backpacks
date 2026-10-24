package dev.feldsher.deconstructedbackpacks.backpack;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A placed backpack's shape: the boxes of its body and of what's on its sides and front, facing south as defined,
 * turned to the backpack's facing. 1.12 has no voxel shapes: the block collides and is aimed at by these boxes, and
 * its outline is drawn around them.
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

	// Tanks and the battery replace the pouches with the upgrades that show them

	private static final Map<EnumFacing, List<AxisAlignedBB>> SHAPES = new EnumMap<>(EnumFacing.class);
	private static final Map<EnumFacing, AxisAlignedBB> ENVELOPES = new EnumMap<>(EnumFacing.class);

	static {
		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			List<AxisAlignedBB> boxes = new ArrayList<>();
			for (double[][] part : new double[][][] {BODY, LEFT_POUCHES, RIGHT_POUCHES, FRONT_POUCH}) {
				for (double[] box : part) {
					boxes.add(rotate(box, facing));
				}
			}
			SHAPES.put(facing, Collections.unmodifiableList(boxes));
			AxisAlignedBB envelope = boxes.get(0);
			for (AxisAlignedBB box : boxes) {
				envelope = envelope.union(box);
			}
			ENVELOPES.put(facing, envelope);
		}
	}

	public static List<AxisAlignedBB> getShape(EnumFacing facing) {
		return SHAPES.get(facing);
	}

	/** The one box around all of the shape, which 1.12 wants for a block's bounds. */
	public static AxisAlignedBB getEnvelope(EnumFacing facing) {
		return ENVELOPES.get(facing);
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
