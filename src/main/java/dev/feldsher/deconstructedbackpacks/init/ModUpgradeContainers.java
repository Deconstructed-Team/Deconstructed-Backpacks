package dev.feldsher.deconstructedbackpacks.init;

import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerRegistry;
import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerType;
import dev.feldsher.deconstructedcore.upgrades.ContentsFilteredUpgradeContainer;
import dev.feldsher.deconstructedcore.upgrades.pickup.PickupUpgradeWrapper;

import java.util.Objects;

/** The containers behind the backpack upgrades' settings tabs. */
public class ModUpgradeContainers {
	private ModUpgradeContainers() {}

	public static final UpgradeContainerType<PickupUpgradeWrapper, ContentsFilteredUpgradeContainer<PickupUpgradeWrapper>> PICKUP_BASIC_TYPE = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);
	public static final UpgradeContainerType<PickupUpgradeWrapper, ContentsFilteredUpgradeContainer<PickupUpgradeWrapper>> PICKUP_ADVANCED_TYPE = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);

	public static void register() {
		UpgradeContainerRegistry.register(Objects.requireNonNull(ModItems.PICKUP_UPGRADE.getRegistryName()), PICKUP_BASIC_TYPE);
		UpgradeContainerRegistry.register(Objects.requireNonNull(ModItems.ADVANCED_PICKUP_UPGRADE.getRegistryName()), PICKUP_ADVANCED_TYPE);
	}
}
