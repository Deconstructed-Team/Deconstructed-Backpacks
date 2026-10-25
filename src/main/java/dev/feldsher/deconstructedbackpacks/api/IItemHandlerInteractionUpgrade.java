package dev.feldsher.deconstructedbackpacks.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;

/** An upgrade that acts on an inventory the player sneak right clicks with the backpack, or uses the interaction key on. */
public interface IItemHandlerInteractionUpgrade {
	void onHandlerInteract(IItemHandler itemHandler, EntityPlayer player);
}
