package dev.feldsher.deconstructedbackpacks.upgrades.refill;

import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContainer;
import dev.feldsher.deconstructedcore.client.gui.StorageScreenBase;
import dev.feldsher.deconstructedcore.client.gui.UpgradeSettingsTab;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.client.gui.utils.TranslationHelper;
import dev.feldsher.deconstructedcore.upgrades.FilterLogic;
import dev.feldsher.deconstructedcore.upgrades.FilterLogicContainer;
import dev.feldsher.deconstructedcore.upgrades.FilterLogicControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The refill filter. In the advanced one each filter slot shows its target slot, which scrolling over it changes;
 * the change is sent when the mouse leaves the slot, and the hovered filter item's tooltip tells the target.
 */
public abstract class RefillUpgradeTab extends UpgradeSettingsTab<RefillUpgradeContainer> {
	private static final int TARGET_SLOT_COLOR = 0x55FF55;
	private final FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>> filterLogicControl;
	private int slotBeingChanged = -1;
	private TargetSlot targetSlotBeingChanged = null;

	private static List<String> additionalTooltip = new ArrayList<>();

	static {
		MinecraftForge.EVENT_BUS.register(RefillUpgradeTab.class);
	}

	@SubscribeEvent
	public static void addToTooltip(ItemTooltipEvent event) {
		if (!additionalTooltip.isEmpty() && Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.openContainer instanceof BackpackContainer) {
			event.getToolTip().addAll(additionalTooltip);
		}
	}

	protected RefillUpgradeTab(RefillUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen, int slotsInRow, String upgradeName) {
		super(upgradeContainer, position, screen, SBPTranslationHelper.INSTANCE.translUpgrade(upgradeName),
				new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translUpgradeKey(upgradeName) + TranslationHelper.TOOLTIP_SUFFIX));
		filterLogicControl = addHideableChild(new RefillFilterLogicControl(screen, slotsInRow));
	}

	@Override
	protected void moveSlotsToTab() {
		filterLogicControl.moveSlotsToView();
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if (!shouldRender.getAsBoolean()) {
			return;
		}
		super.render(mouseX, mouseY, partialTicks);

		if (!isOpen() || !filterLogicControl.isMouseOver(mouseX, mouseY)) {
			resetAdditionalTooltip();
			if (slotBeingChanged > -1) {
				saveTargetSlot();
			}
		}
	}

	private static void resetAdditionalTooltip() {
		if (!additionalTooltip.isEmpty()) {
			additionalTooltip = new ArrayList<>();
		}
	}

	@Override
	protected void onTabClose() {
		super.onTabClose();
		resetAdditionalTooltip();
	}

	private void saveTargetSlot() {
		getContainer().setTargetSlot(slotBeingChanged, targetSlotBeingChanged);
		slotBeingChanged = -1;
	}

	public static class Basic extends RefillUpgradeTab {
		public Basic(RefillUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen, int slotsInRow) {
			super(upgradeContainer, position, screen, slotsInRow, "refill");
		}
	}

	public static class Advanced extends RefillUpgradeTab {
		public Advanced(RefillUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen, int slotsInRow) {
			super(upgradeContainer, position, screen, slotsInRow, "advanced_refill");
		}
	}

	private class RefillFilterLogicControl extends FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>> {
		private RefillFilterLogicControl(StorageScreenBase<?> screen, int slotsInRow) {
			super(screen, new Position(RefillUpgradeTab.this.x + 3, RefillUpgradeTab.this.y + 24), RefillUpgradeTab.this.getContainer().getFilterLogicContainer(), slotsInRow);
		}

		@Override
		protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(mouseX, mouseY, partialTicks);
			if (!getContainer().allowsTargetSlotSelection()) {
				return;
			}
			renderTargetSlotAcronyms();
			updateTargetSlotTooltip(mouseX, mouseY);
		}

		private void updateTargetSlotTooltip(int mouseX, int mouseY) {
			if (!isMouseOver(mouseX, mouseY)) {
				return;
			}
			if (slotBeingChanged > -1) {
				updateTooltip(targetSlotBeingChanged);
			} else {
				TargetSlot targetSlot = getContainer().getTargetSlot(getSlot(mouseX, mouseY));
				if (additionalTooltip.isEmpty() || !additionalTooltip.get(0).contains(targetSlot.getDescription().getFormattedText())) {
					updateTooltip(targetSlot);
				}
			}
		}

		/** Over the slots' items, which are drawn after the tabs. */
		private void renderTargetSlotAcronyms() {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 300);
			for (Slot slot : getContainer().getSlots()) {
				if (slot instanceof FilterLogicContainer.FilterLogicSlot && !slot.getStack().isEmpty()) {
					int slotIndex = slot.getSlotIndex();
					TargetSlot targetSlot = slotBeingChanged == slotIndex ? targetSlotBeingChanged : getContainer().getTargetSlot(slotIndex);
					font.drawStringWithShadow(targetSlot.getAcronym().getFormattedText(),
							x + (slotIndex % slotsPerRow) * 18 + 10, y + slotsTopYOffset + (slotIndex / slotsPerRow) * 18 + 2, TARGET_SLOT_COLOR);
				}
			}
			GlStateManager.popMatrix();
		}

		private void updateTooltip(TargetSlot targetSlot) {
			resetAdditionalTooltip();
			additionalTooltip.add(TextFormatting.GRAY + SBPTranslationHelper.INSTANCE.translUpgrade("refill.target_slot.tooltip", targetSlot.getDescription()).getFormattedText());
			additionalTooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + SBPTranslationHelper.INSTANCE.translUpgrade("refill.scroll.tooltip").getFormattedText());
		}

		@Override
		public void render(int mouseX, int mouseY, float partialTicks) {
			super.render(mouseX, mouseY, partialTicks);
			if (slotBeingChanged > -1 && slotBeingChanged != getSlot(mouseX, mouseY)) {
				saveTargetSlot();
			}
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
			if (!getContainer().allowsTargetSlotSelection()) {
				return false;
			}
			int slot = getSlot(mouseX, mouseY);
			if (slot < 0 || slot >= getContainer().getFilterLogicContainer().getFilterSlots().size()) {
				return false;
			}
			if (slotBeingChanged == -1) {
				slotBeingChanged = slot;
				targetSlotBeingChanged = getContainer().getTargetSlot(slot);
			}
			targetSlotBeingChanged = delta > 0 ? targetSlotBeingChanged.next() : targetSlotBeingChanged.previous();
			return true;
		}

		private int getSlot(double mouseX, double mouseY) {
			return ((int) mouseX - x) / 18 + slotsPerRow * (((int) mouseY - y - slotsTopYOffset) / 18);
		}
	}
}
