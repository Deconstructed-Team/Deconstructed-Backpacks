package dev.feldsher.deconstructedbackpacks.client.render;

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
 * A backpack item's model: the plain one, but a stack that shows an item gets one that hands drawing to
 * {@link BackpackItemStackRenderer}, held, worn on the head or in a slot alike, with the plain one's transforms.
 */
public class BackpackItemModel implements IBakedModel {
	private final IBakedModel originalModel;
	private final IBakedModel builtInModel = new BuiltIn();
	private final ItemOverrideList overrides = new ItemOverrideList(Collections.emptyList()) {
		@Override
		public IBakedModel handleItemState(IBakedModel model, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
			return showsItem(stack) ? builtInModel : originalModel.getOverrides().handleItemState(originalModel, stack, world, entity);
		}
	};

	public BackpackItemModel(IBakedModel originalModel) {
		this.originalModel = originalModel;
	}

	public IBakedModel getOriginalModel() {
		return originalModel;
	}

	/** The stack carries what it shows, so this needs no lookup of its contents. */
	private static boolean showsItem(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		return tag != null && tag.hasKey(RenderInfo.RENDER_INFO_TAG);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		return originalModel.getQuads(state, side, rand);
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
