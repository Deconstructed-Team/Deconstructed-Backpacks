package dev.feldsher.deconstructedbackpacks.client;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.client.gui.BackpackScreen;
import dev.feldsher.deconstructedbackpacks.network.BackpackOpenMessage;
import dev.feldsher.deconstructedbackpacks.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

/**
 * The open backpack key: in game it opens the first backpack the player carries; over a backpack in a screen's
 * player inventory it opens that one, and in a backpack's screen over anything else it closes the screen.
 * The keys of the upgrades come with them.
 */
@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID, value = Side.CLIENT)
public class KeybindHandler {
	private static final String KEYBIND_CATEGORY = "keybind.deconstructedbackpacks.category";

	public static final KeyBinding BACKPACK_OPEN_KEYBIND = new KeyBinding("keybind.deconstructedbackpacks.open_backpack",
			KeyConflictContext.UNIVERSAL, Keyboard.KEY_B, KEYBIND_CATEGORY);

	private KeybindHandler() {}

	public static void register() {
		ClientRegistry.registerKeyBinding(BACKPACK_OPEN_KEYBIND);
	}

	@SubscribeEvent
	public static void onKeyInput(InputEvent.KeyInputEvent event) {
		if (Minecraft.getMinecraft().currentScreen == null && BACKPACK_OPEN_KEYBIND.isPressed()) {
			PacketHandler.INSTANCE.sendToServer(new BackpackOpenMessage());
		}
	}

	@SubscribeEvent
	public static void onGuiKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (!(event.getGui() instanceof GuiContainer) || !Keyboard.getEventKeyState()
				|| !BACKPACK_OPEN_KEYBIND.isActiveAndMatches(Keyboard.getEventKey())) {
			return;
		}
		GuiContainer screen = (GuiContainer) event.getGui();
		Slot slot = screen.getSlotUnderMouse();
		if (slot != null && slot.inventory instanceof InventoryPlayer && slot.getStack().getItem() instanceof BackpackItem) {
			PacketHandler.INSTANCE.sendToServer(new BackpackOpenMessage(slot.getSlotIndex()));
			event.setCanceled(true);
		} else if (screen instanceof BackpackScreen) {
			Minecraft.getMinecraft().player.closeScreen();
			event.setCanceled(true);
		}
	}
}
