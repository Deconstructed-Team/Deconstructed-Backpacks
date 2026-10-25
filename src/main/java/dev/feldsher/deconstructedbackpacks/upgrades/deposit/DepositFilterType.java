package dev.feldsher.deconstructedbackpacks.upgrades.deposit;

import java.util.HashMap;
import java.util.Map;

public enum DepositFilterType {
	ALLOW("allow"),
	BLOCK("block"),
	INVENTORY("inventory");

	private static final Map<String, DepositFilterType> NAME_VALUES = new HashMap<>();
	private static final DepositFilterType[] VALUES = values();

	static {
		for (DepositFilterType value : VALUES) {
			NAME_VALUES.put(value.getSerializedName(), value);
		}
	}

	private final String name;

	DepositFilterType(String name) {
		this.name = name;
	}

	public String getSerializedName() {
		return name;
	}

	public DepositFilterType next() {
		return VALUES[(ordinal() + 1) % VALUES.length];
	}

	public static DepositFilterType fromName(String name) {
		return NAME_VALUES.getOrDefault(name, BLOCK);
	}
}
