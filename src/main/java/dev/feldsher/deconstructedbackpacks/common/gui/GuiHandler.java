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
 * For a sub-backpack, its slot in the parent goes in y, above the 8 bits of a block's height.
 */
public class GuiHandler implements IGuiHandler {
	public static final int ITEM_BACKPACK = 0;
	public static final int BLOCK_BACKPACK = 1;
	public static final int ITEM_BACKPACK_SETTINGS = 2;
	public static final int BLOCK_BACKPACK_SETTINGS = 3;
	public static final int ITEM_SUB_BACKPACK = 4;
	public static final int BLOCK_SUB_BACKPACK = 5;
	public static final int ITEM_SUB_BACKPACK_SETTINGS = 6;
	public static final int BLOCK_SUB_BACKPACK_SETTINGS = 7;
	/** The offhand's slot in the player's inventory. */
	public static final int OFFHAND_SLOT = 40;

	@Nullable
	private static BackpackContext getContext(int id, int x, int y, int z) {
		switch (id) {
			case ITEM_BACKPACK:
			case ITEM_BACKPACK_SETTINGS:
				return new BackpackContext.Item(x);
			case BLOCK_BACKPACK:
			case BLOCK_BACKPACK_SETTINGS:
				return new BackpackContext.Block(new BlockPos(x, y, z));
			case ITEM_SUB_BACKPACK:
			case ITEM_SUB_BACKPACK_SETTINGS:
				return new BackpackContext.ItemSubBackpack(x, y);
			case BLOCK_SUB_BACKPACK:
			case BLOCK_SUB_BACKPACK_SETTINGS:
				return new BackpackContext.BlockSubBackpack(new BlockPos(x, y & 0xFF, z), y >>> 8);
			default:
				return null;
		}
	}

	@Nullable
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		BackpackContext context = getContext(id, x, y, z);
		if (context == null || !context.canInteractWith(player)) {
			return null;
		}
		return isSettings(id) ? new BackpackSettingsContainerMenu(player, context) : new BackpackContainer(player, context);
	}

	@Nullable
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		BackpackContext context = getContext(id, x, y, z);
		if (context == null || !context.canInteractWith(player)) {
			return null;
		}
		return isSettings(id) ? DeconstructedBackpacks.proxy.createBackpackSettingsScreen(new BackpackSettingsContainerMenu(player, context))
				: DeconstructedBackpacks.proxy.createBackpackScreen(new BackpackContainer(player, context));
	}

	private static boolean isSettings(int id) {
		return id == ITEM_BACKPACK_SETTINGS || id == BLOCK_BACKPACK_SETTINGS || id == ITEM_SUB_BACKPACK_SETTINGS || id == BLOCK_SUB_BACKPACK_SETTINGS;
	}
}
