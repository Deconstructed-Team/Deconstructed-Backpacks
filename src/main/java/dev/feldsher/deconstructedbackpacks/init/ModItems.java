package dev.feldsher.deconstructedbackpacks.init;

import dev.feldsher.deconstructedbackpacks.upgrades.inception.InceptionUpgradeItem;
import dev.feldsher.deconstructedbackpacks.upgrades.refill.RefillUpgradeItem;
import dev.feldsher.deconstructedbackpacks.upgrades.toolswapper.ToolSwapperUpgradeItem;
import dev.feldsher.deconstructedcore.upgrades.infinity.InfinityUpgradeItem;
import dev.feldsher.deconstructedcore.upgrades.compacting.CompactingUpgradeItem;
import dev.feldsher.deconstructedcore.upgrades.feeding.FeedingUpgradeItem;
import dev.feldsher.deconstructedbackpacks.upgrades.everlasting.EverlastingUpgradeItem;
import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedcore.upgrades.filter.FilterUpgradeItem;
import dev.feldsher.deconstructedcore.upgrades.magnet.MagnetUpgradeItem;
import dev.feldsher.deconstructedcore.upgrades.pickup.PickupUpgradeItem;
import dev.feldsher.deconstructedbackpacks.upgrades.deposit.DepositUpgradeItem;
import dev.feldsher.deconstructedbackpacks.upgrades.restock.RestockUpgradeItem;
import dev.feldsher.deconstructedcore.upgrades.stack.StackUpgradeItem;
import dev.feldsher.deconstructedcore.upgrades.voiding.VoidUpgradeItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID)
public class ModItems {
	private ModItems() {}

