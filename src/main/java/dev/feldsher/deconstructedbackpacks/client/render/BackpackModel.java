package dev.feldsher.deconstructedbackpacks.client.render;

import java.util.Optional;
import org.lwjgl.opengl.GL11;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import dev.feldsher.deconstructedcore.renderdata.TankRenderInfo;
import dev.feldsher.deconstructedcore.renderdata.TankPosition;
import dev.feldsher.deconstructedcore.renderdata.BatteryRenderInfo;
import dev.feldsher.deconstructedcore.renderdata.RenderInfo;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;

/**
 * The backpack worn on the back: 1.21's BackpackModel, its cubes and UVs as they are, on 1.12's model renderers, which
 * take whole sizes only: the tanks' glass, which is flat there, is flat here, and the fluids are drawn as plain quads.
 * Tanks take the side pouches' places and the battery the front pouch's, as the render info says.
 */
public class BackpackModel extends ModelBase {
	private static final ResourceLocation BACKPACK_ENTITY_TEXTURE = DeconstructedBackpacks.getRL("textures/entity/backpack.png");
	private static final ResourceLocation TANK_GLASS_TEXTURE = DeconstructedBackpacks.getRL("textures/entity/tank_glass.png");
	private static final float SCALE = 0.0625F;
	public static final float CHILD_Y_OFFSET = 0.3F;
	public static final float CHILD_Z_OFFSET = 0.1F;
	public static final float CHILD_SCALE = 0.55F;
	private static final float ENDERMAN_Y_OFFSET = -0.8F;

	private final ModelRenderer cloth;
	private final ModelRenderer rightPouchesBorder;
	private final ModelRenderer leftPouchesBorder;
	private final ModelRenderer frontPouchBorder;
	private final ModelRenderer frontPouch;
	private final ModelRenderer rightPouches;
	private final ModelRenderer leftPouches;
	private final ModelRenderer border;
	private final ModelRenderer fabricFront;
	private final ModelRenderer fabricRight;
	private final ModelRenderer fabricLeft;
	private final ModelRenderer fabric;
	private final Map<Item, ModelRenderer> clipsBody = new HashMap<>();
	private final Map<Item, ModelRenderer> clipsLeftPouches = new HashMap<>();
	private final Map<Item, ModelRenderer> clipsRightPouches = new HashMap<>();
	private final Map<Item, ModelRenderer> clipsFrontPouch = new HashMap<>();
	private final Map<Item, ModelRenderer> clipsBattery = new HashMap<>();
	private final Map<Integer, ModelRenderer> batteryCharges = new HashMap<>();
	private final ModelRenderer battery;
	private final ModelRenderer batteryBorder;
	private final ModelRenderer leftTank;
	private final ModelRenderer leftTankBorder;
	private final ModelRenderer rightTank;
	private final ModelRenderer rightTankBorder;
	private final GlassModel glassModel = new GlassModel();

