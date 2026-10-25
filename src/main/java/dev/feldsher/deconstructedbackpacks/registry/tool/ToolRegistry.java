package dev.feldsher.deconstructedbackpacks.registry.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.registry.IRegistryDataLoader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Which items are tools for which blocks and entities, beyond what vanilla tool classes tell, for the tool swapper's
 * key: from the "block_tools" and "entity_tools" registry data. Blocks and entities are kept by registry name. A block
 * or entity of a mod that has no mapping takes any unstackable item of the same mod, as most such tools are wrenches.
 */
public class ToolRegistry {
	private ToolRegistry() {}

	private static final String TOOLS_PROPERTY = "tools";

	private static final Set<String> modsWithMapping = new HashSet<>();

	private static final ToolMapping<BlockContext> BLOCK_TOOL_MAPPING = new ToolMapping<>(blockContext -> blockContext.getBlock().getRegistryName());
	private static final ToolMapping<Entity> ENTITY_TOOL_MAPPING = new ToolMapping<>(EntityList::getKey);

	public static boolean isToolForBlock(ItemStack stack, World world, IBlockState state, BlockPos pos) {
		return BLOCK_TOOL_MAPPING.isToolFor(stack, state.getBlock().getRegistryName(), () -> new BlockContext(world, state, pos));
	}

	public static boolean isToolForEntity(ItemStack stack, Entity entity) {
		return ENTITY_TOOL_MAPPING.isToolFor(stack, EntityList.getKey(entity), () -> entity);
	}

	static void addModWithMapping(String modId) {
		modsWithMapping.add(modId);
	}

	private abstract static class ToolsLoaderBase<C> implements IRegistryDataLoader {
		private final List<IMatcherFactory<C>> objectMatcherFactories;
		private final ToolMapping<C> toolMapping;
		private final Predicate<ResourceLocation> isInRegistry;
		private final String name;
		private final String objectJsonArrayName;

		protected ToolsLoaderBase(List<IMatcherFactory<C>> objectMatcherFactories, ToolMapping<C> toolMapping, Predicate<ResourceLocation> isInRegistry,
				String name, String objectJsonArrayName) {
			this.objectMatcherFactories = objectMatcherFactories;
			this.toolMapping = toolMapping;
			this.isInRegistry = isInRegistry;
			this.name = name;
			this.objectJsonArrayName = objectJsonArrayName;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void parse(JsonObject json, @Nullable String modId) {
			for (JsonElement jsonElement : JsonUtils.getJsonArray(json, name)) {
				if (jsonElement.isJsonObject()) {
					parseEntry(jsonElement.getAsJsonObject());
				}
			}
			toolMapping.getObjectTools().keySet().forEach(rn -> modsWithMapping.add(rn.getNamespace()));
		}

		@Override
		public void clear() {
			toolMapping.clear();
			modsWithMapping.clear();
		}

		private void parseEntry(JsonObject entry) {
			if (entry.size() == 1) {
				parseFromProperty(entry);
			} else if (entry.size() == 2 && entry.has(objectJsonArrayName) && entry.has(TOOLS_PROPERTY)) {
				parseFromArrays(JsonUtils.getJsonArray(entry, objectJsonArrayName), JsonUtils.getJsonArray(entry, TOOLS_PROPERTY));
			} else {
				DeconstructedBackpacks.LOGGER.error("Invalid {} entry - needs to have either 1 array property with mod/object name or \"{}\" and \"tools\" array properties {}",
						name, objectJsonArrayName, entry);
			}
		}

		private void parseFromArrays(JsonArray objectsArray, JsonArray toolsArray) {
			Tools tools = getItemsAndItemPredicates(toolsArray);
			if (tools.isEmpty()) {
				return;
			}
			for (JsonElement jsonElement : objectsArray) {
				if (jsonElement.isJsonPrimitive() && jsonElement.getAsString().contains(":")) {
					parseObjectEntry(tools, jsonElement.getAsString());
				} else {
					parseObjectPredicateEntry(tools, jsonElement);
				}
			}
		}

		private void parseObjectPredicateEntry(Tools tools, JsonElement jsonElement) {
			for (IMatcherFactory<C> matcherFactory : objectMatcherFactories) {
				if (matcherFactory.appliesTo(jsonElement)) {
					matcherFactory.getPredicate(jsonElement).ifPresent(predicate -> toolMapping.addObjectPredicateTools(tools, predicate));
					break;
				}
			}
		}

		private void parseObjectEntry(Tools tools, String objectName) {
			ResourceLocation registryName = new ResourceLocation(objectName);
			if (isInRegistry.test(registryName)) {
				toolMapping.addObjectTools(tools, registryName);
			} else {
				DeconstructedBackpacks.LOGGER.debug("{} doesn't exist in registry, skipping ...", objectName);
			}
		}

		private void parseFromProperty(JsonObject entry) {
			for (Map.Entry<String, JsonElement> property : entry.entrySet()) {
				if (!property.getValue().isJsonArray()) {
					DeconstructedBackpacks.LOGGER.error("Invalid tools list - needs to be an array {}", property.getValue());
					continue;
				}
				Tools tools = getItemsAndItemPredicates(property.getValue().getAsJsonArray());
				if (tools.isEmpty()) {
					continue;
				}
				if (property.getKey().contains(":")) {
					parseObjectEntry(tools, property.getKey());
				} else if (Loader.isModLoaded(property.getKey())) {
					toolMapping.addObjectPredicateTools(tools, new ModMatcher<>(property.getKey(), toolMapping.getNameFromContext));
				} else {
					DeconstructedBackpacks.LOGGER.debug("{} mod isn't loaded, skipping ... {} ", property.getKey(), property);
				}
			}
		}
	}

