package dev.feldsher.deconstructedbackpacks.settings;

import dev.feldsher.deconstructedcore.client.gui.controls.ToggleButton;
import dev.feldsher.deconstructedcore.client.gui.controls.ButtonDefinitions;
import dev.feldsher.deconstructedcore.client.gui.controls.ButtonDefinition;
import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.SettingsScreen;
import dev.feldsher.deconstructedcore.client.gui.controls.ImageButton;
import dev.feldsher.deconstructedcore.client.gui.utils.Dimension;
import dev.feldsher.deconstructedcore.client.gui.utils.GuiHelper;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.client.gui.utils.TextureBlitData;
import dev.feldsher.deconstructedcore.client.gui.utils.UV;
import dev.feldsher.deconstructedcore.settings.main.MainSettingsTab;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Arrays;
import java.util.List;

/** The backpack's main settings, and, as the config allows, whether another player may open it while it's worn. */
public class BackpackMainSettingsTab extends MainSettingsTab<BackpackMainSettingsContainer> {
	private static final TextureBlitData ICON = new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(64, 48), Dimension.SQUARE_16);
	private static final List<ITextComponent> BACKPACK_CONTEXT_TOOLTIP = Arrays.asList(
			new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translSettingsButton("context_backpack.tooltip")),
			gray(new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translSettingsButton("context_backpack.tooltip_detail")))
	);

	private static final ButtonDefinition.Toggle<Boolean> ANOTHER_PLAYER_CAN_OPEN = ButtonDefinitions.createToggleButtonDefinition(
			ButtonDefinitions.getBooleanStateData(
					GuiHelper.getButtonStateData(new UV(176, 32), Dimension.SQUARE_16, new Position(1, 1), Arrays.asList(
							new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translSettingsButton("another_player_can_open.on")),
							gray(new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translSettingsButton("another_player_can_open.on.tooltip"))))),
					GuiHelper.getButtonStateData(new UV(192, 32), Dimension.SQUARE_16, new Position(1, 1), Arrays.asList(
							new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translSettingsButton("another_player_can_open.off")),
							gray(new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translSettingsButton("another_player_can_open.off.tooltip")))))
			));

	public BackpackMainSettingsTab(BackpackMainSettingsContainer container, Position position, SettingsScreen screen) {
		super(container, position, screen, BACKPACK_CONTEXT_TOOLTIP, new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translSettingsButton("context_backpack")),
				SBPTranslationHelper.INSTANCE.translSettings("backpack"), SBPTranslationHelper.INSTANCE.translSettingsTooltip("backpack"),
				onTabIconClicked -> new ImageButton(new Position(position.x() + 1, position.y() + 4), Dimension.SQUARE_16, ICON, onTabIconClicked));
		if (Config.SERVER.allowOpeningOtherPlayerBackpacks) {
			addHideableChild(new ToggleButton<>(new Position(x + 57, y + 46), ANOTHER_PLAYER_CAN_OPEN, button -> container.toggleAnotherPlayerCanOpen(), container::canAnotherPlayerOpen));
		}
	}
}
