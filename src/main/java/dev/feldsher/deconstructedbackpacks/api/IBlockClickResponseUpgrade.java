package dev.feldsher.deconstructedbackpacks.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

/** An upgrade that responds to the player left clicking a block. */
public interface IBlockClickResponseUpgrade {
	/** @return true when it handled the click and no other such upgrade should */
	boolean onBlockClick(EntityPlayer player, BlockPos pos);
}
