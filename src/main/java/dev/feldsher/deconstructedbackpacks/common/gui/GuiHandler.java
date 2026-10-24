package dev.feldsher.deconstructedbackpacks.common.gui;

import dev.feldsher.deconstructedbackpacks.client.gui.BackpackScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

/**
 * Opens backpack containers. What 1.21 writes into the menu's buffer goes in the three ints:
 * for {@link #ITEM_BACKPACK}, x is the backpack's slot in the player's inventory.
 */
public class GuiHandler implements IGuiHandler {
	public static final int ITEM_BACKPACK = 0;
	/** The offhand's slot in the player's inventory. */
	public static final int OFFHAND_SLOT = 40;

	@Nullable
	private static BackpackContext getContext(int id, int x) {
		return id == ITEM_BACKPACK ? new BackpackContext.Item(x) : null;
	}

	@Nullable
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		BackpackContext context = getContext(id, x);
		return context != null && context.canInteractWith(player) ? new BackpackContainer(player, context) : null;
	}

	@Nullable
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		BackpackContext context = getContext(id, x);
		return context != null && context.canInteractWith(player) ? new BackpackScreen(new BackpackContainer(player, context)) : null;
	}
}