	public BackpackModel() {
		textureWidth = 64;
		textureHeight = 64;

		cloth = part(0, 24, 0);
		box(cloth, 0, 0, -3.5F, -13.25F, -3.25F, 7, 4, 6, false);
		box(cloth, 0, 10, -5.0F, -13.0F, -3.0F, 10, 13, 6, false);

		rightPouchesBorder = part(0, 24, 0);
		box(rightPouchesBorder, 44, 0, 5.0F, -2.0F, -2.5F, 2, 1, 5, true);
		leftPouchesBorder = part(0, 24, 0);
		box(leftPouchesBorder, 44, 0, -7.0F, -2.0F, -2.5F, 2, 1, 5, false);
		frontPouchBorder = part(0, 24, 0);
		box(frontPouchBorder, 44, 0, -4.0F, -2.0F, -5.0F, 8, 1, 2, false);

		frontPouch = part(0, 24, 0);
		box(frontPouch, 25, 0, -4.0F, -1.0F, -5.0F, 8, 1, 2, false);
		box(frontPouch, 13, 2, -4.0F, -4.0F, -5.0F, 8, 2, 2, false);
		box(frontPouch, 13, 0, -4.0F, -6.0F, -5.0F, 8, 1, 2, false);

		rightPouches = part(0, 24, 0);
		box(rightPouches, 32, 5, 5.0F, -1.0F, -2.5F, 2, 1, 5, false);
		box(rightPouches, 32, 13, 5.0F, -4.0F, -2.5F, 2, 2, 5, false);
		box(rightPouches, 32, 11, 5.0F, -6.0F, -2.5F, 2, 1, 5, false);
		box(rightPouches, 32, 22, 5.0F, -9.0F, -2.5F, 1, 2, 5, false);
		box(rightPouches, 32, 20, 5.0F, -11.0F, -2.5F, 1, 1, 5, false);

		leftPouches = part(0, 24, 0);
		box(leftPouches, 32, 5, -7.0F, -1.0F, -2.5F, 2, 1, 5, true);
		box(leftPouches, 32, 13, -7.0F, -4.0F, -2.5F, 2, 2, 5, true);
		box(leftPouches, 32, 11, -7.0F, -6.0F, -2.5F, 2, 1, 5, true);
		box(leftPouches, 32, 22, -6.0F, -9.0F, -2.5F, 1, 2, 5, true);
		box(leftPouches, 32, 20, -6.0F, -11.0F, -2.5F, 1, 1, 5, true);

		border = part(0, 24, 0);
		box(border, 44, 7, -3.5F, -9.25F, -3.25F, 7, 1, 1, false);
		box(border, 50, 20, 3.5F, -13.25F, -3.25F, 1, 5, 6, false);
		box(border, 50, 9, -4.5F, -13.25F, -3.25F, 1, 5, 6, false);

		fabricFront = part(-3.25F, 16.0F, -6.0F);
		box(fabricFront, 12, 58, -0.75F, 3.0F, 1.0F, 8, 1, 2, true);
		fabricRight = part(-3.25F, 16.0F, -6.0F);
		box(fabricRight, 32, 49, 8.25F, -2.0F, 3.5F, 1, 1, 5, true);
		box(fabricRight, 8, 48, 8.25F, 3.0F, 3.5F, 2, 1, 5, true);
		fabricLeft = part(-3.25F, 16.0F, -6.0F);
		box(fabricLeft, 32, 49, -2.75F, -2.0F, 3.5F, 1, 1, 5, false);
		box(fabricLeft, 8, 48, -3.75F, 3.0F, 3.5F, 2, 1, 5, false);
		fabric = part(-3.25F, 16.0F, -6.0F);
		box(fabric, 54, 0, 1.25F, -4.75F, 5.75F, 1, 1, 1, false);
		box(fabric, 58, 0, 4.25F, -4.75F, 5.75F, 1, 1, 1, false);
		box(fabric, 44, 0, 1.25F, -5.75F, 5.75F, 4, 1, 1, true);
		box(fabric, 16, 47, 0.0F, -5.5F, 2.5F, 1, 4, 7, false);
		box(fabric, 0, 47, 5.5F, -5.5F, 2.5F, 1, 4, 7, false);

		batteryBorder = part(0, 24, 0);
		box(batteryBorder, 28, 38, -4.25F, -5.25F, -6.25F, 1, 1, 4, false);
		box(batteryBorder, 28, 43, -3.5F, -5.25F, -6.25F, 7, 1, 1, false);
		box(batteryBorder, 33, 38, -4.25F, -1.25F, -6.25F, 1, 1, 4, false);
		box(batteryBorder, 33, 38, 3.25F, -5.25F, -6.25F, 1, 1, 4, false);
		box(batteryBorder, 27, 45, -3.5F, -1.25F, -6.25F, 7, 1, 1, false);
		box(batteryBorder, 39, 37, 3.25F, -1.25F, -6.25F, 1, 1, 4, false);

		battery = part(0, 24, 0);
		box(battery, 28, 29, -4.0F, -6.0F, -6.0F, 8, 6, 3, false);
		box(battery, 28, 53, -2.0F, -6.25F, -4.5F, 1, 1, 1, false);
		box(battery, 28, 53, -0.75F, -6.25F, -4.5F, 1, 1, 1, false);
		box(battery, 28, 53, -2.0F, -8.0F, -3.25F, 1, 1, 1, false);
		box(battery, 28, 53, -0.75F, -8.0F, -3.25F, 1, 1, 1, false);
		box(battery, 0, 58, -2.0F, -7.4F, -4.5F, 1, 2, 1, -0.2F, false);
		box(battery, 6, 58, -0.75F, -7.4F, -4.5F, 1, 2, 1, -0.2F, false);
		box(battery, 0, 61, -2.0F, -8.0F, -4.5F, 1, 1, 2, -0.2F, false);
		box(battery, 6, 61, -0.75F, -8.0F, -4.5F, 1, 1, 2, -0.2F, false);

		leftTankBorder = part(0, 24, 0);
		box(leftTankBorder, 50, 43, -8.0F, -9.5F, -2.0F, 3, 1, 4, false);
		leftTank = part(0, 24, 0);
		box(leftTank, 54, 27, -5.5F, -7.5F, -2.0F, 1, 6, 4, false);
		box(leftTank, 50, 37, -8.0F, -1.5F, -2.0F, 3, 1, 4, false);
		box(leftTank, 50, 42, -8.0F, -8.5F, -2.0F, 3, 1, 4, false);
		box(leftTank, 50, 37, -8.0F, -10.5F, -2.0F, 3, 1, 4, false);
		box(leftTank, 52, 48, -7.5F, -11.5F, -1.5F, 3, 1, 3, false);

		rightTankBorder = part(0, 24, 0);
		box(rightTankBorder, 50, 43, 5.0F, -9.5F, -2.0F, 3, 1, 4, true);
		rightTank = part(0, 24, 0);
		box(rightTank, 54, 27, 4.5F, -7.5F, -2.0F, 1, 6, 4, true);
		box(rightTank, 50, 37, 5.0F, -1.5F, -2.0F, 3, 1, 4, true);
		box(rightTank, 50, 42, 5.0F, -8.5F, -2.0F, 3, 1, 4, true);
		box(rightTank, 50, 37, 5.0F, -10.5F, -2.0F, 3, 1, 4, true);
		box(rightTank, 52, 48, 4.5F, -11.5F, -1.5F, 3, 1, 3, true);

		for (int pixels = 1; pixels <= BatteryRenderInfo.CHARGE_STEPS; pixels++) {
			ModelRenderer charge = part(0, 0, 0);
			box(charge, 30, 58, -2.0F, 21F, -6.01F, pixels, 1, 1, false);
			batteryCharges.put(pixels, charge);
		}

		// The clips' texture rows go by tier, as in 1.21: leather, copper, iron, gold, diamond, netherite
		addClips(ModItems.BACKPACK, 0);
		addClips(ModItems.COPPER_BACKPACK, 1);
		addClips(ModItems.IRON_BACKPACK, 2);
		addClips(ModItems.GOLD_BACKPACK, 3);
		addClips(ModItems.DIAMOND_BACKPACK, 4);
		addClips(ModItems.NETHERITE_BACKPACK, 5);
	}

