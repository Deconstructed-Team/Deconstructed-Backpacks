package dev.feldsher.deconstructedbackpacks.init;

import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedcore.upgrades.pickup.PickupUpgradeItem;
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
	public static final BackpackItem IRON_BACKPACK = new BackpackItem("iron_backpack",
			() -> Config.SERVER.ironBackpack.inventorySlotCount, () -> Config.SERVER.ironBackpack.upgradeSlotCount);
	public static final BackpackItem GOLD_BACKPACK = new BackpackItem("gold_backpack",
			() -> Config.SERVER.goldBackpack.inventorySlotCount, () -> Config.SERVER.goldBackpack.upgradeSlotCount);
	public static final BackpackItem DIAMOND_BACKPACK = new BackpackItem("diamond_backpack",
			() -> Config.SERVER.diamondBackpack.inventorySlotCount, () -> Config.SERVER.diamondBackpack.upgradeSlotCount);

	public static final List<BackpackItem> BACKPACKS = Collections.unmodifiableList(Arrays.asList(BACKPACK, IRON_BACKPACK, GOLD_BACKPACK, DIAMOND_BACKPACK));

	public static final Item UPGRADE_BASE = item(new Item(), "upgrade_base");

	public static final PickupUpgradeItem PICKUP_UPGRADE = item(new PickupUpgradeItem(() -> Config.SERVER.pickupUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "pickup_upgrade");
	public static final PickupUpgradeItem ADVANCED_PICKUP_UPGRADE = item(new PickupUpgradeItem(() -> Config.SERVER.advancedPickupUpgrade.filterSlots,
			Config.SERVER.maxUpgradesPerStorageConfig), "advanced_pickup_upgrade");

	/** What 1.21 tags as backpack upgrades: the only items a backpack's upgrade slots take. */
	public static final List<Item> BACKPACK_UPGRADES = Collections.unmodifiableList(Arrays.asList(PICKUP_UPGRADE, ADVANCED_PICKUP_UPGRADE));

	private static <T extends Item> T item(T item, String name) {
		item.setRegistryName(DeconstructedBackpacks.getRL(name));
		item.setTranslationKey(DeconstructedBackpacks.MOD_ID + "." + name);
		item.setCreativeTab(CREATIVE_TAB);
		return item;
	}

	public static List<Item> getAllItems() {
		List<Item> items = new ArrayList<>(BACKPACKS);
		items.add(UPGRADE_BASE);
		items.addAll(BACKPACK_UPGRADES);
		return items;
	}

	public static boolean isBackpackUpgrade(Item item) {
		return BACKPACK_UPGRADES.contains(item);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		getAllItems().forEach(event.getRegistry()::register);
	}
}