	private static Tools getItemsAndItemPredicates(JsonArray toolArray) {
		Tools tools = new Tools();
		for (JsonElement jsonElement : toolArray) {
			if (jsonElement.isJsonPrimitive()) {
				ResourceLocation itemName = new ResourceLocation(jsonElement.getAsString());
				Item item = ForgeRegistries.ITEMS.getValue(itemName);
				if (item == null) {
					DeconstructedBackpacks.LOGGER.debug("{} isn't loaded in item registry, skipping ...", itemName);
				} else {
					tools.items.add(item);
				}
			} else if (jsonElement.isJsonObject()) {
				Matchers.getItemMatcher(jsonElement).ifPresent(tools.itemPredicates::add);
			}
		}
		return tools;
	}

	private static class Tools {
		private final Set<Item> items = new HashSet<>();
		private final Set<Predicate<ItemStack>> itemPredicates = new HashSet<>();

		private boolean isEmpty() {
			return items.isEmpty() && itemPredicates.isEmpty();
		}
	}

	public static class BlockToolsLoader extends ToolsLoaderBase<BlockContext> {
		public BlockToolsLoader() {
			super(Matchers.getBlockMatcherFactories(), BLOCK_TOOL_MAPPING, ForgeRegistries.BLOCKS::containsKey, "block_tools", "blocks");
		}
	}

	public static class EntityToolsLoader extends ToolsLoaderBase<Entity> {
		public EntityToolsLoader() {
			super(Matchers.getEntityMatcherFactories(), ENTITY_TOOL_MAPPING, ForgeRegistries.ENTITIES::containsKey, "entity_tools", "entities");
		}
	}

	private static class ToolMapping<C> {
		private final Function<C, ResourceLocation> getNameFromContext;
		private final Map<ResourceLocation, Set<Item>> notToolCache = new HashMap<>();

