package dev.feldsher.deconstructedbackpacks.client.render;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import net.minecraft.item.Item;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedcore.renderdata.RenderInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;
import java.util.List;

/**
 * A backpack item's model: its base model with the parts its render info calls for, pouches when it has none. A stack
 * that shows an item gets one that hands drawing to {@link BackpackItemStackRenderer}, held, worn on the head or in a
 * slot alike, with the plain one's transforms.
 */
public class BackpackItemModel implements IBakedModel {
	private static final int MAX_CACHED_MODELS = 256;
	private final IBakedModel originalModel;
	private final Item backpackItem;
	private final List<BakedQuad> defaultParts;
	private final IBakedModel builtInModel = new BuiltIn();
	private final Map<NBTTagCompound, IBakedModel> modelsByRenderInfo = new HashMap<>();
	private final ItemOverrideList overrides = new ItemOverrideList(Collections.emptyList()) {
		@Override
		public IBakedModel handleItemState(IBakedModel model, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
			NBTTagCompound renderInfoTag = getRenderInfoTag(stack);
			if (renderInfoTag == null) {
				return BackpackItemModel.this;
			}
			RenderInfo renderInfo = BackpackWrapper.fromStack(stack).getRenderInfo();
			if (renderInfo.getItemDisplayRenderInfo().getDisplayItem().isPresent()) {
				return builtInModel;
			}
			if (modelsByRenderInfo.size() > MAX_CACHED_MODELS) {
				modelsByRenderInfo.clear();
			}
			return modelsByRenderInfo.computeIfAbsent(renderInfoTag.copy(), tag -> new WithParts(getParts(renderInfo)));
		}
	};

	public BackpackItemModel(IBakedModel originalModel, Item backpackItem) {
		this.originalModel = originalModel;
		this.backpackItem = backpackItem;
		defaultParts = getParts(RenderInfo.EMPTY);
	}

	private List<BakedQuad> getParts(RenderInfo renderInfo) {
		return BackpackParts.getQuads(backpackItem, EnumFacing.NORTH, renderInfo);
	}

	/** The quads the stack is drawn with: the base model's and those of the parts its render info calls for. */
	public List<BakedQuad> getQuads(ItemStack stack, @Nullable EnumFacing side) {
		List<BakedQuad> baseQuads = originalModel.getQuads(null, side, 0);
		if (side != null) {
			return baseQuads;
		}
		List<BakedQuad> ret = new ArrayList<>(baseQuads);
		ret.addAll(getRenderInfoTag(stack) == null ? defaultParts : getParts(BackpackWrapper.fromStack(stack).getRenderInfo()));
		return ret;
	}

	/** The stack carries what it shows, so this needs no lookup of its contents. */
	@Nullable
	private static NBTTagCompound getRenderInfoTag(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		return tag != null && tag.hasKey(RenderInfo.RENDER_INFO_TAG) ? tag.getCompoundTag(RenderInfo.RENDER_INFO_TAG) : null;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		List<BakedQuad> baseQuads = originalModel.getQuads(state, side, rand);
		if (side != null) {
			return baseQuads;
		}
		List<BakedQuad> ret = new ArrayList<>(baseQuads);
		ret.addAll(defaultParts);
		return ret;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return originalModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return originalModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return originalModel.getParticleTexture();
	}

	@Override
	@SuppressWarnings("deprecation")
	public ItemCameraTransforms getItemCameraTransforms() {
		return originalModel.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return overrides;
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
		return Pair.of(this, originalModel.handlePerspective(cameraTransformType).getRight());
	}

	/** One render info's parts. */
	private class WithParts implements IBakedModel {
		private final List<BakedQuad> parts;

		private WithParts(List<BakedQuad> parts) {
			this.parts = parts;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
			List<BakedQuad> baseQuads = originalModel.getQuads(state, side, rand);
			if (side != null) {
				return baseQuads;
			}
			List<BakedQuad> ret = new ArrayList<>(baseQuads);
			ret.addAll(parts);
			return ret;
		}

		@Override
		public boolean isAmbientOcclusion() {
			return originalModel.isAmbientOcclusion();
		}

		@Override
		public boolean isGui3d() {
			return originalModel.isGui3d();
		}

		@Override
		public boolean isBuiltInRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return originalModel.getParticleTexture();
		}

		@Override
		@SuppressWarnings("deprecation")
		public ItemCameraTransforms getItemCameraTransforms() {
			return originalModel.getItemCameraTransforms();
		}

		@Override
		public ItemOverrideList getOverrides() {
			return ItemOverrideList.NONE;
		}

		@Override
		public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
			return Pair.of(this, originalModel.handlePerspective(cameraTransformType).getRight());
		}
	}

	private class BuiltIn implements IBakedModel {
		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
			return Collections.emptyList();
		}

		@Override
		public boolean isAmbientOcclusion() {
			return originalModel.isAmbientOcclusion();
		}

		@Override
		public boolean isGui3d() {
			return originalModel.isGui3d();
		}

		@Override
		public boolean isBuiltInRenderer() {
			return true;
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return originalModel.getParticleTexture();
		}

		@Override
		@SuppressWarnings("deprecation")
		public ItemCameraTransforms getItemCameraTransforms() {
			return originalModel.getItemCameraTransforms();
		}

		@Override
		public ItemOverrideList getOverrides() {
			return ItemOverrideList.NONE;
		}

		@Override
		public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
			return Pair.of(this, originalModel.handlePerspective(cameraTransformType).getRight());
		}
	}
}
