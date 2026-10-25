package dev.feldsher.deconstructedbackpacks.client;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.client.gui.BackpackScreen;
import dev.feldsher.deconstructedbackpacks.network.BackpackOpenMessage;
import dev.feldsher.deconstructedbackpacks.network.InventoryInteractionMessage;
import dev.feldsher.deconstructedbackpacks.network.PacketHandler;
import dev.feldsher.deconstructedbackpacks.util.InventoryInteractionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * The open backpack key: in game it opens the first backpack the player carries; over a backpack in a screen's
 * player inventory it opens that one, and in a backpack's screen over anything else it closes the screen.
 * The sort key sorts the open backpack, unless the mouse is over the player's inventory, which is left to sorting mods.
 * Both are handled before those mods see the input, so a key they share sorts only one inventory.
 * The inventory interaction key runs the backpack's restock and deposit upgrades on the inventory looked at.
 * The keys of the other upgrades come with them.
 */
@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID, value = Side.CLIENT)
public class KeybindHandler {
	private static final String KEYBIND_CATEGORY = "keybind.deconstructedbackpacks.category";
	private static final int MIDDLE_MOUSE_BUTTON = 2;

	public static final KeyBinding BACKPACK_OPEN_KEYBIND = new KeyBinding("keybind.deconstructedbackpacks.open_backpack",
			KeyConflictContext.UNIVERSAL, Keyboard.KEY_B, KEYBIND_CATEGORY);
	public static final KeyBinding INVENTORY_INTERACTION_KEYBIND = new KeyBinding("keybind.deconstructedbackpacks.inventory_interaction",
			KeyConflictContext.IN_GAME, Keyboard.KEY_C, KEYBIND_CATEGORY);
	public static final KeyBinding SORT_KEYBIND = new KeyBinding("keybind.deconstructedbackpacks.sort",
			KeyConflictContext.GUI, MIDDLE_MOUSE_BUTTON - 100, KEYBIND_CATEGORY);

	private KeybindHandler() {}

	public static void register() {
		ClientRegistry.registerKeyBinding(BACKPACK_OPEN_KEYBIND);
		ClientRegistry.registerKeyBinding(INVENTORY_INTERACTION_KEYBIND);
		ClientRegistry.registerKeyBinding(SORT_KEYBIND);
	}

	@SubscribeEvent
	public static void onKeyInput(InputEvent.KeyInputEvent event) {
		if (Minecraft.getMinecraft().currentScreen != null) {
			return;
		}
		if (BACKPACK_OPEN_KEYBIND.isPressed()) {
			PacketHandler.INSTANCE.sendToServer(new BackpackOpenMessage());
		} else if (INVENTORY_INTERACTION_KEYBIND.isPressed()) {
			sendInteractWithInventoryMessage();
		}
	}

	private static void sendInteractWithInventoryMessage() {
		Minecraft mc = Minecraft.getMinecraft();
		RayTraceResult rayTrace = mc.objectMouseOver;
		if (rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.BLOCK || !InventoryInteractionHelper.hasItemHandler(mc.world, rayTrace.getBlockPos(), rayTrace.sideHit)) {
			return;
		}
		PacketHandler.INSTANCE.sendToServer(new InventoryInteractionMessage(rayTrace.getBlockPos(), rayTrace.sideHit));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onGuiMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (Mouse.getEventButtonState() && Mouse.getEventButton() >= 0
				&& SORT_KEYBIND.isActiveAndMatches(Mouse.getEventButton() - 100) && tryCallSort(event.getGui())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onGuiKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (!(event.getGui() instanceof GuiContainer) || !Keyboard.getEventKeyState()) {
			return;
		}
		if (SORT_KEYBIND.isActiveAndMatches(Keyboard.getEventKey()) && tryCallSort(event.getGui())) {
			event.setCanceled(true);
			return;
		}
		if (!BACKPACK_OPEN_KEYBIND.isActiveAndMatches(Keyboard.getEventKey())) {
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

	/**
	 * Sorts the open backpack when the mouse is over anything but the player's inventory. In creative, where the
	 * pick block key clones stacks, a sort key bound to it sorts only when there is nothing to clone or hold.
	 */
	private static boolean tryCallSort(GuiScreen gui) {
		if (!(gui instanceof BackpackScreen)) {
			return false;
		}
		BackpackScreen screen = (BackpackScreen) gui;
		Slot slot = screen.getSlotUnderMouse();
		if (slot != null && slot.inventory instanceof InventoryPlayer) {
			return false;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player.isCreative() && SORT_KEYBIND.getKeyCode() == mc.gameSettings.keyBindPickBlock.getKeyCode()
				&& (!mc.player.inventory.getItemStack().isEmpty() || slot != null && slot.getHasStack())) {
			return false;
		}
		screen.getMenu().sort();
		return true;
	}
}
