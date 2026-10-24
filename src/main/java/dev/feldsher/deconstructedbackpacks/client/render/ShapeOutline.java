package dev.feldsher.deconstructedbackpacks.client.render;

import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * The outline of boxes put together, as 1.21 draws a voxel shape's: the edges of what they fill, without the lines
 * where one box meets another. Each line is {x1, y1, z1, x2, y2, z2}.
 */
public final class ShapeOutline {
	private ShapeOutline() {}

	public static List<double[]> getEdges(List<AxisAlignedBB> boxes) {
		double[][] coords = {coordinates(boxes, 0), coordinates(boxes, 1), coordinates(boxes, 2)};
		int[] sizes = {coords[0].length - 1, coords[1].length - 1, coords[2].length - 1};
		boolean[][][] filled = new boolean[sizes[0]][sizes[1]][sizes[2]];
		for (int x = 0; x < sizes[0]; x++) {
			for (int y = 0; y < sizes[1]; y++) {
				for (int z = 0; z < sizes[2]; z++) {
					filled[x][y][z] = isInAnyBox(boxes, mid(coords[0], x), mid(coords[1], y), mid(coords[2], z));
				}
			}
		}

		List<double[]> edges = new ArrayList<>();
		for (int axis = 0; axis < 3; axis++) {
			addAxisEdges(axis, coords, sizes, filled, edges);
		}
		return edges;
	}

	/** The edges along an axis: where the four cells around a grid line aren't a flat face or all the same. */
	private static void addAxisEdges(int axis, double[][] coords, int[] sizes, boolean[][][] filled, List<double[]> edges) {
		int u = (axis + 1) % 3;
		int v = (axis + 2) % 3;
		for (int i = 0; i <= sizes[u]; i++) {
			for (int j = 0; j <= sizes[v]; j++) {
				int runStart = -1;
				for (int a = 0; a <= sizes[axis]; a++) {
					boolean edge = a < sizes[axis] && isEdge(filled, axis, u, v, a, i, j, sizes);
					if (edge && runStart < 0) {
						runStart = a;
					} else if (!edge && runStart >= 0) {
						double[] line = new double[6];
						line[axis] = coords[axis][runStart];
						line[3 + axis] = coords[axis][a];
						line[u] = line[3 + u] = coords[u][i];
						line[v] = line[3 + v] = coords[v][j];
						edges.add(line);
						runStart = -1;
					}
				}
			}
		}
	}

	private static boolean isEdge(boolean[][][] filled, int axis, int u, int v, int a, int i, int j, int[] sizes) {
		boolean c00 = cell(filled, axis, u, v, a, i - 1, j - 1, sizes);
		boolean c10 = cell(filled, axis, u, v, a, i, j - 1, sizes);
		boolean c01 = cell(filled, axis, u, v, a, i - 1, j, sizes);
		boolean c11 = cell(filled, axis, u, v, a, i, j, sizes);
		int count = (c00 ? 1 : 0) + (c10 ? 1 : 0) + (c01 ? 1 : 0) + (c11 ? 1 : 0);
		return count == 1 || count == 3 || (count == 2 && c00 == c11);
	}

	private static boolean cell(boolean[][][] filled, int axis, int u, int v, int a, int i, int j, int[] sizes) {
		if (i < 0 || j < 0 || i >= sizes[u] || j >= sizes[v]) {
			return false;
		}
		int[] index = new int[3];
		index[axis] = a;
		index[u] = i;
		index[v] = j;
		return filled[index[0]][index[1]][index[2]];
	}

	private static double[] coordinates(List<AxisAlignedBB> boxes, int axis) {
		TreeSet<Double> values = new TreeSet<>();
		for (AxisAlignedBB box : boxes) {
			values.add(round(min(box, axis)));
			values.add(round(max(box, axis)));
		}
		return values.stream().mapToDouble(Double::doubleValue).toArray();
	}

	private static double round(double value) {
		return Math.round(value * 1e6) / 1e6;
	}

	private static double mid(double[] coords, int index) {
		return (coords[index] + coords[index + 1]) / 2;
	}

	private static boolean isInAnyBox(List<AxisAlignedBB> boxes, double x, double y, double z) {
		for (AxisAlignedBB box : boxes) {
			if (x > box.minX && x < box.maxX && y > box.minY && y < box.maxY && z > box.minZ && z < box.maxZ) {
				return true;
			}
		}
		return false;
	}

	private static double min(AxisAlignedBB box, int axis) {
		return axis == 0 ? box.minX : axis == 1 ? box.minY : box.minZ;
	}

	private static double max(AxisAlignedBB box, int axis) {
		return axis == 0 ? box.maxX : axis == 1 ? box.maxY : box.maxZ;
	}
}
