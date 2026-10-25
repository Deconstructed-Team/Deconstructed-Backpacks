package dev.feldsher.deconstructedbackpacks.upgrades.toolswapper;

import net.minecraft.util.IStringSerializable;

/** When the tool swapper swaps a tool in for the block clicked: for anything held, only for other tools, or never. */
public enum ToolSwapMode implements IStringSerializable {
	ANY("name"),
	ONLY_TOOLS("onlyTools"),
	NO_SWAP("noSwap");

	private static final ToolSwapMode[] VALUES = values();

	private final String name;

	ToolSwapMode(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public ToolSwapMode next() {
		return VALUES[(ordinal() + 1) % VALUES.length];
	}

	public static ToolSwapMode fromName(String name) {
		for (ToolSwapMode value : VALUES) {
			if (value.name.equals(name)) {
				return value;
			}
		}
		return ANY;
	}
}
