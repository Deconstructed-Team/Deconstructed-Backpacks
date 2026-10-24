package dev.feldsher.deconstructedbackpacks.settings;

import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.SettingsScreen;
import dev.feldsher.deconstructedcore.client.gui.controls.ImageButton;
import dev.feldsher.deconstructedcore.client.gui.utils.Dimension;
import dev.feldsher.deconstructedcore.client.gui.utils.GuiHelper;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.client.gui.utils.TextureBlitData;
import dev.feldsher.deconstructedcore.client.gui.utils.UV;
import dev.feldsher.deconstructedcore.settings.main.MainSettingsContainer;
import dev.feldsher.deconstructedcore.settings.main.MainSettingsTab;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Arrays;
import java.util.List;

public class BackpackMainSettingsTab extends MainSettingsTab<MainSettingsContainer> {
	private static final TextureBlitData ICON = new TextureBlitData(GuiHelper.ICONS, Dimension.SQUARE_256, new UV(64, 48), Dimension.SQUARE_16);
	private static final List<ITextComponent> BACKPACK_CONTEXT_TOOLTIP = Arrays.asList(
			new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translSettingsButton("context_backpack.tooltip")),
			gray(new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translSettingsButton("context_backpack.tooltip_detail")))
	);

	public BackpackMainSettingsTab(MainSettingsContainer container, Position position, SettingsScreen screen) {
		super(container, position, screen, BACKPACK_CONTEXT_TOOLTIP, new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translSettingsButton("context_backpack")),
				SBPTranslationHelper.INSTANCE.translSettings("backpack"), SBPTranslationHelper.INSTANCE.translSettingsTooltip("backpack"),
				onTabIconClicked -> new ImageButton(new Position(position.x() + 1, position.y() + 4), Dimension.SQUARE_16, ICON, onTabIconClicked));
	}
}