	private ModelRenderer part(float x, float y, float z) {
		ModelRenderer part = new ModelRenderer(this);
		part.setRotationPoint(x, y, z);
		return part;
	}

	private static void box(ModelRenderer part, int u, int v, float x, float y, float z, int width, int height, int depth, boolean mirror) {
		box(part, u, v, x, y, z, width, height, depth, 0.0F, mirror);
	}

	private static void box(ModelRenderer part, int u, int v, float x, float y, float z, int width, int height, int depth, float delta, boolean mirror) {
		part.cubeList.add(new ModelBox(part, u, v, x, y, z, width, height, depth, delta, mirror));
	}

	private void addClips(Item backpackItem, int tierIndex) {
		int yTextureOffset = 29 + tierIndex * 3;

		ModelRenderer body = part(0, 24, 0);
		box(body, 22, yTextureOffset, -3.25F, -9.5F, -3.5F, 1, 2, 1, false);
		box(body, 25, yTextureOffset, 2.25F, -9.5F, -3.5F, 1, 2, 1, false);
		clipsBody.put(backpackItem, body);

		ModelRenderer left = part(0, 24, 0);
		box(left, 18, yTextureOffset, -6.25F, -10.0F, -0.5F, 1, 2, 1, false);
		box(left, 6, yTextureOffset, -7.25F, -5.0F, -0.5F, 1, 2, 1, false);
		clipsLeftPouches.put(backpackItem, left);

		ModelRenderer right = part(0, 24, 0);
		box(right, 6, yTextureOffset, 6.25F, -5.0F, -0.5F, 1, 2, 1, true);
		box(right, 18, yTextureOffset, 5.25F, -10.0F, -0.5F, 1, 2, 1, true);
		clipsRightPouches.put(backpackItem, right);

		ModelRenderer front = part(0, 24, 0);
		box(front, 0, yTextureOffset, 2.0F, -5.0F, -5.25F, 1, 2, 1, false);
		box(front, 3, yTextureOffset, -3.0F, -5.0F, -5.25F, 1, 2, 1, false);
		clipsFrontPouch.put(backpackItem, front);

		ModelRenderer batteryClips = part(0, 24, 0);
		box(batteryClips, 24, yTextureOffset + 1, 1.0F, -5.25F, -6.15F, 1, 1, 1, 0.2F, false);
		box(batteryClips, 21, yTextureOffset + 1, 1.0F, -1.25F, -6.15F, 1, 1, 1, 0.2F, false);
		clipsBattery.put(backpackItem, batteryClips);
	}

