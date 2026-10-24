package dev.feldsher.deconstructedbackpacks.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.feldsher.deconstructedbackpacks.Config;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

/** {@code {"type": "deconstructedbackpacks:tier_enabled", "tier": "copper"}}: a recipe of a tier the config may leave out. */
public class TierEnabledCondition implements IConditionFactory {
	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json) {
		String tier = JsonUtils.getString(json, "tier");
		switch (tier) {
			case "copper":
				return () -> Config.SERVER.copperTierEnabled;
			case "netherite":
				return () -> Config.SERVER.netheriteTierEnabled;
			default:
				throw new JsonSyntaxException("Unknown backpack tier " + tier);
		}
	}
}
