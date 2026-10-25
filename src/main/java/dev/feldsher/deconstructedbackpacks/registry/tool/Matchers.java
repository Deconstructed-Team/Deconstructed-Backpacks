package dev.feldsher.deconstructedbackpacks.registry.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import net.minecraft.block.BlockRailBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The matchers registry data files can use in place of names. Items: {"type": "ore", "ore": ...}, an ore dictionary
 * name, as 1.21's item tags, and {"type": "nonbt", "item": ...}, the item with no NBT, as 1.21's "nocomponents".
 * Blocks: a mod id, and {"type": "all"}, {"type": "rail"} and {"type": "item_handler"}. Entities: {"type": "animal"},
 * {"type": "living"} and {"type": "tameable"}.
 */
public class Matchers {
	private Matchers() {}

	private static final List<TypedMatcherFactory<ItemStack>> ITEM_MATCHER_FACTORIES = new ArrayList<>();
	private static final List<IMatcherFactory<BlockContext>> BLOCK_MATCHER_FACTORIES = new ArrayList<>();
	private static final List<IMatcherFactory<Entity>> ENTITY_MATCHER_FACTORIES = new ArrayList<>();

	static {
		ITEM_MATCHER_FACTORIES.add(new TypedMatcherFactory<ItemStack>("ore") {
			@Override
			protected Optional<Predicate<ItemStack>> getPredicateFromObject(JsonObject jsonObject) {
				int oreId = OreDictionary.getOreID(JsonUtils.getString(jsonObject, "ore"));
				return Optional.of(stack -> !stack.isEmpty() && contains(OreDictionary.getOreIDs(stack), oreId));
			}
		});
		ITEM_MATCHER_FACTORIES.add(new TypedMatcherFactory<ItemStack>("nonbt") {
			@Override
			protected Optional<Predicate<ItemStack>> getPredicateFromObject(JsonObject jsonObject) {
				ResourceLocation itemName = new ResourceLocation(JsonUtils.getString(jsonObject, "item"));
				Item item = ForgeRegistries.ITEMS.getValue(itemName);
				if (item == null) {
					DeconstructedBackpacks.LOGGER.debug("{} isn't loaded in item registry, skipping ...", itemName);
					return Optional.empty();
				}
				return Optional.of(stack -> stack.getItem() == item && !stack.hasTagCompound());
			}
		});

		BLOCK_MATCHER_FACTORIES.add(new IMatcherFactory<BlockContext>() {
			@Override
			public boolean appliesTo(JsonElement jsonElement) {
				return jsonElement.isJsonPrimitive();
			}

			@Override
			public Optional<Predicate<BlockContext>> getPredicate(JsonElement jsonElement) {
				String modId = jsonElement.getAsString();
				if (!Loader.isModLoaded(modId)) {
					DeconstructedBackpacks.LOGGER.debug("{} mod isn't loaded, skipping ...", modId);
					return Optional.empty();
				}
				return Optional.of(new ModMatcher<>(modId, blockContext -> blockContext.getBlock().getRegistryName()));
			}
		});
		BLOCK_MATCHER_FACTORIES.add(new TypedMatcherFactory<BlockContext>("all") {
			@Override
			protected Optional<Predicate<BlockContext>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(blockContext -> true);
			}
		});
		BLOCK_MATCHER_FACTORIES.add(new TypedMatcherFactory<BlockContext>("rail") {
			@Override
			protected Optional<Predicate<BlockContext>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(blockContext -> blockContext.getBlock() instanceof BlockRailBase);
			}
		});
		BLOCK_MATCHER_FACTORIES.add(new TypedMatcherFactory<BlockContext>("item_handler") {
			@Override
			protected Optional<Predicate<BlockContext>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(blockContext -> {
					TileEntity te = blockContext.getWorld().getTileEntity(blockContext.getPos());
					return te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				});
			}
		});

		ENTITY_MATCHER_FACTORIES.add(new TypedMatcherFactory<Entity>("animal") {
			@Override
			protected Optional<Predicate<Entity>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(EntityAnimal.class::isInstance);
			}
		});
		ENTITY_MATCHER_FACTORIES.add(new TypedMatcherFactory<Entity>("living") {
			@Override
			protected Optional<Predicate<Entity>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(EntityLivingBase.class::isInstance);
			}
		});
		ENTITY_MATCHER_FACTORIES.add(new TypedMatcherFactory<Entity>("tameable") {
			@Override
			protected Optional<Predicate<Entity>> getPredicateFromObject(JsonObject jsonObject) {
				return Optional.of(EntityTameable.class::isInstance);
			}
		});
	}

	private static boolean contains(int[] values, int value) {
		for (int v : values) {
			if (v == value) {
				return true;
			}
		}
		return false;
	}

	public static Optional<Predicate<ItemStack>> getItemMatcher(JsonElement jsonElement) {
		for (TypedMatcherFactory<ItemStack> itemMatcherFactory : ITEM_MATCHER_FACTORIES) {
			if (itemMatcherFactory.appliesTo(jsonElement)) {
				return itemMatcherFactory.getPredicate(jsonElement);
			}
		}
		return Optional.empty();
	}

	public static List<IMatcherFactory<BlockContext>> getBlockMatcherFactories() {
		return BLOCK_MATCHER_FACTORIES;
	}

	public static List<IMatcherFactory<Entity>> getEntityMatcherFactories() {
		return ENTITY_MATCHER_FACTORIES;
	}
}