		private final Map<ResourceLocation, Set<Item>> objectTools = new HashMap<>();
		private final Map<ResourceLocation, Set<Predicate<ItemStack>>> objectToolPredicates = new HashMap<>();
		private final Map<Predicate<C>, Set<Item>> objectPredicateTools = new HashMap<>();
		private final Map<Predicate<C>, Set<Predicate<ItemStack>>> objectPredicateToolPredicates = new HashMap<>();

		private ToolMapping(Function<C, ResourceLocation> getNameFromContext) {
			this.getNameFromContext = getNameFromContext;
		}

		private void addObjectPredicateTools(Tools tools, Predicate<C> predicate) {
			tools.items.forEach(t -> objectPredicateTools.computeIfAbsent(predicate, p -> new HashSet<>()).add(t));
			tools.itemPredicates.forEach(tp -> objectPredicateToolPredicates.computeIfAbsent(predicate, p -> new HashSet<>()).add(tp));
		}

		private void addObjectTools(Tools tools, ResourceLocation object) {
			tools.items.forEach(t -> objectTools.computeIfAbsent(object, b -> new HashSet<>()).add(t));
			tools.itemPredicates.forEach(tp -> objectToolPredicates.computeIfAbsent(object, b -> new HashSet<>()).add(tp));
		}

		private void clear() {
			notToolCache.clear();
			objectTools.clear();
			objectToolPredicates.clear();
			objectPredicateTools.clear();
			objectPredicateToolPredicates.clear();
		}

		/** The object's own tools first, then the predicates' ones; whatever matches is remembered by the object's name. */
		private boolean isToolFor(ItemStack stack, @Nullable ResourceLocation object, Supplier<C> getContext) {
			Item item = stack.getItem();
			if (object != null) {
				if (objectTools.getOrDefault(object, Collections.emptySet()).contains(item)) {
					return true;
				}
				if (notToolCache.getOrDefault(object, Collections.emptySet()).contains(item)) {
					return false;
				}
				if (tryToMatchTools(stack, object, objectToolPredicates.getOrDefault(object, Collections.emptySet()))) {
					return true;
				}
			}

			C context = getContext.get();
			for (Map.Entry<Predicate<C>, Set<Item>> entry : objectPredicateTools.entrySet()) {
				if (entry.getKey().test(context) && entry.getValue().contains(item)) {
					addObjectToolMapping(object, item);
					return true;
				}
			}
			for (Map.Entry<Predicate<C>, Set<Predicate<ItemStack>>> entry : objectPredicateToolPredicates.entrySet()) {
				if (entry.getKey().test(context) && tryToMatchTools(stack, object, entry.getValue())) {
					return true;
				}
			}

			if (object == null) {
				return false;
			}
			if (isNoMappingModAndNonStackableItemFromSameMod(stack, object)) {
				addObjectToolMapping(object, item);
				return true;
			}
			notToolCache.computeIfAbsent(object, b -> new HashSet<>()).add(item);
			return false;
		}

		private boolean isNoMappingModAndNonStackableItemFromSameMod(ItemStack stack, ResourceLocation object) {
			ResourceLocation itemName = stack.getItem().getRegistryName();
			return !object.getNamespace().equals("minecraft") && !modsWithMapping.contains(object.getNamespace())
					&& itemName != null && itemName.getNamespace().equals(object.getNamespace()) && stack.getMaxStackSize() == 1;
		}

		private boolean tryToMatchTools(ItemStack stack, @Nullable ResourceLocation object, Set<Predicate<ItemStack>> toolPredicates) {
			for (Predicate<ItemStack> itemPredicate : toolPredicates) {
				if (itemPredicate.test(stack)) {
					addObjectToolMapping(object, stack.getItem());
					return true;
				}
			}
			return false;
		}

		private void addObjectToolMapping(@Nullable ResourceLocation object, Item item) {
			if (object != null) {
				objectTools.computeIfAbsent(object, b -> new HashSet<>()).add(item);
			}
		}

		private Map<ResourceLocation, Set<Item>> getObjectTools() {
			return objectTools;
		}
	}
}
