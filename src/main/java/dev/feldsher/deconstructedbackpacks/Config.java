package dev.feldsher.deconstructedbackpacks;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.HashSet;
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

		@net.minecraftforge.common.config.Config.Comment("Leather Backpack Settings")
		public final BackpackConfig leatherBackpack = new BackpackConfig(27, 1);
		@net.minecraftforge.common.config.Config.Comment("Iron Backpack Settings")
		public final BackpackConfig ironBackpack = new BackpackConfig(54, 2);
		@net.minecraftforge.common.config.Config.Comment("Gold Backpack Settings")
		public final BackpackConfig goldBackpack = new BackpackConfig(81, 3);
		@net.minecraftforge.common.config.Config.Comment("Diamond Backpack Settings")
		public final BackpackConfig diamondBackpack = new BackpackConfig(108, 5);

		@net.minecraftforge.common.config.Config.Ignore
		@Nullable
		private Set<ResourceLocation> disallowedItemsSet = null;

		public boolean isItemDisallowed(Item item) {
			if (disallowedItemsSet == null) {
				disallowedItemsSet = new HashSet<>();
				for (String itemName : disallowedItems) {
					disallowedItemsSet.add(new ResourceLocation(itemName));
				}
			}
			return disallowedItemsSet.contains(item.getRegistryName());
		}

		private void clearCache() {
			disallowedItemsSet = null;
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

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.getModID().equals(DeconstructedBackpacks.MOD_ID)) {
			ConfigManager.sync(DeconstructedBackpacks.MOD_ID, net.minecraftforge.common.config.Config.Type.INSTANCE);
			SERVER.clearCache();
		}
	}
}
