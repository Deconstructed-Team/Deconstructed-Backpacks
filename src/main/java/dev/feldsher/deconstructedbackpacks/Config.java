package dev.feldsher.deconstructedbackpacks;

import dev.feldsher.deconstructedcore.upgrades.battery.IBatteryUpgradeConfig;
import dev.feldsher.deconstructedcore.upgrades.xppump.IXpPumpUpgradeConfig;
import dev.feldsher.deconstructedcore.upgrades.pump.IPumpUpgradeConfig;
import dev.feldsher.deconstructedcore.upgrades.tank.ITankUpgradeConfig;
import dev.feldsher.deconstructedcore.upgrades.cooking.IAutoCookingUpgradeConfig;
import dev.feldsher.deconstructedcore.upgrades.cooking.ICookingUpgradeConfig;
import dev.feldsher.deconstructedcore.upgrades.IUpgradeCountLimitConfig;
import dev.feldsher.deconstructedcore.upgrades.UpgradeGroup;
import dev.feldsher.deconstructedcore.upgrades.stack.StackUpgradeConfig;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@net.minecraftforge.common.config.Config(modid = DeconstructedBackpacks.MOD_ID)
@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID)
public class Config {
	@net.minecraftforge.common.config.Config.Comment("Server Settings")
	@net.minecraftforge.common.config.Config.Name("server")
	public static final Server SERVER = new Server();

	private Config() {}

	public static class Server {
		@net.minecraftforge.common.config.Config.Comment("List of items that are not allowed to be put in backpacks - e.g. \"minecraft:shulker_box\"")
		public String[] disallowedItems = new String[0];

		@net.minecraftforge.common.config.Config.Comment("Whether the copper backpack and the starter tier stack upgrade exist. Their recipes take copper from the ore dictionary (ingotCopper, blockCopper); in a pack with none, they're left without recipes.")
		@net.minecraftforge.common.config.Config.RequiresMcRestart
		public boolean copperTierEnabled = false;
		@net.minecraftforge.common.config.Config.Comment("Whether the netherite backpack exists. Its recipe takes netherite from the ore dictionary (ingotNetherite, as Future MC and Unseen's Nether Backport register); in a pack with none, it's left without a recipe.")
		@net.minecraftforge.common.config.Config.RequiresMcRestart
		public boolean netheriteTierEnabled = false;
		@net.minecraftforge.common.config.Config.Comment("With Future MC and its smithing table, the netherite backpack is made there, as in 1.21, instead of on the crafting table. False keeps it on the crafting table.")
		@net.minecraftforge.common.config.Config.RequiresMcRestart
		public boolean futureMcSmithingTable = true;

		@net.minecraftforge.common.config.Config.Comment("Leather Backpack Settings")
		public final BackpackConfig leatherBackpack = new BackpackConfig(27, 1);
		@net.minecraftforge.common.config.Config.Comment("Copper Backpack Settings")
		public final BackpackConfig copperBackpack = new BackpackConfig(45, 1);
		@net.minecraftforge.common.config.Config.Comment("Iron Backpack Settings")
		public final BackpackConfig ironBackpack = new BackpackConfig(54, 2);
		@net.minecraftforge.common.config.Config.Comment("Gold Backpack Settings")
		public final BackpackConfig goldBackpack = new BackpackConfig(81, 3);
		@net.minecraftforge.common.config.Config.Comment("Diamond Backpack Settings")
		public final BackpackConfig diamondBackpack = new BackpackConfig(108, 5);
		@net.minecraftforge.common.config.Config.Comment("Netherite Backpack Settings")
		public final BackpackConfig netheriteBackpack = new BackpackConfig(120, 7);