	/** Renders in the space the caller set up; see {@link #translateRotateAndScale}. */
	public void render(int clothColor, int borderColor, Item backpackItem, RenderInfo renderInfo) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(BACKPACK_ENTITY_TEXTURE);
		TankRenderInfo leftTankInfo = renderInfo.getTankRenderInfos().get(TankPosition.LEFT);
		TankRenderInfo rightTankInfo = renderInfo.getTankRenderInfos().get(TankPosition.RIGHT);
		Optional<BatteryRenderInfo> batteryInfo = renderInfo.getBatteryRenderInfo();

		if (leftTankInfo != null) {
			renderPart(leftTank, -1);
			renderPart(leftTankBorder, borderColor);
		} else {
			renderPart(fabricLeft, -1);
			renderPart(clipsLeftPouches.get(backpackItem), -1);
			renderPart(leftPouches, clothColor);
			renderPart(leftPouchesBorder, borderColor);
		}

		if (rightTankInfo != null) {
			renderPart(rightTank, -1);
			renderPart(rightTankBorder, borderColor);
		} else {
			renderPart(fabricRight, -1);
			renderPart(clipsRightPouches.get(backpackItem), -1);
			renderPart(rightPouches, clothColor);
			renderPart(rightPouchesBorder, borderColor);
		}

		if (batteryInfo.isPresent()) {
			renderPart(battery, -1);
			renderPart(batteryBorder, borderColor);
			renderPart(clipsBattery.get(backpackItem), -1);
			renderPart(batteryCharges.get((int) (batteryInfo.get().getChargeRatio() * BatteryRenderInfo.CHARGE_STEPS)), -1);
		} else {
			renderPart(fabricFront, -1);
			renderPart(clipsFrontPouch.get(backpackItem), -1);
			renderPart(frontPouch, clothColor);
			renderPart(frontPouchBorder, borderColor);
		}

