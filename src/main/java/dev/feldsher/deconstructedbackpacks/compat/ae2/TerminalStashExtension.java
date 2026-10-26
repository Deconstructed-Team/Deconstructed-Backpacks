package dev.feldsher.deconstructedbackpacks.compat.ae2;

import appeng.client.me.SlotME;
import dev.feldsher.deconstructedbackpacks.network.PacketHandler;
import dev.feldsher.deconstructedcore.api.IStashSlotExtension;
import dev.feldsher.deconstructedcore.api.IStashStorageItem;
import dev.feldsher.deconstructedcore.client.StashHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/** The items of the network in a terminal, which the client alone has as slots. */
public final class TerminalStashExtension implements IStashSlotExtension {
	private TerminalStashExtension() {}

	/** Only with {@link Ae2Compat#isAvailable()}. */
	public static void register() {
		StashHandler.registerExtension(new TerminalStashExtension());
	}

	@Override
	public boolean handles(Slot slot) {
		return slot instanceof SlotME;
	}

	@Nullable
	@Override
	public IStashStorageItem.StashResult getStashResult(Slot slot, ItemStack held) {
		ItemStack inNetwork = slot.getStack();
		if (inNetwork.isEmpty() || held.getCount() != 1 || !(held.getItem() instanceof IStashStorageItem)) {
			return null;
		}
		ItemStack one = inNetwork.copy();
		one.setCount(1);
		IStashStorageItem.StashResult result = ((IStashStorageItem) held.getItem()).getItemStashable(held, one);
		return result == IStashStorageItem.StashResult.NO_SPACE ? null : result;
	}

	@Override
	public void stash(GuiContainer screen, Slot slot) {
		ItemStack one = slot.getStack().copy();
		one.setCount(1);
		PacketHandler.INSTANCE.sendToServer(new NetworkStashMessage(Minecraft.getMinecraft().player.openContainer.windowId, one, GuiScreen.isShiftKeyDown()));
	}
}