		@net.minecraftforge.common.config.Config.Comment("Pickup Upgrade Settings")
		public final FilteredUpgradeConfig pickupUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Pickup Upgrade Settings")
		public final FilteredUpgradeConfig advancedPickupUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Filter Upgrade Settings")
		public final FilteredUpgradeConfig filterUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Filter Upgrade Settings")
		public final FilteredUpgradeConfig advancedFilterUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Compacting Upgrade Settings")
		public final FilteredUpgradeConfig compactingUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Compacting Upgrade Settings")
		public final FilteredUpgradeConfig advancedCompactingUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Feeding Upgrade Settings")
		public final FilteredUpgradeConfig feedingUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Feeding Upgrade Settings")
		public final FilteredUpgradeConfig advancedFeedingUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Restock Upgrade Settings")
		public final FilteredUpgradeConfig restockUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Restock Upgrade Settings")
		public final FilteredUpgradeConfig advancedRestockUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Deposit Upgrade Settings")
		public final FilteredUpgradeConfig depositUpgrade = new FilteredUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Deposit Upgrade Settings")
		public final FilteredUpgradeConfig advancedDepositUpgrade = new FilteredUpgradeConfig(16, 4);
		@net.minecraftforge.common.config.Config.Comment("Inception Upgrade Settings")
		public final InceptionUpgradeConfig inceptionUpgrade = new InceptionUpgradeConfig();
		@net.minecraftforge.common.config.Config.Comment("Refill Upgrade Settings")
		public final FilteredUpgradeConfig refillUpgrade = new FilteredUpgradeConfig(6, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Refill Upgrade Settings")
		public final FilteredUpgradeConfig advancedRefillUpgrade = new FilteredUpgradeConfig(12, 4);
		@net.minecraftforge.common.config.Config.Comment("Tool Swapper Upgrade Settings")
		public final FilteredUpgradeConfig toolSwapperUpgrade = new FilteredUpgradeConfig(8, 4);
		@net.minecraftforge.common.config.Config.Comment("Magnet Upgrade Settings")
		public final MagnetUpgradeConfig magnetUpgrade = new MagnetUpgradeConfig(9, 3, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Magnet Upgrade Settings")
		public final MagnetUpgradeConfig advancedMagnetUpgrade = new MagnetUpgradeConfig(16, 4, 5);
		@net.minecraftforge.common.config.Config.Comment("Void Upgrade Settings")
		public final VoidUpgradeConfig voidUpgrade = new VoidUpgradeConfig(9, 3);
		@net.minecraftforge.common.config.Config.Comment("Advanced Void Upgrade Settings")
		public final VoidUpgradeConfig advancedVoidUpgrade = new VoidUpgradeConfig(16, 4);

		@net.minecraftforge.common.config.Config.Comment("Smelting Upgrade Settings")
		public final CookingUpgradeConfig smeltingUpgrade = new CookingUpgradeConfig();
		@net.minecraftforge.common.config.Config.Comment("Smoking Upgrade Settings")
		public final CookingUpgradeConfig smokingUpgrade = new CookingUpgradeConfig();
		@net.minecraftforge.common.config.Config.Comment("Blasting Upgrade Settings")
		public final CookingUpgradeConfig blastingUpgrade = new CookingUpgradeConfig();
		@net.minecraftforge.common.config.Config.Comment("Auto-Smelting Upgrade Settings")
		public final AutoCookingUpgradeConfig autoSmeltingUpgrade = new AutoCookingUpgradeConfig();
		@net.minecraftforge.common.config.Config.Comment("Auto-Smoking Upgrade Settings")
		public final AutoCookingUpgradeConfig autoSmokingUpgrade = new AutoCookingUpgradeConfig();
		@net.minecraftforge.common.config.Config.Comment("Auto-Blasting Upgrade Settings")
		public final AutoCookingUpgradeConfig autoBlastingUpgrade = new AutoCookingUpgradeConfig();

		@net.minecraftforge.common.config.Config.Comment("Advanced Jukebox Upgrade Settings")
		public final JukeboxUpgradeConfig advancedJukeboxUpgrade = new JukeboxUpgradeConfig(12);

		@net.minecraftforge.common.config.Config.Comment("Tank Upgrade Settings")
		public final TankUpgradeConfig tankUpgrade = new TankUpgradeConfig();

		@net.minecraftforge.common.config.Config.Comment("Pump Upgrade Settings")
		public final PumpUpgradeConfig pumpUpgrade = new PumpUpgradeConfig();

		@net.minecraftforge.common.config.Config.Comment("Xp Pump Upgrade Settings")
		public final XpPumpUpgradeConfig xpPumpUpgrade = new XpPumpUpgradeConfig();

		@net.minecraftforge.common.config.Config.Comment("Battery Upgrade Settings")
		public final BatteryUpgradeConfig batteryUpgrade = new BatteryUpgradeConfig();

		@net.minecraftforge.common.config.Config.Comment("Stack Upgrade Settings")
		public final StackUpgradeSettings stackUpgrade = new StackUpgradeSettings();

		@net.minecraftforge.common.config.Config.Comment("Maximum number of upgrades of type per backpack in format of \"UpgradeRegistryName[or UpgradeGroup]|MaxNumber\"")
		public String[] maxUpgradesPerStorage = {"stack_upgrades|3", "furnace_upgrades|1", "jukebox_upgrades|1"};

		@net.minecraftforge.common.config.Config.Ignore
		public final IUpgradeCountLimitConfig maxUpgradesPerStorageConfig = new IUpgradeCountLimitConfig() {
			@Override
			public int getMaxUpgradesPerStorage(String storageType, @Nullable ResourceLocation upgradeRegistryName) {
				return upgradeRegistryName == null ? Integer.MAX_VALUE : getMaxUpgrades().getOrDefault(upgradeRegistryName.getPath(), Integer.MAX_VALUE);
			}

			@Override
			public int getMaxUpgradesInGroupPerStorage(String storageType, UpgradeGroup upgradeGroup) {
				return getMaxUpgrades().getOrDefault(upgradeGroup.name(), Integer.MAX_VALUE);
			}
		};

		@net.minecraftforge.common.config.Config.Comment("Determines whether player can right click on backpack that another player is wearing to open it. If off will turn off that capability for everyone and remove related settings from backpack.")
		public boolean allowOpeningOtherPlayerBackpacks = true;

		@net.minecraftforge.common.config.Config.Comment("Settings for Spawning Entities with Backpack")
		public final EntityBackpackAdditionsConfig entityBackpackAdditions = new EntityBackpackAdditionsConfig();

		@net.minecraftforge.common.config.Config.Comment("Turns on/off item fluid handler of backpack in its item form. There are some dupe bugs caused by default fluid handling implementation that manifest when backpack is drained / filled in its item form in another mod's tank and the only way to prevent them is disallowing drain/fill in item form altogether")
		public boolean itemFluidHandlerEnabled = true;

		@net.minecraftforge.common.config.Config.Comment("List of blocks that inventory interaction upgrades can't interact with - e.g. \"minecraft:shulker_box\"")
		public String[] noInteractionBlocks = {};

		@net.minecraftforge.common.config.Config.Ignore
		@Nullable
		private Set<ResourceLocation> disallowedItemsSet = null;
		@net.minecraftforge.common.config.Config.Ignore
		@Nullable
		private Set<ResourceLocation> noInteractionBlocksSet = null;
		@net.minecraftforge.common.config.Config.Ignore
		@Nullable
		private Map<String, Integer> maxUpgrades = null;

		public boolean isItemDisallowed(Item item) {
			if (disallowedItemsSet == null) {
				disallowedItemsSet = new HashSet<>();
				for (String itemName : disallowedItems) {
					disallowedItemsSet.add(new ResourceLocation(itemName));
				}
			}
			return disallowedItemsSet.contains(item.getRegistryName());
		}

		public boolean isBlockInteractionDisallowed(Block block) {
			if (noInteractionBlocksSet == null) {
				noInteractionBlocksSet = new HashSet<>();
				for (String blockName : noInteractionBlocks) {
					noInteractionBlocksSet.add(new ResourceLocation(blockName));
				}
			}
			return noInteractionBlocksSet.contains(block.getRegistryName());
		}

		private Map<String, Integer> getMaxUpgrades() {
			if (maxUpgrades == null) {
				maxUpgrades = new HashMap<>();
				for (String mapping : maxUpgradesPerStorage) {
					String[] upgradeMax = mapping.split("\\|");
					if (upgradeMax.length < 2 || !upgradeMax[1].matches("\\d+")) {
						continue;
					}
					maxUpgrades.put(upgradeMax[0], Integer.parseInt(upgradeMax[1]));
				}
			}
			return maxUpgrades;
		}

		private void clearCache() {
			disallowedItemsSet = null;
			noInteractionBlocksSet = null;
			maxUpgrades = null;
			entityBackpackAdditions.clearCache();
			stackUpgrade.config.clearNonStackableItems();
		}
	}

	public static class StackUpgradeSettings {
		@net.minecraftforge.common.config.Config.Comment("List of items that are not supposed to stack in storage even when stack upgrade is inserted. Item registry names are expected here.")
		public String[] nonStackableItems = StackUpgradeConfig.getDefaultNonStackableList();

		@net.minecraftforge.common.config.Config.Ignore
		public final StackUpgradeConfig config = new StackUpgradeConfig(() -> nonStackableItems);
	}

	public static class EntityBackpackAdditionsConfig {
		@net.minecraftforge.common.config.Config.Comment("Chance of an entity spawning with Backpack")
		@net.minecraftforge.common.config.Config.RangeDouble(min = 0, max = 1)
		public double chance = 0.01;
		@net.minecraftforge.common.config.Config.Comment("Whether mobs from spawners can spawn with Backpack too")
		public boolean spawnersToo = false;
		@net.minecraftforge.common.config.Config.Comment("Turns on/off addition of loot into backpacks")
		public boolean addLoot = true;
		@net.minecraftforge.common.config.Config.Comment("Turns on/off buffing the entity that wears backpack with potion effects. These are scaled based on how much loot is added.")
		public boolean buffWithPotionEffects = true;
		@net.minecraftforge.common.config.Config.Comment("Turns on/off buffing the entity that wears backpack with additional health. Health is scaled based on backpack tier the mob wears.")
		public boolean buffHealth = true;
		@net.minecraftforge.common.config.Config.Comment("Turns on/off equiping the entity that wears backpack with armor. What armor material and how enchanted is scaled based on backpack tier the mob wears.")
		public boolean equipWithArmor = true;
		@net.minecraftforge.common.config.Config.Comment("Map of entities that can spawn with backpack and related loot tables (if adding a loot is enabled) in format of \"EntityRegistryName|LootTableName\"")
		public String[] entityLootTableList = {
				"minecraft:creeper|minecraft:chests/desert_pyramid",
				"minecraft:enderman|minecraft:chests/end_city_treasure",
				"minecraft:evocation_illager|minecraft:chests/woodland_mansion",
				"minecraft:husk|minecraft:chests/desert_pyramid",
				"minecraft:skeleton|minecraft:chests/simple_dungeon",
				"minecraft:stray|minecraft:chests/igloo_chest",
				"minecraft:vex|minecraft:chests/woodland_mansion",
				"minecraft:vindication_illager|minecraft:chests/woodland_mansion",
				"minecraft:witch|minecraft:chests/jungle_temple",
				"minecraft:wither_skeleton|minecraft:chests/nether_bridge",
				"minecraft:zombie|minecraft:chests/simple_dungeon",
				"minecraft:zombie_villager|minecraft:chests/village_blacksmith",
				"minecraft:zombie_pigman|minecraft:chests/nether_bridge"
		};
		@net.minecraftforge.common.config.Config.Comment("List of music discs that are not supposed to be played by entities")
		public String[] discBlockList = {"botania:recordgaia1", "botania:recordgaia2"};
		@net.minecraftforge.common.config.Config.Comment("Turns on/off a chance that the entity that wears backpack gets jukebox upgrade and plays a music disc.")
		public boolean playJukebox = true;
		@net.minecraftforge.common.config.Config.Comment("Determines whether backpack drops to fake players if killed by them in addition to real ones that it always drops to")
		public boolean dropToFakePlayers = false;
		@net.minecraftforge.common.config.Config.Comment("Chance of mob dropping backpack when killed by player")
		@net.minecraftforge.common.config.Config.RangeDouble(min = 0, max = 1)
		public double backpackDropChance = 0.5;
		@net.minecraftforge.common.config.Config.Comment("Chance increase per looting level of mob dropping backpack")
		@net.minecraftforge.common.config.Config.RangeDouble(min = 0, max = 0.3)
		public double lootingChanceIncreasePerLevel = 0.15;

		@net.minecraftforge.common.config.Config.Ignore
		@Nullable
		private Map<ResourceLocation, Optional<ResourceLocation>> entityLootTables = null;
		@net.minecraftforge.common.config.Config.Ignore
		@Nullable
		private Set<ResourceLocation> blockedDiscs = null;

		/** The loot table of the entity's backpack, empty if it gets none ("null" in the list). */
		public Optional<ResourceLocation> getLootTableName(ResourceLocation entityName) {
			return getEntityLootTables().getOrDefault(entityName, Optional.empty());
		}

		public boolean canWearBackpack(ResourceLocation entityName) {
			return getEntityLootTables().containsKey(entityName);
		}

		public boolean isDiscBlocked(ResourceLocation discName) {
			if (blockedDiscs == null) {
				blockedDiscs = new HashSet<>();
				for (String disc : discBlockList) {
					blockedDiscs.add(new ResourceLocation(disc));
				}
			}
			return blockedDiscs.contains(discName);
		}

		private Map<ResourceLocation, Optional<ResourceLocation>> getEntityLootTables() {
			if (entityLootTables == null) {
				entityLootTables = new HashMap<>();
				for (String mapping : entityLootTableList) {
					String[] entityLoot = mapping.split("\\|");
					if (entityLoot.length < 2) {
						continue;
					}
					entityLootTables.put(new ResourceLocation(entityLoot[0]), entityLoot[1].equals("null") ? Optional.empty() : Optional.of(new ResourceLocation(entityLoot[1])));
				}
			}
			return entityLootTables;
		}

		private void clearCache() {
			entityLootTables = null;
			blockedDiscs = null;
		}
	}

	public static class BackpackConfig {
		@net.minecraftforge.common.config.Config.Comment("Number of inventory slots in the backpack")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 144)
		public int inventorySlotCount;
		@net.minecraftforge.common.config.Config.Comment("Number of upgrade slots in the backpack")
		@net.minecraftforge.common.config.Config.RangeInt(min = 0, max = 10)
		public int upgradeSlotCount;

		BackpackConfig(int inventorySlotCountDefault, int upgradeSlotCountDefault) {
			inventorySlotCount = inventorySlotCountDefault;
			upgradeSlotCount = upgradeSlotCountDefault;
		}
	}

