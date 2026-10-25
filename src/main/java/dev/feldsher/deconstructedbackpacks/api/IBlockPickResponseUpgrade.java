package dev.feldsher.deconstructedbackpacks.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** An upgrade that responds to the player picking a block they don't have in their inventory. */
public interface IBlockPickResponseUpgrade {
	/** @return true when it put the picked item into the player's hand and no other such upgrade should */
	boolean pickBlock(EntityPlayer player, ItemStack filter);
}
