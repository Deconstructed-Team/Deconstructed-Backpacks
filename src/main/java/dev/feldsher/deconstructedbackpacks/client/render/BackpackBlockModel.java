package dev.feldsher.deconstructedbackpacks.client.render;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackBlock;
import dev.feldsher.deconstructedcore.renderdata.RenderInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** A placed backpack facing one way: its base model, and the parts its render info calls for. */
public class BackpackBlockModel implements IBakedModel {
	private final IBakedModel baseModel;
	private final Item backpackItem;
	private final EnumFacing facing;

	public BackpackBlockModel(IBakedModel baseModel, Item backpackItem, EnumFacing facing) {
		this.baseModel = baseModel;
		this.backpackItem = backpackItem;
		this.facing = facing;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		List<BakedQuad> baseQuads = baseModel.getQuads(state, side, rand);
		if (side != null) {
			return baseQuads;
		}
		RenderInfo renderInfo = state instanceof IExtendedBlockState ? ((IExtendedBlockState) state).getValue(BackpackBlock.RENDER_INFO) : null;
		List<BakedQuad> ret = new ArrayList<>(baseQuads);
		ret.addAll(BackpackParts.getQuads(backpackItem, facing, renderInfo == null ? RenderInfo.EMPTY : renderInfo));
		return ret;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return baseModel.getParticleTexture();
	}

	@Override
	@SuppressWarnings("deprecation")
	public ItemCameraTransforms getItemCameraTransforms() {
		return baseModel.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}
}