	public static class FilteredUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Number of the upgrade's filter slots")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int filterSlots;
		@net.minecraftforge.common.config.Config.Comment("Number of filter slots displayed in a row")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 6)
		public int slotsInRow;

		FilteredUpgradeConfig(int defaultFilterSlots, int defaultSlotsInRow) {
			filterSlots = defaultFilterSlots;
			slotsInRow = defaultSlotsInRow;
		}
	}

	// Forge's config reads the fields a class declares, not inherited ones: each config class has all of its own

	public static class MagnetUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Number of the upgrade's filter slots")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int filterSlots;
		@net.minecraftforge.common.config.Config.Comment("Number of filter slots displayed in a row")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 6)
		public int slotsInRow;
		@net.minecraftforge.common.config.Config.Comment("Range around storage in blocks at which magnet will pickup items")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int magnetRange;

		MagnetUpgradeConfig(int defaultFilterSlots, int defaultSlotsInRow, int defaultMagnetRange) {
			filterSlots = defaultFilterSlots;
			slotsInRow = defaultSlotsInRow;
			magnetRange = defaultMagnetRange;
		}
	}

	public static class InceptionUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Allows / Disallows backpack upgrades to work with inventories of Backpacks in the Backpack with Inception Upgrade")
		public boolean upgradesUseInventoriesOfBackpacksInBackpack = true;
		@net.minecraftforge.common.config.Config.Comment("Allows / Disallows upgrades to be functional even when they are in Backpacks in the inventory of Backpack with Inception Upgrade")
		public boolean upgradesInContainedBackpacksAreFunctional = true;
	}

	public static class VoidUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Number of the upgrade's filter slots")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int filterSlots;
		@net.minecraftforge.common.config.Config.Comment("Number of filter slots displayed in a row")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 6)
		public int slotsInRow;
		@net.minecraftforge.common.config.Config.Comment("Determines whether void upgrade allows voiding anything or it only has overflow option")
		public boolean voidAnythingEnabled = true;

		VoidUpgradeConfig(int defaultFilterSlots, int defaultSlotsInRow) {
			filterSlots = defaultFilterSlots;
			slotsInRow = defaultSlotsInRow;
		}
	}

	public static class JukeboxUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Number of slots for discs in jukebox upgrade")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 16)
		public int numberOfSlots;
		@net.minecraftforge.common.config.Config.Comment("Number of lots displayed in a row")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 6)
		public int slotsInRow = 4;

		JukeboxUpgradeConfig(int defaultNumberOfSlots) {
			numberOfSlots = defaultNumberOfSlots;
		}
	}

	public static class TankUpgradeConfig implements ITankUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Capacity in mB the tank upgrade will have per row of storage slots")
		@net.minecraftforge.common.config.Config.RangeInt(min = 500, max = 20000)
		public int capacityPerSlotRow = 4000;
		@net.minecraftforge.common.config.Config.Comment("Ratio that gets applied (multiplies) to inventory stack multiplier before this is applied to tank capacity. Value lower than 1 makes stack multiplier affect the capacity less, higher makes it affect the capacity more. 0 turns off stack multiplier affecting tank capacity")
		@net.minecraftforge.common.config.Config.RangeDouble(min = 0, max = 5)
		public double stackMultiplierRatio = 1;
		@net.minecraftforge.common.config.Config.Comment("Cooldown between fill/drain actions done on fluid containers in tank slots. Only fills/drains one bucket worth to/from container after this cooldown and then waits again.")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 100)
		public int autoFillDrainContainerCooldown = 20;
		@net.minecraftforge.common.config.Config.Comment("How much mB can be transfered in / out per operation. This is a base transfer rate and same as max tank capacity gets multiplied by number of rows in storage and stack multiplier.")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 1000)
		public int maxInputOutput = 20;

		@Override
		public int getCapacityPerSlotRow() {
			return capacityPerSlotRow;
		}

		@Override
		public double getStackMultiplierRatio() {
			return stackMultiplierRatio;
		}

		@Override
		public int getAutoFillDrainContainerCooldown() {
			return autoFillDrainContainerCooldown;
		}

		@Override
		public int getMaxInputOutput() {
			return maxInputOutput;
		}
	}

	public static class PumpUpgradeConfig implements IPumpUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Number of fluid filter slots")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int filterSlots = 4;
		@net.minecraftforge.common.config.Config.Comment("How much mB can be transfered in / out per operation. This is a base transfer rate that gets multiplied by number of rows in storage and stack multiplier.")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 1000)
		public int maxInputOutput = 20;
		@net.minecraftforge.common.config.Config.Comment("Ratio that gets applied (multiplies) to inventory stack multiplier before this is applied to max input/output value. Value lower than 1 makes stack multiplier affect the capacity less, higher makes it affect the capacity more. 0 turns off stack multiplier affecting input/output")
		@net.minecraftforge.common.config.Config.RangeDouble(min = 0, max = 5)
		public double stackMultiplierRatio = 1;

		@Override
		public int getFilterSlots() {
			return filterSlots;
		}

		@Override
		public int getMaxInputOutput() {
			return maxInputOutput;
		}

		@Override
		public double getStackMultiplierRatio() {
			return stackMultiplierRatio;
		}
	}

	public static class XpPumpUpgradeConfig implements IXpPumpUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Whether xp pump can mend items with mending. Set false here to turn off the feature altogether.")
		public boolean mendingOn = true;
		@net.minecraftforge.common.config.Config.Comment("How many experience points at a maximum would be used to mend an item per operation (every 5 ticks and 1 xp point usually translates to 2 damage repaired).")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int maxXpPointsPerMending = 5;

		@Override
		public boolean isMendingOn() {
			return mendingOn;
		}

		@Override
		public int getMaxXpPointsPerMending() {
			return maxXpPointsPerMending;
		}
	}

	public static class BatteryUpgradeConfig implements IBatteryUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Energy in FE the battery upgrade will have per row of storage slots")
		@net.minecraftforge.common.config.Config.RangeInt(min = 500, max = 50000)
		public int energyPerSlotRow = 10000;
		@net.minecraftforge.common.config.Config.Comment("Ratio that gets applied (multiplies) to inventory stack multiplier before this is applied to max energy of the battery and max in/out. Value lower than 1 makes stack multiplier affect the max energy less, higher makes it affect the max energy more. 0 turns off stack multiplier affecting battery upgrade")
		@net.minecraftforge.common.config.Config.RangeDouble(min = 0, max = 5)
		public double stackMultiplierRatio = 1;
		@net.minecraftforge.common.config.Config.Comment("How much FE can be transfered in / out per operation. This is a base transfer rate and same as max capacity gets multiplied by number of rows in storage and stack multiplier.")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 1000)
		public int maxInputOutput = 20;

		@Override
		public int getEnergyPerSlotRow() {
			return energyPerSlotRow;
		}

		@Override
		public double getStackMultiplierRatio() {
			return stackMultiplierRatio;
		}

		@Override
		public int getMaxInputOutput() {
			return maxInputOutput;
		}
	}

	public static class CookingUpgradeConfig implements ICookingUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Smelting speed multiplier (1.0 equals speed at which vanilla furnace smelts items)")
		@net.minecraftforge.common.config.Config.RangeDouble(min = 0.25, max = 4.0)
		public double smeltingSpeedMultiplier = 1.0;
		@net.minecraftforge.common.config.Config.Comment("Fuel efficiency multiplier (1.0 equals speed at which it's used in vanilla furnace)")
		@net.minecraftforge.common.config.Config.RangeDouble(min = 0.25, max = 4.0)
		public double fuelEfficiencyMultiplier = 1.0;

		@Override
		public double getCookingSpeedMultiplier() {
			return smeltingSpeedMultiplier;
		}

		@Override
		public double getFuelEfficiencyMultiplier() {
			return fuelEfficiencyMultiplier;
		}
	}

	public static class AutoCookingUpgradeConfig implements IAutoCookingUpgradeConfig {
		@net.minecraftforge.common.config.Config.Comment("Smelting speed multiplier (1.0 equals speed at which vanilla furnace smelts items)")
		@net.minecraftforge.common.config.Config.RangeDouble(min = 0.25, max = 4.0)
		public double smeltingSpeedMultiplier = 1.0;
		@net.minecraftforge.common.config.Config.Comment("Fuel efficiency multiplier (1.0 equals speed at which it's used in vanilla furnace)")
		@net.minecraftforge.common.config.Config.RangeDouble(min = 0.25, max = 4.0)
		public double fuelEfficiencyMultiplier = 1.0;
		@net.minecraftforge.common.config.Config.Comment("Number of input filter slots")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int inputFilterSlots = 8;
		@net.minecraftforge.common.config.Config.Comment("Number of input filter slots displayed in a row")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 6)
		public int inputFilterSlotsInRow = 4;
		@net.minecraftforge.common.config.Config.Comment("Number of fuel filter slots")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 20)
		public int fuelFilterSlots = 4;
		@net.minecraftforge.common.config.Config.Comment("Number of fuel filter slots displayed in a row")
		@net.minecraftforge.common.config.Config.RangeInt(min = 1, max = 6)
		public int fuelFilterSlotsInRow = 4;

		@Override
		public double getCookingSpeedMultiplier() {
			return smeltingSpeedMultiplier;
		}

		@Override
		public double getFuelEfficiencyMultiplier() {
			return fuelEfficiencyMultiplier;
		}

		@Override
		public int getInputFilterSlots() {
			return inputFilterSlots;
		}

		@Override
		public int getInputFilterSlotsInRow() {
			return inputFilterSlotsInRow;
		}

		@Override
		public int getFuelFilterSlots() {
			return fuelFilterSlots;
		}

		@Override
		public int getFuelFilterSlotsInRow() {
			return fuelFilterSlotsInRow;
		}
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(DeconstructedBackpacks.MOD_ID)) {
			ConfigManager.sync(DeconstructedBackpacks.MOD_ID, net.minecraftforge.common.config.Config.Type.INSTANCE);
			SERVER.clearCache();
		}
	}
}
