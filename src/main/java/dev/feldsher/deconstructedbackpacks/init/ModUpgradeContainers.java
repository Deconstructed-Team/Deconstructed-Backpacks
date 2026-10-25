package dev.feldsher.deconstructedbackpacks.init;

import dev.feldsher.deconstructedcore.upgrades.stonecutter.StonecutterUpgradeWrapper;
import dev.feldsher.deconstructedcore.upgrades.stonecutter.StonecutterUpgradeContainer;
import dev.feldsher.deconstructedcore.upgrades.crafting.CraftingUpgradeWrapper;
import dev.feldsher.deconstructedcore.upgrades.crafting.CraftingUpgradeContainer;
import dev.feldsher.deconstructedbackpacks.upgrades.inception.InceptionUpgradeWrapper;
import dev.feldsher.deconstructedbackpacks.upgrades.inception.InceptionUpgradeContainer;
import dev.feldsher.deconstructedbackpacks.upgrades.refill.RefillUpgradeWrapper;
import dev.feldsher.deconstructedbackpacks.upgrades.refill.RefillUpgradeContainer;
import dev.feldsher.deconstructedbackpacks.upgrades.toolswapper.ToolSwapperUpgradeWrapper;
import dev.feldsher.deconstructedbackpacks.upgrades.toolswapper.ToolSwapperUpgradeContainer;
import dev.feldsher.deconstructedcore.upgrades.compacting.CompactingUpgradeWrapper;
import dev.feldsher.deconstructedcore.upgrades.compacting.CompactingUpgradeContainer;
import dev.feldsher.deconstructedcore.upgrades.feeding.FeedingUpgradeWrapper;
import dev.feldsher.deconstructedcore.upgrades.feeding.FeedingUpgradeContainer;
import dev.feldsher.deconstructedbackpacks.upgrades.deposit.DepositUpgradeContainer;
import dev.feldsher.deconstructedbackpacks.upgrades.deposit.DepositUpgradeWrapper;
import dev.feldsher.deconstructedbackpacks.upgrades.restock.RestockUpgradeWrapper;
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

	public static final UpgradeContainerType<CompactingUpgradeWrapper, CompactingUpgradeContainer> COMPACTING_BASIC_TYPE = new UpgradeContainerType<>(CompactingUpgradeContainer::new);
	public static final UpgradeContainerType<CompactingUpgradeWrapper, CompactingUpgradeContainer> COMPACTING_ADVANCED_TYPE = new UpgradeContainerType<>(CompactingUpgradeContainer::new);
	public static final UpgradeContainerType<FeedingUpgradeWrapper, FeedingUpgradeContainer> FEEDING_BASIC_TYPE = new UpgradeContainerType<>(FeedingUpgradeContainer::new);
	public static final UpgradeContainerType<FeedingUpgradeWrapper, FeedingUpgradeContainer> FEEDING_ADVANCED_TYPE = new UpgradeContainerType<>(FeedingUpgradeContainer::new);
	public static final UpgradeContainerType<RestockUpgradeWrapper, ContentsFilteredUpgradeContainer<RestockUpgradeWrapper>> RESTOCK_BASIC_TYPE = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);
	public static final UpgradeContainerType<RestockUpgradeWrapper, ContentsFilteredUpgradeContainer<RestockUpgradeWrapper>> RESTOCK_ADVANCED_TYPE = new UpgradeContainerType<>(ContentsFilteredUpgradeContainer::new);
	public static final UpgradeContainerType<DepositUpgradeWrapper, DepositUpgradeContainer> DEPOSIT_BASIC_TYPE = new UpgradeContainerType<>(DepositUpgradeContainer::new);
	public static final UpgradeContainerType<DepositUpgradeWrapper, DepositUpgradeContainer> DEPOSIT_ADVANCED_TYPE = new UpgradeContainerType<>(DepositUpgradeContainer::new);

	public static final UpgradeContainerType<CraftingUpgradeWrapper, CraftingUpgradeContainer> CRAFTING_TYPE = new UpgradeContainerType<>(CraftingUpgradeContainer::new);
	public static final UpgradeContainerType<StonecutterUpgradeWrapper, StonecutterUpgradeContainer> STONECUTTER_TYPE = new UpgradeContainerType<>(StonecutterUpgradeContainer::new);
	public static final UpgradeContainerType<InceptionUpgradeWrapper, InceptionUpgradeContainer> INCEPTION_TYPE = new UpgradeContainerType<>(InceptionUpgradeContainer::new);
	public static final UpgradeContainerType<RefillUpgradeWrapper, RefillUpgradeContainer> REFILL_BASIC_TYPE = new UpgradeContainerType<>(RefillUpgradeContainer::new);
	public static final UpgradeContainerType<RefillUpgradeWrapper, RefillUpgradeContainer> REFILL_ADVANCED_TYPE = new UpgradeContainerType<>(RefillUpgradeContainer::new);
	public static final UpgradeContainerType<ToolSwapperUpgradeWrapper, ToolSwapperUpgradeContainer> TOOL_SWAPPER_TYPE = new UpgradeContainerType<>(ToolSwapperUpgradeContainer::new);

	public static final UpgradeContainerType<MagnetUpgradeWrapper, MagnetUpgradeContainer> MAGNET_BASIC_TYPE = new UpgradeContainerType<>(MagnetUpgradeContainer::new);
	public static final UpgradeContainerType<MagnetUpgradeWrapper, MagnetUpgradeContainer> MAGNET_ADVANCED_TYPE = new UpgradeContainerType<>(MagnetUpgradeContainer::new);
	public static final UpgradeContainerType<VoidUpgradeWrapper, VoidUpgradeContainer> VOID_BASIC_TYPE = new UpgradeContainerType<>(VoidUpgradeContainer::new);
	public static final UpgradeContainerType<VoidUpgradeWrapper, VoidUpgradeContainer> VOID_ADVANCED_TYPE = new UpgradeContainerType<>(VoidUpgradeContainer::new);

	public static void register() {
		register(ModItems.PICKUP_UPGRADE, PICKUP_BASIC_TYPE);
		register(ModItems.ADVANCED_PICKUP_UPGRADE, PICKUP_ADVANCED_TYPE);
		register(ModItems.FILTER_UPGRADE, FilterUpgradeContainer.BASIC_TYPE);
		register(ModItems.ADVANCED_FILTER_UPGRADE, FilterUpgradeContainer.ADVANCED_TYPE);
		register(ModItems.COMPACTING_UPGRADE, COMPACTING_BASIC_TYPE);
		register(ModItems.ADVANCED_COMPACTING_UPGRADE, COMPACTING_ADVANCED_TYPE);
		register(ModItems.FEEDING_UPGRADE, FEEDING_BASIC_TYPE);
		register(ModItems.ADVANCED_FEEDING_UPGRADE, FEEDING_ADVANCED_TYPE);
		register(ModItems.RESTOCK_UPGRADE, RESTOCK_BASIC_TYPE);
		register(ModItems.ADVANCED_RESTOCK_UPGRADE, RESTOCK_ADVANCED_TYPE);
		register(ModItems.DEPOSIT_UPGRADE, DEPOSIT_BASIC_TYPE);
		register(ModItems.ADVANCED_DEPOSIT_UPGRADE, DEPOSIT_ADVANCED_TYPE);
		register(ModItems.CRAFTING_UPGRADE, CRAFTING_TYPE);
		register(ModItems.STONECUTTER_UPGRADE, STONECUTTER_TYPE);
		register(ModItems.INCEPTION_UPGRADE, INCEPTION_TYPE);
		register(ModItems.REFILL_UPGRADE, REFILL_BASIC_TYPE);
		register(ModItems.ADVANCED_REFILL_UPGRADE, REFILL_ADVANCED_TYPE);
		register(ModItems.ADVANCED_TOOL_SWAPPER_UPGRADE, TOOL_SWAPPER_TYPE);
		register(ModItems.MAGNET_UPGRADE, MAGNET_BASIC_TYPE);
		register(ModItems.ADVANCED_MAGNET_UPGRADE, MAGNET_ADVANCED_TYPE);
		register(ModItems.VOID_UPGRADE, VOID_BASIC_TYPE);
		register(ModItems.ADVANCED_VOID_UPGRADE, VOID_ADVANCED_TYPE);
	}

	private static void register(Item upgrade, UpgradeContainerType<?, ?> type) {
		UpgradeContainerRegistry.register(Objects.requireNonNull(upgrade.getRegistryName()), type);
	}
}
