package dev.feldsher.deconstructedbackpacks.compat.ae2;

import net.minecraftforge.fml.common.Loader;

/**
 * AE2UD's terminals: a right click with a backpack held onto an item of the network takes it into the backpack. Its
 * classes are touched only when AE2UD, or an AE2 with the same API, is there.
 */
public final class Ae2Compat {
	private static final String MOD_ID = "appliedenergistics2";
	private static final String KEY_CLASS = "appeng.api.stacks.AEItemKey";
	private static Boolean available = null;

	private Ae2Compat() {}

	public static boolean isAvailable() {
		if (available == null) {
			available = Loader.isModLoaded(MOD_ID) && hasClass(KEY_CLASS) && hasClass("appeng.container.AEBaseContainer");
		}
		return available;
	}

	private static boolean hasClass(String name) {
		try {
			Class.forName(name, false, Ae2Compat.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException | LinkageError e) {
			return false;
		}
	}
}
