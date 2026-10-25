package dev.feldsher.deconstructedbackpacks.upgrades.inception;

import net.minecraft.util.IStringSerializable;

/** Which the upgrades use first: the backpack's own inventory, or those of the backpacks in it. */
public enum InventoryOrder implements IStringSerializable {
	MAIN_FIRST("main_first"),
	INCEPTED_FIRST("incepted_first");

	private static final InventoryOrder[] VALUES = values();

	private final String name;

	InventoryOrder(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public InventoryOrder next() {
		return VALUES[(ordinal() + 1) % VALUES.length];
	}

	public static InventoryOrder fromName(String name) {
		for (InventoryOrder value : VALUES) {
			if (value.name.equals(name)) {
				return value;
			}
		}
		return MAIN_FIRST;
	}
}
