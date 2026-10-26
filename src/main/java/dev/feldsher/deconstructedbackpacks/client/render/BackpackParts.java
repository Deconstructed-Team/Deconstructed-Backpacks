package dev.feldsher.deconstructedbackpacks.client.render;

import com.google.common.collect.ImmutableMap;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import dev.feldsher.deconstructedcore.renderdata.BatteryRenderInfo;
import dev.feldsher.deconstructedcore.renderdata.RenderInfo;
import dev.feldsher.deconstructedcore.renderdata.TankPosition;
import dev.feldsher.deconstructedcore.renderdata.TankRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The parts of a backpack's block and item model that its upgrades change, as 1.21's BackpackDynamicModel has them:
 * a pouch on each side and in front, or a tank in a side pouch's place and the battery in the front one's. Baked for
 * each tier, with its clips, and each facing. A tank's fluid and the battery's charge are quads made from the render info.
 */
public final class BackpackParts {
	public static final ResourceLocation MODULES_TEXTURE = DeconstructedBackpacks.getRL("block/backpack_modules");
	private static final Map<Item, Map<EnumFacing, Map<Part, List<BakedQuad>>>> BAKED = new HashMap<>();

	private BackpackParts() {}

	public enum Part {
		LEFT_POUCH("backpack_left_pouch"), RIGHT_POUCH("backpack_right_pouch"), FRONT_POUCH("backpack_front_pouch"),
		LEFT_TANK("backpack_left_tank"), RIGHT_TANK("backpack_right_tank"), BATTERY("backpack_battery");

		private final ResourceLocation model;

		Part(String model) {
			this.model = DeconstructedBackpacks.getRL("block/" + model);
		}
	}

	/** In the block's variants, as the blockstate files turn them. */
	public static int getYRotation(EnumFacing facing) {
		return ((int) facing.getHorizontalAngle() + 180) % 360;
	}

