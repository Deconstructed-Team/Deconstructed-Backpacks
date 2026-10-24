package dev.feldsher.deconstructedbackpacks.common.gui;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Opens a backpack's container again once its upgrades changed, so that its settings tabs and their slots are built
 * anew. 1.21 rebuilds them in the open menu with packets of its own; 1.12's containers can't change their slots
 * while open. The reopening waits for the end of the tick, out of the click that changed the upgrades, and keeps
 * the stack the player holds on the cursor.
 */
@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID)
public class ContainerReopener {
	private static final Map<EntityPlayerMP, Container> PENDING = new HashMap<>();

	private ContainerReopener() {}

	public static void scheduleReopen(EntityPlayerMP player, Container container) {
		PENDING.put(player, container);
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
			return;
		}
		Map<EntityPlayerMP, Container> pending = new HashMap<>(PENDING);
		PENDING.clear();
		pending.forEach(ContainerReopener::reopen);
	}

	private static void reopen(EntityPlayerMP player, Container container) {
		if (player.openContainer != container || !(container instanceof BackpackContainer)) {
			return;
		}
		BackpackContext context = ((BackpackContainer) container).getBackpackContext();
		if (!context.canInteractWith(player)) {
			return;
		}

		ItemStack cursorStack = player.inventory.getItemStack();
		player.inventory.setItemStack(ItemStack.EMPTY);
		context.open(player);
		player.inventory.setItemStack(cursorStack);
		player.updateHeldItem();
	}
}
