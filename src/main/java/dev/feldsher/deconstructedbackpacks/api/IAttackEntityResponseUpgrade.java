package dev.feldsher.deconstructedbackpacks.api;

import net.minecraft.entity.player.EntityPlayer;

/** An upgrade that responds to the player attacking an entity. */
public interface IAttackEntityResponseUpgrade {
	/** @return true when it handled the attack and no other such upgrade should */
	boolean onAttackEntity(EntityPlayer player);
}
