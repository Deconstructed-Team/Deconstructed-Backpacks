package dev.feldsher.deconstructedbackpacks.backpack;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * A dropped item that fire and lava don't burn, as 1.21's fire resistant items. Registered, so that it's saved with its
 * chunk; drawn by the item entity renderer, which the game finds through the superclass.
 */
public class FireResistantItemEntity extends EntityItem {
	@SuppressWarnings("unused") // as an entity's, for loading
	public FireResistantItemEntity(World world) {
		super(world);
		isImmuneToFire = true;
	}

	/** Takes the place of the item entity the game made: all of it, its motion and pickup delay too, but its UUID. */
	public FireResistantItemEntity(World world, EntityItem original, ItemStack stack) {
		super(world, original.posX, original.posY, original.posZ, stack);
		NBTTagCompound tag = original.writeToNBT(new NBTTagCompound());
		tag.removeTag("UUIDMost");
		tag.removeTag("UUIDLeast");
		readFromNBT(tag);
		setItem(stack);
		isImmuneToFire = true;
	}
}
