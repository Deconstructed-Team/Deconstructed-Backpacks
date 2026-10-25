package dev.feldsher.deconstructedbackpacks.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/** An upgrade that, on the tool swap key, puts into the player's hand a tool that works on the entity they look at. */
public interface IEntityToolSwapUpgrade {
	/** Whether this upgrade, of its tier, swaps on the key at all. */
	default boolean canProcessEntityInteract() {
		return true;
	}

	/** @return true when it handled the key and no other such upgrade should */
	boolean onEntityInteract(World world, Entity entity, EntityPlayer player);
}
