package dev.feldsher.deconstructedbackpacks.init;

import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerRegistry;
import dev.feldsher.deconstructedcore.common.gui.UpgradeContainerType;
import dev.feldsher.deconstructedcore.upgrades.ContentsFilteredUpgradeContainer;
import dev.feldsher.deconstructedcore.upgrades.filter.FilterUpgradeContainer;
import dev.feldsher.deconstructedcore.upgrades.magnet.MagnetUpgradeContainer;
import dev.feldsher.deconstructedcore.upgrades.magnet.MagnetUpgradeWrapper;
import dev.feldsher.deconstructedcore.upgrades.pickup.PickupUpgradeWrapper;
import dev.feldsher.deconstructedcore.upgrades.voiding.VoidUpgradeContainer;
import dev.feldsher.deconstructedcore.upgrades.voiding.VoidUpgradeWrapper;
import net.minecraft.item.Item;

import java.util.Objects;

/** The containers behind the backpack upgrades' settings tabs. */
public class ModUpgradeContainers {
	private ModUpgradeContainers() {}

	public static final UpgradeContainerType<PickupUpgradeWrapper, ContentsFilteredUpgradeContainer<PickupUpgradeWrapper>> PICKUP_BASIC_TYPE = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);
	public static final UpgradeContainerType<PickupUpgradeWrapper, ContentsFilteredUpgradeContainer<PickupUpgradeWrapper>> PICKUP_ADVANCED_TYPE = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);

	public static final UpgradeContainerType<MagnetUpgradeWrapper, MagnetUpgradeContainer> MAGNET_BASIC_TYPE = new UpgradeContainerType<>(MagnetUpgradeContainer::new);
	public static final UpgradeContainerType<MagnetUpgradeWrapper, MagnetUpgradeContainer> MAGNET_ADVANCED_TYPE = new UpgradeContainerType<>(MagnetUpgradeContainer::new);
	public static final UpgradeContainerType<VoidUpgradeWrapper, VoidUpgradeContainer> VOID_BASIC_TYPE = new UpgradeContainerType<>(VoidUpgradeContainer::new);
	public static final UpgradeContainerType<VoidUpgradeWrapper, VoidUpgradeContainer> VOID_ADVANCED_TYPE = new UpgradeContainerType<>(VoidUpgradeContainer::new);

	public static void register() {
		register(ModItems.PICKUP_UPGRADE, PICKUP_BASIC_TYPE);
		register(ModItems.ADVANCED_PICKUP_UPGRADE, PICKUP_ADVANCED_TYPE);
		register(ModItems.FILTER_UPGRADE, FilterUpgradeContainer.BASIC_TYPE);
		register(ModItems.ADVANCED_FILTER_UPGRADE, FilterUpgradeContainer.ADVANCED_TYPE);
		register(ModItems.MAGNET_UPGRADE, MAGNET_BASIC_TYPE);
		register(ModItems.ADVANCED_MAGNET_UPGRADE, MAGNET_ADVANCED_TYPE);
		register(ModItems.VOID_UPGRADE, VOID_BASIC_TYPE);
		register(ModItems.ADVANCED_VOID_UPGRADE, VOID_ADVANCED_TYPE);
	}

	private static void register(Item upgrade, UpgradeContainerType<?, ?> type) {
		UpgradeContainerRegistry.register(Objects.requireNonNull(upgrade.getRegistryName()), type);
	}
}
