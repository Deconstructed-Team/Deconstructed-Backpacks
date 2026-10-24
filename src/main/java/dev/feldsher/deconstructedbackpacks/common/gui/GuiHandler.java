package dev.feldsher.deconstructedbackpacks.common.gui;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

/**
 * Opens backpack containers. What 1.21 writes into the menu's buffer goes in the three ints:
 * for {@link #ITEM_BACKPACK}, x is the backpack's slot in the player's inventory; for {@link #BLOCK_BACKPACK}, x y z are its position.
 */
public class GuiHandler implements IGuiHandler {
	public static final int ITEM_BACKPACK = 0;
	public static final int BLOCK_BACKPACK = 1;
	/** The offhand's slot in the player's inventory. */
	public static final int OFFHAND_SLOT = 40;

	@Nullable
	private static BackpackContext getContext(int id, int x, int y, int z) {
		switch (id) {
			case ITEM_BACKPACK:
				return new BackpackContext.Item(x);
			case BLOCK_BACKPACK:
				return new BackpackContext.Block(new BlockPos(x, y, z));
			default:
				return null;
		}
	}

	@Nullable
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		BackpackContext context = getContext(id, x, y, z);
		return context != null && context.canInteractWith(player) ? new BackpackContainer(player, context) : null;
	}

	@Nullable
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		BackpackContext context = getContext(id, x, y, z);
		return context != null && context.canInteractWith(player) ? DeconstructedBackpacks.proxy.createBackpackScreen(new BackpackContainer(player, context)) : null;
	}
}
