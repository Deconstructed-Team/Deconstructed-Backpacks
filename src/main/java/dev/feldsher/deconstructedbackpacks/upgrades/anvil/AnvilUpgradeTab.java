package dev.feldsher.deconstructedbackpacks.upgrades.anvil;

import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.StorageScreenBase;
import dev.feldsher.deconstructedcore.client.gui.UpgradeSettingsTab;
import dev.feldsher.deconstructedcore.client.gui.controls.TextBox;
import dev.feldsher.deconstructedcore.client.gui.utils.Dimension;
import dev.feldsher.deconstructedcore.client.gui.utils.GuiHelper;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.client.gui.utils.TextureBlitData;
import dev.feldsher.deconstructedcore.client.gui.utils.TranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.utils.UV;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class AnvilUpgradeTab extends UpgradeSettingsTab<AnvilUpgradeContainer> {
	private static final TextureBlitData EDIT_ITEM_NAME_BACKGROUND_DISABLED = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(28, 115), new Dimension(100, 16));
	private static final TextureBlitData EDIT_ITEM_NAME_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(28, 99), new Dimension(100, 16));
	private static final TextureBlitData PLUS_SIGN = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(113, 203), new Dimension(13, 13));
	private static final TextureBlitData ARROW = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(56, 221), new Dimension(14, 15));
	private static final TextureBlitData RED_CROSS = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(113, 216), new Dimension(15, 15));
	private static final int COST_COLOR = 0x80FF20;
	private static final int TOO_EXPENSIVE_COLOR = 0xFF6060;
	private static final int COST_BACKGROUND_COLOR = 0x4F000000;
	private final TextBox itemNameTextBox;

	public AnvilUpgradeTab(AnvilUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen) {
		super(upgradeContainer, position, screen, SBPTranslationHelper.INSTANCE.translUpgrade("anvil"),
				new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translUpgradeKey("anvil") + TranslationHelper.TOOLTIP_SUFFIX));
		openTabDimension = new Dimension(103, 92);
		itemNameTextBox = new TextBox(new Position(x + 5, y + 25), new Dimension(90, 14)) {
			@Override
			protected void renderBg(Minecraft minecraft, int mouseX, int mouseY) {
				super.renderBg(minecraft, mouseX, mouseY);
				TextureBlitData background = getContainer().getSlots().get(0).getHasStack() ? EDIT_ITEM_NAME_BACKGROUND : EDIT_ITEM_NAME_BACKGROUND_DISABLED;
				GuiHelper.blit(getX() - 2, getY() - 2, background, getWidth() + 4, getHeight() + 2);
			}
		};
		itemNameTextBox.setTextColor(-1);
		itemNameTextBox.setTextColorUneditable(-1);
		itemNameTextBox.setBordered(false);
		itemNameTextBox.setMaxLength(AnvilUpgradeContainer.MAX_ITEM_NAME_LENGTH);
		itemNameTextBox.setResponder(name -> getContainer().setItemName(name));
		itemNameTextBox.setValue(getContainer().getItemName());
		addHideableChild(itemNameTextBox);
		itemNameTextBox.setEditable(getContainer().getSlots().get(0).getHasStack());

		getContainer().setNameChangeListener(() -> {
			itemNameTextBox.setValue(getContainer().getItemName());
			itemNameTextBox.setEditable(getContainer().getSlots().get(0).getHasStack());
		});
	}

	@Override
	protected void renderBg(Minecraft minecraft, int mouseX, int mouseY) {
		super.renderBg(minecraft, mouseX, mouseY);
		if (getContainer().isOpen()) {
			for (int i = 0; i < 3; i++) {
				Slot slot = getContainer().getSlots().get(i);
				GuiHelper.renderSlotsBackground(slot.xPos + screen.getGuiLeft() - 1, slot.yPos + screen.getGuiTop() - 1, 1, 1);
			}
		}
	}

	@Override
	protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
		super.renderWidget(mouseX, mouseY, partialTicks);
		if (!isOpen) {
			return;
		}
		renderCost(x + 3, y + 62);

		Slot firstSlot = getContainer().getSlots().get(0);
		int inputSlotsY = firstSlot.yPos + screen.getGuiTop();
		int firstInputSlotX = firstSlot.xPos + screen.getGuiLeft();
		int secondInputSlotX = getContainer().getSlots().get(1).xPos + screen.getGuiLeft();
		Slot resultSlot = getContainer().getSlots().get(2);
		int resultSlotX = resultSlot.xPos + screen.getGuiLeft();

		GuiHelper.blit(firstInputSlotX + 18 + (secondInputSlotX - (firstInputSlotX + 18)) / 2 - PLUS_SIGN.getWidth() / 2 - 1, inputSlotsY + 2, PLUS_SIGN);
		int arrowX = secondInputSlotX + 18 + (resultSlotX - (secondInputSlotX + 18)) / 2 - ARROW.getWidth() / 2 - 1;
		int arrowY = inputSlotsY + 1;
		GuiHelper.blit(arrowX, arrowY, ARROW);
		if (firstSlot.getHasStack() && !resultSlot.getHasStack()) {
			GuiHelper.blit(arrowX, arrowY, RED_CROSS);
		}
	}

	@Override
	protected void moveSlotsToTab() {
		Slot firstInputSlot = getContainer().getSlots().get(0);
		firstInputSlot.xPos = x - screen.getGuiLeft() + 4;
		firstInputSlot.yPos = y + 42 - screen.getGuiTop() + 1;

		Slot secondInputSlot = getContainer().getSlots().get(1);
		secondInputSlot.xPos = x - screen.getGuiLeft() + getWidth() / 2 - 9;
		secondInputSlot.yPos = y + 42 - screen.getGuiTop() + 1;

		Slot resultSlot = getContainer().getSlots().get(2);
		resultSlot.xPos = x - screen.getGuiLeft() + getWidth() - 2 - 3 - 18;
		resultSlot.yPos = y + 42 - screen.getGuiTop() + 1;
	}

	/** As vanilla's anvil shows it: the levels it costs, red when the player lacks them, or that it's too expensive. */
	private void renderCost(int x, int y) {
		int cost = getContainer().getCost();
		if (cost <= 0) {
			return;
		}
		int color = COST_COLOR;
		String text;
		if (cost >= 40 && !minecraft.player.capabilities.isCreativeMode) {
			text = I18n.format("container.repair.expensive");
			color = TOO_EXPENSIVE_COLOR;
		} else if (!getContainer().getSlots().get(2).getHasStack()) {
			return;
		} else {
			text = I18n.format("container.repair.cost", cost);
			if (!getContainer().getSlots().get(2).canTakeStack(minecraft.player)) {
				color = TOO_EXPENSIVE_COLOR;
			}
		}
		int maxWidth = getWidth() - 9;
		List<String> lines = font.listFormattedStringToWidth(text, maxWidth);
		Gui.drawRect(x, y, x + maxWidth, y + lines.size() * 12, COST_BACKGROUND_COLOR);
		int yOffset = 0;
		for (String line : lines) {
			font.drawStringWithShadow(line, x + 2 + (float) (maxWidth - font.getStringWidth(line)) / 2, y + 2 + yOffset, color);
			yOffset += 12;
		}
	}
}
