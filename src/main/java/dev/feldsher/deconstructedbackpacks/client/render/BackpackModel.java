package dev.feldsher.deconstructedbackpacks.client.render;

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

/**
 * The backpack worn on the back: 1.21's BackpackModel, its cubes and UVs as they are, on 1.12's model renderers.
 * The tanks, the battery and its charge come with the upgrades that show them.
 */
public class BackpackModel extends ModelBase {
	private static final ResourceLocation BACKPACK_ENTITY_TEXTURE = DeconstructedBackpacks.getRL("textures/entity/backpack.png");
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

		// The clips' texture rows go by tier, as in 1.21: leather, copper, iron, gold, diamond, netherite
		addClips(ModItems.BACKPACK, 0);
		addClips(ModItems.IRON_BACKPACK, 2);
		addClips(ModItems.GOLD_BACKPACK, 3);
		addClips(ModItems.DIAMOND_BACKPACK, 4);
	}

	private ModelRenderer part(float x, float y, float z) {
		ModelRenderer part = new ModelRenderer(this);
		part.setRotationPoint(x, y, z);
		return part;
	}

	private static void box(ModelRenderer part, int u, int v, float x, float y, float z, int width, int height, int depth, boolean mirror) {
		part.cubeList.add(new ModelBox(part, u, v, x, y, z, width, height, depth, 0.0F, mirror));
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
	}

	/** Renders in the space the caller set up; see {@link #translateRotateAndScale}. */
	public void render(int clothColor, int borderColor, Item backpackItem) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(BACKPACK_ENTITY_TEXTURE);

		renderPart(fabricLeft, -1);
		renderPart(clipsLeftPouches.get(backpackItem), -1);
		renderPart(leftPouches, clothColor);
		renderPart(leftPouchesBorder, borderColor);

		renderPart(fabricRight, -1);
		renderPart(clipsRightPouches.get(backpackItem), -1);
		renderPart(rightPouches, clothColor);
		renderPart(rightPouchesBorder, borderColor);

		renderPart(fabricFront, -1);
		renderPart(clipsFrontPouch.get(backpackItem), -1);
		renderPart(frontPouch, clothColor);
		renderPart(frontPouchBorder, borderColor);

		renderPart(fabric, -1);
		renderPart(clipsBody.get(backpackItem), -1);
		renderPart(cloth, clothColor);
		renderPart(border, borderColor);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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

		if (livingEntity instanceof net.minecraft.entity.player.EntityPlayer) {
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
