package dev.feldsher.deconstructedbackpacks.upgrades.toolswapper;

import dev.feldsher.deconstructedbackpacks.client.KeybindHandler;
import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.utils.TranslationHelper;
import dev.feldsher.deconstructedcore.upgrades.IUpgradeCountLimitConfig;
import dev.feldsher.deconstructedcore.upgrades.UpgradeItemBase;
import dev.feldsher.deconstructedcore.upgrades.UpgradeType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Swaps into the player's hand the backpack's best tool for the block they start mining, and its best weapon for
 * what they attack. The advanced one has settings, and also swaps in on a key a tool that works on the block or
 * entity looked at, as a wrench, shears or a bucket, by the tool registry.
 */
public class ToolSwapperUpgradeItem extends UpgradeItemBase<ToolSwapperUpgradeWrapper> {
	private static final UpgradeType<ToolSwapperUpgradeWrapper> TYPE = new UpgradeType<>(ToolSwapperUpgradeWrapper::new);
	private static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = Collections.singletonList(
			new UpgradeConflictDefinition(ToolSwapperUpgradeItem.class::isInstance, 0, SBPTranslationHelper.INSTANCE.translError("add.tool_swapper_exists")));
	private final boolean hasSettingsTab;
	private final boolean swapToolOnKeyPress;
	private final IntSupplier filterSlotCount;

	public ToolSwapperUpgradeItem(boolean hasSettingsTab, boolean swapToolOnKeyPress, IntSupplier filterSlotCount, IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
		super(upgradeTypeLimitConfig);
		this.hasSettingsTab = hasSettingsTab;
		this.swapToolOnKeyPress = swapToolOnKeyPress;
		this.filterSlotCount = filterSlotCount;
	}

	@Override
	public UpgradeType<ToolSwapperUpgradeWrapper> getType() {
		return TYPE;
	}

	public boolean hasSettingsTab() {
		return hasSettingsTab;
	}

	public boolean shouldSwapToolOnKeyPress() {
		return swapToolOnKeyPress;
	}

	public int getFilterSlotCount() {
		return filterSlotCount.getAsInt();
	}

	@Override
	public List<UpgradeConflictDefinition> getUpgradeConflicts() {
		return UPGRADE_CONFLICT_DEFINITIONS;
	}

	/** The advanced one's tooltip names the tool swap key. */
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.addAll(TranslationHelper.INSTANCE.getTranslatedLines(getTranslationKey() + TranslationHelper.TOOLTIP_SUFFIX,
				new Object[] {KeybindHandler.TOOL_SWAP_KEYBIND.getDisplayName()}, TextFormatting.DARK_GRAY));
	}
}
