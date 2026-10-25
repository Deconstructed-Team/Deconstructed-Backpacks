package dev.feldsher.deconstructedbackpacks.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** An upgrade that, on the tool swap key, puts into the player's hand a tool that works on the block they look at. */
public interface IBlockToolSwapUpgrade {
	/** Whether this upgrade, of its tier, swaps on the key at all. */
	default boolean canProcessBlockInteract() {
		return true;
	}

	/** @return true when it handled the key and no other such upgrade should */
	boolean onBlockInteract(World world, BlockPos pos, IBlockState state, EntityPlayer player);
}
