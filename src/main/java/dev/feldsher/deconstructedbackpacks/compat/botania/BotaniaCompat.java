package dev.feldsher.deconstructedbackpacks.compat.botania;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedcore.upgrades.magnet.MagnetUpgradeWrapper;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Magnet upgrades leave alone what's around Botania's Solegnolia, as Botania's own magnet ring does. Through reflection,
 * so that Botania is needed neither to build nor to run.
 */
public final class BotaniaCompat {
	private static final String MOD_ID = "botania";
	private static final String BOTANIA_API = "vazkii.botania.api.BotaniaAPI";
	private static final String INTERNAL_METHOD_HANDLER = "vazkii.botania.api.internal.IInternalMethodHandler";

	private BotaniaCompat() {}

	public static void init() {
		if (!Loader.isModLoaded(MOD_ID)) {
			return;
		}
		try {
			// read on each check: Botania sets its handler as it loads
			Field internalHandler = Class.forName(BOTANIA_API).getField("internalHandler");
			Method hasSolegnoliaAround = Class.forName(INTERNAL_METHOD_HANDLER).getMethod("hasSolegnoliaAround", Entity.class);
			MagnetUpgradeWrapper.addMagnetPreventionChecker(entity -> {
				try {
					Object handler = internalHandler.get(null);
					return handler != null && (boolean) hasSolegnoliaAround.invoke(handler, entity);
				} catch (ReflectiveOperationException e) {
					return false;
				}
			});
		} catch (ReflectiveOperationException e) {
			DeconstructedBackpacks.LOGGER.error("Botania's API isn't as expected: magnets won't respect the Solegnolia", e);
		}
	}
}