		renderPart(fabric, -1);
		renderPart(clipsBody.get(backpackItem), -1);
		renderPart(cloth, clothColor);
		renderPart(border, borderColor);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		if (leftTankInfo != null || rightTankInfo != null) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(1 / 2F, 6 / 10F, 1 / 2F);
			Minecraft.getMinecraft().getTextureManager().bindTexture(TANK_GLASS_TEXTURE);
			if (leftTankInfo != null) {
				glassModel.left.render(SCALE);
			}
			if (rightTankInfo != null) {
				glassModel.right.render(SCALE);
			}
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			if (leftTankInfo != null) {
				leftTankInfo.getFluid().ifPresent(fluid -> renderFluid(fluid, leftTankInfo.getFillRatio(), true));
			}
			if (rightTankInfo != null) {
				rightTankInfo.getFluid().ifPresent(fluid -> renderFluid(fluid, rightTankInfo.getFillRatio(), false));
			}
			GlStateManager.popMatrix();
		}
	}

	/** The fluid in a tank, as a box in the model's space: as tall as the tank is full, ten pixels when it's full. */
	private static void renderFluid(FluidStack fluidStack, float fillRatio, boolean left) {
		int fill = (int) (fillRatio * 10);
		if (fill <= 0) {
			return;
		}
		ResourceLocation still = fluidStack.getFluid().getStill(fluidStack);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(still == null ? "missingno" : still.toString());
		int color = fluidStack.getFluid().getColor(fluidStack);
		float x1 = left ? -14.5F : 11F;
		float x2 = x1 + 3.5F;
		float y1 = 24 + 13.5F - fill;
		float y2 = 24 + 13.5F;
		float z1 = -2F;
		float z2 = 2F;

		GlStateManager.color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, (color >>> 24) / 255F);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
		// y grows downwards in the model: y1 is the fluid's top
		face(buffer, sprite, new float[][] {{x1, y1, z1}, {x1, y1, z2}, {x2, y1, z2}, {x2, y1, z1}}, 0, -1, 0, x2 - x1, z2 - z1);
		face(buffer, sprite, new float[][] {{x1, y1, z1}, {x2, y1, z1}, {x2, y2, z1}, {x1, y2, z1}}, 0, 0, -1, x2 - x1, y2 - y1);
		face(buffer, sprite, new float[][] {{x2, y1, z2}, {x1, y1, z2}, {x1, y2, z2}, {x2, y2, z2}}, 0, 0, 1, x2 - x1, y2 - y1);
		face(buffer, sprite, new float[][] {{x1, y1, z2}, {x1, y1, z1}, {x1, y2, z1}, {x1, y2, z2}}, -1, 0, 0, z2 - z1, y2 - y1);
		face(buffer, sprite, new float[][] {{x2, y1, z1}, {x2, y1, z2}, {x2, y2, z2}, {x2, y2, z1}}, 1, 0, 0, z2 - z1, y2 - y1);
		face(buffer, sprite, new float[][] {{x1, y2, z2}, {x1, y2, z1}, {x2, y2, z1}, {x2, y2, z2}}, 0, 1, 0, x2 - x1, z2 - z1);
		tessellator.draw();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	/** Corners clockwise from the sprite's top left; as many of its pixels across and down as the face is long. */
	private static void face(BufferBuilder buffer, TextureAtlasSprite sprite, float[][] corners, float nx, float ny, float nz, float width, float height) {
		float u1 = sprite.getMinU();
		float u2 = sprite.getInterpolatedU(Math.min(16, width));
		float v1 = sprite.getMinV();
		float v2 = sprite.getInterpolatedV(Math.min(16, height));
		float[][] uvs = {{u1, v1}, {u2, v1}, {u2, v2}, {u1, v2}};
		for (int i = 0; i < 4; i++) {
			buffer.pos(corners[i][0] * SCALE, corners[i][1] * SCALE, corners[i][2] * SCALE).tex(uvs[i][0], uvs[i][1]).normal(nx, ny, nz).endVertex();
		}
	}

	/** The tanks' glass, flat: a model of its own, for its own texture. */
	private static class GlassModel extends ModelBase {
		private final ModelRenderer left;
		private final ModelRenderer right;

		private GlassModel() {
			textureWidth = 32;
			textureHeight = 32;
			left = new ModelRenderer(this);
			left.setRotationPoint(0, 24, 0);
			left.cubeList.add(new ModelBox(left, 18, 5, -15F, 3.5F, -2.5F, 4, 10, 0, 0.0F, false));
			left.cubeList.add(new ModelBox(left, 0, 0, -15F, 3.5F, -2.5F, 0, 10, 5, 0.0F, false));
			left.cubeList.add(new ModelBox(left, 10, 5, -15F, 3.5F, 2.5F, 4, 10, 0, 0.0F, false));
			right = new ModelRenderer(this);
			right.setRotationPoint(0, 24, 0);
			right.cubeList.add(new ModelBox(right, 18, 5, 11F, 3.5F, -2.5F, 4, 10, 0, 0.0F, true));
			right.cubeList.add(new ModelBox(right, 0, 0, 15F, 3.5F, -2.5F, 0, 10, 5, 0.0F, true));
			right.cubeList.add(new ModelBox(right, 10, 5, 11F, 3.5F, 2.5F, 4, 10, 0, 0.0F, true));
		}
	}

	private static void renderPart(ModelRenderer part, int color) {
		if (part == null) {
			return;
		}
		GlStateManager.color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0F);
		part.render(SCALE);
	}

	/**
	 * From the body of a biped model to the backpack's place on its back.
	 * The caller has applied the body's own rotation (ModelRenderer#postRender) already.
	 */
	public static void translateRotateAndScale(EntityLivingBase livingEntity, boolean wearsArmor) {
		GlStateManager.rotate(180, 0, 1, 0);
		float zOffset = wearsArmor ? -0.35f : -0.3f;
		float yOffset = -0.75f;

		if (livingEntity.isChild()) {
			zOffset += CHILD_Z_OFFSET;
			yOffset = CHILD_Y_OFFSET;
		}

		GlStateManager.translate(0, yOffset, zOffset);

		if (livingEntity instanceof EntityPlayer) {
			return;
		}

		if (livingEntity.isChild()) {
			GlStateManager.scale(CHILD_SCALE, CHILD_SCALE, CHILD_SCALE);
		}

		if (livingEntity instanceof EntityEnderman) {
			GlStateManager.translate(0, ENDERMAN_Y_OFFSET, 0);
		}
	}
}
