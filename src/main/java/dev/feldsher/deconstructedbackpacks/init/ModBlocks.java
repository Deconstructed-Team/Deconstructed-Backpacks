package dev.feldsher.deconstructedbackpacks.init;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackBlock;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackBlockEntity;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One block per backpack tier. They have no item blocks: a backpack's item is its {@link BackpackItem}. */
@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID)
public class ModBlocks {
	private ModBlocks() {}

	public static final BackpackBlock BACKPACK = new BackpackBlock("backpack", () -> ModItems.BACKPACK);
	public static final BackpackBlock IRON_BACKPACK = new BackpackBlock("iron_backpack", () -> ModItems.IRON_BACKPACK);
	public static final BackpackBlock GOLD_BACKPACK = new BackpackBlock("gold_backpack", () -> ModItems.GOLD_BACKPACK);
	public static final BackpackBlock DIAMOND_BACKPACK = new BackpackBlock("diamond_backpack", () -> ModItems.DIAMOND_BACKPACK);

	public static final List<BackpackBlock> BACKPACKS = Collections.unmodifiableList(Arrays.asList(BACKPACK, IRON_BACKPACK, GOLD_BACKPACK, DIAMOND_BACKPACK));

	public static BackpackBlock getBlock(BackpackItem backpackItem) {
		for (BackpackBlock block : BACKPACKS) {
			if (block.getBackpackItem() == backpackItem) {
				return block;
			}
		}
		throw new IllegalArgumentException("No block for backpack " + backpackItem.getRegistryName());
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		BACKPACKS.forEach(event.getRegistry()::register);
		GameRegistry.registerTileEntity(BackpackBlockEntity.class, DeconstructedBackpacks.getRL("backpack"));
	}
}