	public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(DeconstructedBackpacks.MOD_ID) {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(BACKPACK);
		}
	};

	// No copper or netherite in 1.12: the iron backpack is made from the leather one
	public static final BackpackItem BACKPACK = new BackpackItem("backpack",
			() -> Config.SERVER.leatherBackpack.inventorySlotCount, () -> Config.SERVER.leatherBackpack.upgradeSlotCount);
	public static final BackpackItem COPPER_BACKPACK = new BackpackItem("copper_backpack",
			() -> Config.SERVER.copperBackpack.inventorySlotCount, () -> Config.SERVER.copperBackpack.upgradeSlotCount);
	public static final BackpackItem IRON_BACKPACK = new BackpackItem("iron_backpack",
			() -> Config.SERVER.ironBackpack.inventorySlotCount, () -> Config.SERVER.ironBackpack.upgradeSlotCount);
	public static final BackpackItem GOLD_BACKPACK = new BackpackItem("gold_backpack",
			() -> Config.SERVER.goldBackpack.inventorySlotCount, () -> Config.SERVER.goldBackpack.upgradeSlotCount);
	public static final BackpackItem DIAMOND_BACKPACK = new BackpackItem("diamond_backpack",
			() -> Config.SERVER.diamondBackpack.inventorySlotCount, () -> Config.SERVER.diamondBackpack.upgradeSlotCount);

	public static final BackpackItem NETHERITE_BACKPACK = new BackpackItem("netherite_backpack",
			() -> Config.SERVER.netheriteBackpack.inventorySlotCount, () -> Config.SERVER.netheriteBackpack.upgradeSlotCount).setFireResistant();

	/** The backpack tiers the config enables. */
	public static List<BackpackItem> getBackpacks() {
		List<BackpackItem> backpacks = new ArrayList<>(Arrays.asList(BACKPACK, IRON_BACKPACK, GOLD_BACKPACK, DIAMOND_BACKPACK));
		if (Config.SERVER.copperTierEnabled) {
			backpacks.add(1, COPPER_BACKPACK);
		}
		if (Config.SERVER.netheriteTierEnabled) {
			backpacks.add(NETHERITE_BACKPACK);
		}
		return backpacks;
	}

	public static final Item UPGRADE_BASE = item(new Item(), "upgrade_base");

	public static final PickupUpgradeItem PICKUP_UPGRADE = item(new PickupUpgradeItem(() -> Config.SERVER.pickupUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "pickup_upgrade");
	public static final PickupUpgradeItem ADVANCED_PICKUP_UPGRADE = item(new PickupUpgradeItem(() -> Config.SERVER.advancedPickupUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "advanced_pickup_upgrade");

	public static final CompactingUpgradeItem COMPACTING_UPGRADE = item(new CompactingUpgradeItem(false, () -> Config.SERVER.compactingUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "compacting_upgrade");
	public static final CompactingUpgradeItem ADVANCED_COMPACTING_UPGRADE = item(new CompactingUpgradeItem(true, () -> Config.SERVER.advancedCompactingUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "advanced_compacting_upgrade");
	public static final FeedingUpgradeItem FEEDING_UPGRADE = item(new FeedingUpgradeItem(() -> Config.SERVER.feedingUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "feeding_upgrade");
	public static final FeedingUpgradeItem ADVANCED_FEEDING_UPGRADE = item(new FeedingUpgradeItem(() -> Config.SERVER.advancedFeedingUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "advanced_feeding_upgrade");
	public static final RestockUpgradeItem RESTOCK_UPGRADE = item(new RestockUpgradeItem(() -> Config.SERVER.restockUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "restock_upgrade");
	public static final RestockUpgradeItem ADVANCED_RESTOCK_UPGRADE = item(new RestockUpgradeItem(() -> Config.SERVER.advancedRestockUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "advanced_restock_upgrade");
	public static final DepositUpgradeItem DEPOSIT_UPGRADE = item(new DepositUpgradeItem(() -> Config.SERVER.depositUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "deposit_upgrade");
	public static final DepositUpgradeItem ADVANCED_DEPOSIT_UPGRADE = item(new DepositUpgradeItem(() -> Config.SERVER.advancedDepositUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "advanced_deposit_upgrade");

	public static final InceptionUpgradeItem INCEPTION_UPGRADE = item(new InceptionUpgradeItem(Config.SERVER.maxUpgradesPerStorageConfig), "inception_upgrade");
	public static final RefillUpgradeItem REFILL_UPGRADE = item(new RefillUpgradeItem(() -> Config.SERVER.refillUpgrade.filterSlots, false, false,
			Config.SERVER.maxUpgradesPerStorageConfig), "refill_upgrade");
	public static final RefillUpgradeItem ADVANCED_REFILL_UPGRADE = item(new RefillUpgradeItem(() -> Config.SERVER.advancedRefillUpgrade.filterSlots, true, true,
			Config.SERVER.maxUpgradesPerStorageConfig), "advanced_refill_upgrade");
	public static final ToolSwapperUpgradeItem TOOL_SWAPPER_UPGRADE = item(new ToolSwapperUpgradeItem(false, false, () -> Config.SERVER.toolSwapperUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "tool_swapper_upgrade");
	public static final ToolSwapperUpgradeItem ADVANCED_TOOL_SWAPPER_UPGRADE = item(new ToolSwapperUpgradeItem(true, true, () -> Config.SERVER.toolSwapperUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "advanced_tool_swapper_upgrade");

	public static final EverlastingUpgradeItem EVERLASTING_UPGRADE = item(new EverlastingUpgradeItem(Config.SERVER.maxUpgradesPerStorageConfig), "everlasting_upgrade");

	public static final InfinityUpgradeItem INFINITY_UPGRADE = item(new InfinityUpgradeItem(Config.SERVER.maxUpgradesPerStorageConfig, true), "infinity_upgrade");
	public static final InfinityUpgradeItem SURVIVAL_INFINITY_UPGRADE = item(new InfinityUpgradeItem(Config.SERVER.maxUpgradesPerStorageConfig, false), "survival_infinity_upgrade");

	public static final FilterUpgradeItem FILTER_UPGRADE = item(new FilterUpgradeItem(() -> Config.SERVER.filterUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "filter_upgrade");
	public static final FilterUpgradeItem ADVANCED_FILTER_UPGRADE = item(new FilterUpgradeItem(() -> Config.SERVER.advancedFilterUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "advanced_filter_upgrade");
	public static final MagnetUpgradeItem MAGNET_UPGRADE = item(new MagnetUpgradeItem(() -> Config.SERVER.magnetUpgrade.magnetRange,
			() -> Config.SERVER.magnetUpgrade.filterSlots, Config.SERVER.maxUpgradesPerStorageConfig), "magnet_upgrade");
	public static final MagnetUpgradeItem ADVANCED_MAGNET_UPGRADE = item(new MagnetUpgradeItem(() -> Config.SERVER.advancedMagnetUpgrade.magnetRange,
			() -> Config.SERVER.advancedMagnetUpgrade.filterSlots, Config.SERVER.maxUpgradesPerStorageConfig), "advanced_magnet_upgrade");
	public static final VoidUpgradeItem VOID_UPGRADE = item(new VoidUpgradeItem(() -> Config.SERVER.voidUpgrade.filterSlots,
			() -> Config.SERVER.voidUpgrade.voidAnythingEnabled, Config.SERVER.maxUpgradesPerStorageConfig), "void_upgrade");
	public static final VoidUpgradeItem ADVANCED_VOID_UPGRADE = item(new VoidUpgradeItem(() -> Config.SERVER.advancedVoidUpgrade.filterSlots,
			() -> Config.SERVER.advancedVoidUpgrade.voidAnythingEnabled, Config.SERVER.maxUpgradesPerStorageConfig), "advanced_void_upgrade");

	// With the copper tier only, as 1.21 makes it from copper blocks
	public static final StackUpgradeItem STACK_UPGRADE_STARTER_TIER = item(new StackUpgradeItem(1.5, Config.SERVER.maxUpgradesPerStorageConfig), "stack_upgrade_starter_tier");
	public static final StackUpgradeItem STACK_UPGRADE_TIER_1 = item(new StackUpgradeItem(2, Config.SERVER.maxUpgradesPerStorageConfig), "stack_upgrade_tier_1");
	public static final StackUpgradeItem STACK_UPGRADE_TIER_2 = item(new StackUpgradeItem(4, Config.SERVER.maxUpgradesPerStorageConfig), "stack_upgrade_tier_2");
	public static final StackUpgradeItem STACK_UPGRADE_TIER_3 = item(new StackUpgradeItem(8, Config.SERVER.maxUpgradesPerStorageConfig), "stack_upgrade_tier_3");
	public static final StackUpgradeItem STACK_UPGRADE_TIER_4 = item(new StackUpgradeItem(16, Config.SERVER.maxUpgradesPerStorageConfig), "stack_upgrade_tier_4");
	public static final StackUpgradeItem STACK_DOWNGRADE_TIER_1 = item(new StackUpgradeItem(0.125, Config.SERVER.maxUpgradesPerStorageConfig), "stack_downgrade_tier_1");
	public static final StackUpgradeItem STACK_DOWNGRADE_TIER_2 = item(new StackUpgradeItem(0.0625, Config.SERVER.maxUpgradesPerStorageConfig), "stack_downgrade_tier_2");
	public static final StackUpgradeItem STACK_DOWNGRADE_TIER_3 = item(new StackUpgradeItem(0.03125, Config.SERVER.maxUpgradesPerStorageConfig), "stack_downgrade_tier_3");
	public static final StackUpgradeItem STACK_UPGRADE_OMEGA_TIER = item(new StackUpgradeItem(Integer.MAX_VALUE, Config.SERVER.maxUpgradesPerStorageConfig), "stack_upgrade_omega_tier");

	/** What 1.21 tags as backpack upgrades: the only items a backpack's upgrade slots take. */
	public static final List<Item> BACKPACK_UPGRADES = Collections.unmodifiableList(Arrays.asList(PICKUP_UPGRADE, ADVANCED_PICKUP_UPGRADE,
			FILTER_UPGRADE, ADVANCED_FILTER_UPGRADE, FEEDING_UPGRADE, ADVANCED_FEEDING_UPGRADE, COMPACTING_UPGRADE, ADVANCED_COMPACTING_UPGRADE, RESTOCK_UPGRADE, ADVANCED_RESTOCK_UPGRADE, DEPOSIT_UPGRADE, ADVANCED_DEPOSIT_UPGRADE, REFILL_UPGRADE, ADVANCED_REFILL_UPGRADE, INCEPTION_UPGRADE, TOOL_SWAPPER_UPGRADE, ADVANCED_TOOL_SWAPPER_UPGRADE, EVERLASTING_UPGRADE, INFINITY_UPGRADE, SURVIVAL_INFINITY_UPGRADE, MAGNET_UPGRADE, ADVANCED_MAGNET_UPGRADE, VOID_UPGRADE, ADVANCED_VOID_UPGRADE,
			STACK_UPGRADE_TIER_1, STACK_UPGRADE_TIER_2, STACK_UPGRADE_TIER_3, STACK_UPGRADE_TIER_4, STACK_UPGRADE_OMEGA_TIER,
			STACK_DOWNGRADE_TIER_1, STACK_DOWNGRADE_TIER_2, STACK_DOWNGRADE_TIER_3));

	private static <T extends Item> T item(T item, String name) {
		item.setRegistryName(DeconstructedBackpacks.getRL(name));
		item.setTranslationKey(DeconstructedBackpacks.MOD_ID + "." + name);
		item.setCreativeTab(CREATIVE_TAB);
		return item;
	}

	public static List<Item> getAllItems() {
		List<Item> items = new ArrayList<>(getBackpacks());
		items.add(UPGRADE_BASE);
		items.addAll(BACKPACK_UPGRADES);
		if (Config.SERVER.copperTierEnabled) {
			items.add(STACK_UPGRADE_STARTER_TIER);
		}
		return items;
	}

	public static boolean isBackpackUpgrade(Item item) {
		return BACKPACK_UPGRADES.contains(item) || (item == STACK_UPGRADE_STARTER_TIER && Config.SERVER.copperTierEnabled);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		getAllItems().forEach(event.getRegistry()::register);
	}
}
