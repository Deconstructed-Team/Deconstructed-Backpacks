package dev.feldsher.deconstructedbackpacks.registry.tool;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** The block a tool is looked for, where it is. */
public class BlockContext {
	private final World world;
	private final IBlockState state;
	private final BlockPos pos;

	public BlockContext(World world, IBlockState state, BlockPos pos) {
		this.world = world;
		this.state = state;
		this.pos = pos;
	}

	public World getWorld() {
		return world;
	}

	public IBlockState getState() {
		return state;
	}

	public Block getBlock() {
		return state.getBlock();
	}

	public BlockPos getPos() {
		return pos;
	}
}