	public static void bake() {
		BAKED.clear();
		for (Item backpack : ModItems.getBackpacks()) {
			String clips = "deconstructedbackpacks:block/" + getClipsName(backpack);
			Map<EnumFacing, Map<Part, List<BakedQuad>>> byFacing = new EnumMap<>(EnumFacing.class);
			for (EnumFacing facing : EnumFacing.HORIZONTALS) {
				ModelRotation rotation = ModelRotation.getModelRotation(0, getYRotation(facing));
				Map<Part, List<BakedQuad>> parts = new EnumMap<>(Part.class);
				for (Part part : Part.values()) {
					IModel model = ModelLoaderRegistry.getModelOrMissing(part.model).retexture(ImmutableMap.of("clips", clips));
					IBakedModel baked = model.bake(rotation, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
					List<BakedQuad> quads = new ArrayList<>(baked.getQuads(null, null, 0));
					for (EnumFacing side : EnumFacing.values()) {
						quads.addAll(baked.getQuads(null, side, 0));
					}
					parts.put(part, quads);
				}
				byFacing.put(facing, parts);
			}
			BAKED.put(backpack, byFacing);
		}
	}

	private static String getClipsName(Item backpack) {
		String path = backpack.getRegistryName() == null ? "backpack" : backpack.getRegistryName().getPath();
		return path.equals("backpack") ? "leather_clips" : path.replace("_backpack", "_clips");
	}

	/** The parts the render info calls for, the fluids and the charge with them; all faces, culled by none. */
	public static List<BakedQuad> getQuads(Item backpack, EnumFacing facing, RenderInfo renderInfo) {
		Map<EnumFacing, Map<Part, List<BakedQuad>>> byFacing = BAKED.get(backpack);
		if (byFacing == null) {
			return Collections.emptyList();
		}
		Map<Part, List<BakedQuad>> parts = byFacing.get(facing);
		ModelRotation rotation = ModelRotation.getModelRotation(0, getYRotation(facing));
		List<BakedQuad> ret = new ArrayList<>();
		TankRenderInfo left = renderInfo.getTankRenderInfos().get(TankPosition.LEFT);
		if (left != null) {
			ret.addAll(parts.get(Part.LEFT_TANK));
			left.getFluid().ifPresent(fluid -> addFluid(ret, fluid, left.getFillRatio(), 12.85 / 16d, rotation));
		} else {
			ret.addAll(parts.get(Part.LEFT_POUCH));
		}
		TankRenderInfo right = renderInfo.getTankRenderInfos().get(TankPosition.RIGHT);
		if (right != null) {
			ret.addAll(parts.get(Part.RIGHT_TANK));
			right.getFluid().ifPresent(fluid -> addFluid(ret, fluid, right.getFillRatio(), 0.6 / 16d, rotation));
		} else {
			ret.addAll(parts.get(Part.RIGHT_POUCH));
		}
		Optional<BatteryRenderInfo> battery = renderInfo.getBatteryRenderInfo();
		if (battery.isPresent()) {
			ret.addAll(parts.get(Part.BATTERY));
			addCharge(ret, battery.get().getChargeRatio(), rotation);
		} else {
			ret.addAll(parts.get(Part.FRONT_POUCH));
		}
		return ret;
	}

	private static void addCharge(List<BakedQuad> ret, float chargeRatio, ModelRotation rotation) {
		int pixels = (int) (chargeRatio * BatteryRenderInfo.CHARGE_STEPS);
		if (pixels <= 0) {
			return;
		}
		float minX = (10 - pixels) / 16f;
		float minY = 2 / 16f;
		float minZ = 1.95f / 16f;
		float maxX = minX + pixels / 16f;
		float maxY = minY + 1 / 16f;
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(MODULES_TEXTURE.toString());
		ret.add(createQuad(new float[][] {{maxX, maxY, minZ}, {maxX, minY, minZ}, {minX, minY, minZ}, {minX, maxY, minZ}}, -1, sprite, EnumFacing.NORTH,
				14, 14 + pixels / 2f, 6, 6.5f, rotation));
	}

	private static void addFluid(List<BakedQuad> ret, FluidStack fluidStack, float ratio, double xMin, ModelRotation rotation) {
		if (ratio <= 0) {
			return;
		}
		float x1 = (float) xMin;
		float x2 = (float) (xMin + 2.5 / 16d);
		float y1 = 1.5f / 16f;
		float y2 = y1 + ratio * 6 / 16f;
		float z1 = 6.75f / 16f;
		float z2 = 9.25f / 16f;
		ResourceLocation still = fluidStack.getFluid().getStill(fluidStack);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(still == null ? "missingno" : still.toString());
		int color = fluidStack.getFluid().getColor(fluidStack);
		float v2 = ratio * 10;

		ret.add(createQuad(new float[][] {{x1, y2, z1}, {x1, y2, z2}, {x2, y2, z2}, {x2, y2, z1}}, color, sprite, EnumFacing.UP, 0, 5, 0, 5, rotation));
		ret.add(createQuad(new float[][] {{x2, y2, z1}, {x2, y1, z1}, {x1, y1, z1}, {x1, y2, z1}}, color, sprite, EnumFacing.NORTH, 0, 5, 0, v2, rotation));
		ret.add(createQuad(new float[][] {{x1, y2, z2}, {x1, y1, z2}, {x2, y1, z2}, {x2, y2, z2}}, color, sprite, EnumFacing.SOUTH, 0, 5, 0, v2, rotation));
		ret.add(createQuad(new float[][] {{x1, y2, z1}, {x1, y1, z1}, {x1, y1, z2}, {x1, y2, z2}}, color, sprite, EnumFacing.WEST, 0, 5, 0, v2, rotation));
		ret.add(createQuad(new float[][] {{x2, y2, z2}, {x2, y1, z2}, {x2, y1, z1}, {x2, y2, z1}}, color, sprite, EnumFacing.EAST, 0, 5, 0, v2, rotation));
	}

	private static int getQuarterTurns(ModelRotation rotation) {
		EnumFacing north = rotation.rotate(EnumFacing.NORTH);
		return north == EnumFacing.EAST ? 1 : north == EnumFacing.SOUTH ? 2 : north == EnumFacing.WEST ? 3 : 0;
	}

	/** Corners in order u1v1, u1v2, u2v2, u2v1; the UVs in the sprite's sixteenths, as a model's. */
	private static BakedQuad createQuad(float[][] corners, int argb, TextureAtlasSprite sprite, EnumFacing face, float u1, float u2, float v1, float v2,
			ModelRotation rotation) {
		VertexFormat format = DefaultVertexFormats.ITEM;
		EnumFacing rotatedFace = rotation.rotate(face);
		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		builder.setTexture(sprite);
		builder.setQuadOrientation(rotatedFace);
		builder.setApplyDiffuseLighting(true);
		float[][] uvs = {{u1, v1}, {u1, v2}, {u2, v2}, {u2, v1}};
		int quarterTurns = getQuarterTurns(rotation);
		for (int vertex = 0; vertex < 4; vertex++) {
			float x = corners[vertex][0] - 0.5f;
			float z = corners[vertex][2] - 0.5f;
			// As the model's y rotation turns north to east: (x, z) to (-z, x)
			for (int turn = 0; turn < quarterTurns; turn++) {
				float turnedX = -z;
				z = x;
				x = turnedX;
			}
			float[] pos = {x + 0.5f, corners[vertex][1], z + 0.5f};
			for (int e = 0; e < format.getElementCount(); e++) {
				VertexFormatElement element = format.getElement(e);
				switch (element.getUsage()) {
					case POSITION:
						builder.put(e, pos[0], pos[1], pos[2], 1);
						break;
					case COLOR:
						builder.put(e, (argb >> 16 & 255) / 255f, (argb >> 8 & 255) / 255f, (argb & 255) / 255f, (argb >>> 24) / 255f);
						break;
					case UV:
						if (element.getIndex() == 0) {
							builder.put(e, sprite.getInterpolatedU(uvs[vertex][0]), sprite.getInterpolatedV(uvs[vertex][1]), 0, 1);
						} else {
							builder.put(e);
						}
						break;
					case NORMAL:
						builder.put(e, rotatedFace.getXOffset(), rotatedFace.getYOffset(), rotatedFace.getZOffset(), 0);
						break;
					default:
						builder.put(e);
				}
			}
		}
		return builder.build();
	}
}
