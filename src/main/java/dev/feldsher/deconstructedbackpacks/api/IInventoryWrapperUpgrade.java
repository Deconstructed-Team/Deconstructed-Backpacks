package dev.feldsher.deconstructedbackpacks.api;

import dev.feldsher.deconstructedcore.inventory.IItemHandlerSimpleInserter;

/** An upgrade that changes the inventory the backpack's upgrades work with, by wrapping it. */
public interface IInventoryWrapperUpgrade {
	IItemHandlerSimpleInserter wrapInventory(IItemHandlerSimpleInserter inventory);
}
