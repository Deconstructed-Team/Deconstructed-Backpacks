package dev.feldsher.deconstructedbackpacks.client;

import net.minecraftforge.client.event.MouseEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import dev.feldsher.deconstructedbackpacks.network.BlockPickMessage;
import net.minecraft.util.text.TextComponentTranslation;
import dev.feldsher.deconstructedbackpacks.network.ToolSwapMessage;
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
import net.minecraftforge.client.settings.KeyModifier;
import dev.feldsher.deconstructedbackpacks.network.UpgradeToggleMessage;
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
 * The inventory interaction key runs the backpack's restock and deposit upgrades on the inventory looked at, and
 * the tool swap key its tool swapper on the block or entity looked at; the toggle upgrade keys switch its upgrade in
 * that slot on or off. Picking a block the player has no item of, out
 * of creative, asks the backpack's refill upgrades for it. The keys of the other upgrades come with them.
 */
@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID, value = Side.CLIENT)
public class KeybindHandler {
	private static final String KEYBIND_CATEGORY = "keybind.deconstructedbackpacks.category";
	private static final int MIDDLE_MOUSE_BUTTON = 2;

	public static final KeyBinding BACKPACK_OPEN_KEYBIND = new KeyBinding("keybind.deconstructedbackpacks.open_backpack",
			KeyConflictContext.UNIVERSAL, Keyboard.KEY_B, KEYBIND_CATEGORY);
	public static final KeyBinding INVENTORY_INTERACTION_KEYBIND = new KeyBinding("keybind.deconstructedbackpacks.inventory_interaction",
			KeyConflictContext.IN_GAME, Keyboard.KEY_C, KEYBIND_CATEGORY);
	public static final KeyBinding TOOL_SWAP_KEYBIND = new KeyBinding("keybind.deconstructedbackpacks.tool_swap",
			KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, KEYBIND_CATEGORY);
	public static final KeyBinding SORT_KEYBIND = new KeyBinding("keybind.deconstructedbackpacks.sort",
			KeyConflictContext.GUI, MIDDLE_MOUSE_BUTTON - 100, KEYBIND_CATEGORY);
	/** By upgrade slot. */
	public static final KeyBinding[] UPGRADE_SLOT_TOGGLE_KEYBINDS = {
			new KeyBinding("keybind.deconstructedbackpacks.toggle_upgrade_1", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_Z, KEYBIND_CATEGORY),
			new KeyBinding("keybind.deconstructedbackpacks.toggle_upgrade_2", KeyConflictContext.IN_GAME, KeyModifier.ALT, Keyboard.KEY_X, KEYBIND_CATEGORY),
			new KeyBinding("keybind.deconstructedbackpacks.toggle_upgrade_3", KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, KEYBIND_CATEGORY),
			new KeyBinding("keybind.deconstructedbackpacks.toggle_upgrade_4", KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, KEYBIND_CATEGORY),
			new KeyBinding("keybind.deconstructedbackpacks.toggle_upgrade_5", KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, KEYBIND_CATEGORY)
	};

	private KeybindHandler() {}

	public static void register() {
		ClientRegistry.registerKeyBinding(BACKPACK_OPEN_KEYBIND);
		ClientRegistry.registerKeyBinding(INVENTORY_INTERACTION_KEYBIND);
		ClientRegistry.registerKeyBinding(TOOL_SWAP_KEYBIND);
		ClientRegistry.registerKeyBinding(SORT_KEYBIND);
		for (KeyBinding keyBinding : UPGRADE_SLOT_TOGGLE_KEYBINDS) {
			ClientRegistry.registerKeyBinding(keyBinding);
		}
	}

	@SubscribeEvent
	public static void onKeyInput(InputEvent.KeyInputEvent event) {
		if (Minecraft.getMinecraft().currentScreen != null) {
			return;
		}
		if (Keyboard.getEventKeyState() && Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isActiveAndMatches(Keyboard.getEventKey())) {
			handleBlockPick();
		}
		if (BACKPACK_OPEN_KEYBIND.isPressed()) {
			PacketHandler.INSTANCE.sendToServer(new BackpackOpenMessage());
		} else if (INVENTORY_INTERACTION_KEYBIND.isPressed()) {
			sendInteractWithInventoryMessage();
		} else if (TOOL_SWAP_KEYBIND.isPressed()) {
			sendToolSwapMessage();
		} else {
			for (int upgradeSlot = 0; upgradeSlot < UPGRADE_SLOT_TOGGLE_KEYBINDS.length; upgradeSlot++) {
				if (UPGRADE_SLOT_TOGGLE_KEYBINDS[upgradeSlot].isPressed()) {
					PacketHandler.INSTANCE.sendToServer(new UpgradeToggleMessage(upgradeSlot));
				}
			}
		}
	}

	/** Fired before the key binding sees the click, which vanilla's pick block then ignores: the player has no such item. */
	@SubscribeEvent
	public static void onMouseInput(MouseEvent event) {
		if (event.isButtonstate() && event.getButton() >= 0 && Minecraft.getMinecraft().currentScreen == null
				&& Minecraft.getMinecraft().gameSettings.keyBindPickBlock.isActiveAndMatches(event.getButton() - 100)) {
			handleBlockPick();
		}
	}

	private static void handleBlockPick() {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.player;
		RayTraceResult target = mc.objectMouseOver;
		if (player == null || player.isCreative() || target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}
		BlockPos pos = target.getBlockPos();
		IBlockState state = mc.world.getBlockState(pos);
		if (state.getBlock().isAir(state, mc.world, pos)) {
			return;
		}
		ItemStack result = state.getBlock().getPickBlock(state, target, mc.world, pos, player);
		if (result.isEmpty() || player.inventory.getSlotFor(result) > -1) {
			return;
		}
		PacketHandler.INSTANCE.sendToServer(new BlockPickMessage(result));
	}

	private static void sendToolSwapMessage() {
		Minecraft mc = Minecraft.getMinecraft();
		RayTraceResult rayTrace = mc.objectMouseOver;
		if (mc.player == null || rayTrace == null) {
			return;
		}
		if (mc.player.getHeldItemMainhand().getItem() instanceof BackpackItem) {
			mc.player.sendStatusMessage(new TextComponentTranslation("gui.deconstructedbackpacks.status.unable_to_swap_tool_for_backpack"), true);
			return;
		}
		if (rayTrace.typeOfHit == RayTraceResult.Type.BLOCK) {
			PacketHandler.INSTANCE.sendToServer(ToolSwapMessage.block(rayTrace.getBlockPos()));
		} else if (rayTrace.typeOfHit == RayTraceResult.Type.ENTITY && rayTrace.entityHit != null) {
			PacketHandler.INSTANCE.sendToServer(ToolSwapMessage.entity(rayTrace.entityHit.getEntityId()));
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
