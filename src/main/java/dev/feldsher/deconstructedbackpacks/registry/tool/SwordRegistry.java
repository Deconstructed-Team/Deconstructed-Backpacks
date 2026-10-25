package dev.feldsher.deconstructedbackpacks.registry.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.registry.IRegistryDataLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/** What the tool swapper takes for a sword: any {@link ItemSword}, and what the "swords" registry data names. */
public class SwordRegistry {
	private SwordRegistry() {}

	private static final Set<Item> SWORD_ITEMS = new HashSet<>();
	private static final Map<String, Set<Predicate<ItemStack>>> MOD_SWORD_MATCHERS = new HashMap<>();
	private static final Set<Predicate<ItemStack>> SWORD_MATCHERS = new HashSet<>();

	public static boolean isSword(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		if (SWORD_ITEMS.contains(stack.getItem()) || stack.getItem() instanceof ItemSword) {
			return true;
		}

		for (Predicate<ItemStack> swordMatcher : SWORD_MATCHERS) {
			if (swordMatcher.test(stack)) {
				return true;
			}
		}

		ResourceLocation registryName = stack.getItem().getRegistryName();
		if (registryName == null) {
			return false;
		}
		for (Predicate<ItemStack> matcher : MOD_SWORD_MATCHERS.getOrDefault(registryName.getNamespace(), new HashSet<>())) {
			if (matcher.test(stack)) {
				return true;
			}
		}
		return false;
	}

	public static class SwordsLoader implements IRegistryDataLoader {
		@Override
		public String getName() {
			return "swords";
		}

		@Override
		public void parse(JsonObject json, @Nullable String modId) {
			for (JsonElement jsonElement : JsonUtils.getJsonArray(json, "swords")) {
				if (jsonElement.isJsonPrimitive()) {
					parseSword(jsonElement.getAsString());
				} else {
					Matchers.getItemMatcher(jsonElement).ifPresent(swordMatcher -> {
						if (modId != null) {
							MOD_SWORD_MATCHERS.computeIfAbsent(modId, m -> new HashSet<>()).add(swordMatcher);
						} else {
							SWORD_MATCHERS.add(swordMatcher);
						}
					});
				}
			}
		}

		private void parseSword(String swordName) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(swordName));
			if (item != null) {
				SWORD_ITEMS.add(item);
				return;
			}
			String modId = swordName.split(":")[0];
			if (!Loader.isModLoaded(modId)) {
				DeconstructedBackpacks.LOGGER.debug("Mod {} isn't loaded skipping load of sword {}", modId, swordName);
			} else {
				DeconstructedBackpacks.LOGGER.warn("Mod {} is loaded and yet sword {} doesn't exist in registry, skipping its load", modId, swordName);
			}
		}

		@Override
		public void clear() {
			SWORD_ITEMS.clear();
			SWORD_MATCHERS.clear();
			MOD_SWORD_MATCHERS.clear();
		}
	}
}
