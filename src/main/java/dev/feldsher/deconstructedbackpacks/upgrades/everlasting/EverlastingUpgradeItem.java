package dev.feldsher.deconstructedbackpacks.upgrades.everlasting;

import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.upgrades.IUpgradeCountLimitConfig;
import dev.feldsher.deconstructedcore.upgrades.UpgradeItemBase;
import dev.feldsher.deconstructedcore.upgrades.UpgradeType;
import dev.feldsher.deconstructedcore.upgrades.UpgradeWrapperBase;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Makes the backpack indestructible: dropped it doesn't burn, despawn, blow up or fall out of the world, and placed it
 * resists explosions as bedrock does. It has nothing to set and can't be switched off.
 */
public class EverlastingUpgradeItem extends UpgradeItemBase<EverlastingUpgradeItem.Wrapper> {
	public static final UpgradeType<Wrapper> TYPE = new UpgradeType<>(Wrapper::new);
	private static final List<UpgradeConflictDefinition> UPGRADE_CONFLICT_DEFINITIONS = Collections.singletonList(
			new UpgradeConflictDefinition(EverlastingUpgradeItem.class::isInstance, 0, SBPTranslationHelper.INSTANCE.translError("add.everlasting_exists")));

	public EverlastingUpgradeItem(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
		super(upgradeTypeLimitConfig);
	}

	@Override
	public UpgradeType<Wrapper> getType() {
		return TYPE;
	}

	@Override
	public List<UpgradeConflictDefinition> getUpgradeConflicts() {
		return UPGRADE_CONFLICT_DEFINITIONS;
	}

	public static class Wrapper extends UpgradeWrapperBase<Wrapper, EverlastingUpgradeItem> {
		public Wrapper(IStorageWrapper backpackWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
			super(backpackWrapper, upgrade, upgradeSaveHandler);
		}

		@Override
		public boolean hideSettingsTab() {
			return true;
		}

		@Override
		public boolean canBeDisabled() {
			return false;
		}
	}
}
