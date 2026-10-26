package dev.feldsher.deconstructedbackpacks.backpack;

import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.UUID;

/**
 * This class has logic for deduplicating backpack UUIDs because people duplicating backpack including its UUID seems to happen fairly regularly
 * as well as some mods also add enchants / legit ways to duplicate loot when mobs are killed which can again include backpack.
 * A duplicate keeps how it looks, not the contents: it's a new, empty backpack from then on.
 */
public class UUIDDeduplicator {
	private static final double ITEM_ENTITY_SEARCH_RANGE = 10;

	private UUIDDeduplicator() {}

	/** The other backpacks the player carries with the contents of the one they opened lose them. */
	public static void checkForDuplicateBackpacksAndRemoveTheirUUID(EntityPlayer player, UUID backpackUuid, ItemStack backpack) {
		PlayerInventoryProvider.runOnBackpacks(player, (otherBackpack, slot) -> {
			if (otherBackpack != backpack) {
				IBackpackWrapper wrapper = BackpackWrapper.fromStack(otherBackpack);
				wrapper.getContentsUuid().ifPresent(uuid -> {
					if (uuid.equals(backpackUuid)) {
						wrapper.removeContentsUUIDTag();
						wrapper.onContentsNbtUpdated();
					}
				});
			}
			return false;
		});
	}

	/** A backpack dropped, or loaded with its chunk, near another dropped one with its contents loses them. */
	public static void dedupeBackpackItemEntityInArea(EntityItem newBackpackItemEntity) {
		IBackpackWrapper newBackpackWrapper = BackpackWrapper.fromStack(newBackpackItemEntity.getItem());
		newBackpackWrapper.getContentsUuid().ifPresent(backpackId -> dedupeBackpackItemEntityInArea(newBackpackWrapper, newBackpackItemEntity, backpackId));
	}

	private static void dedupeBackpackItemEntityInArea(IBackpackWrapper newBackpackWrapper, EntityItem newBackpackItemEntity, UUID backpackId) {
		for (EntityItem entity : newBackpackItemEntity.world.getEntitiesWithinAABB(EntityItem.class, newBackpackItemEntity.getEntityBoundingBox().grow(ITEM_ENTITY_SEARCH_RANGE), EntityItem::isEntityAlive)) {
			if (entity != newBackpackItemEntity && checkEntityBackpackIdMatchAndRemoveIfItDoes(newBackpackWrapper, backpackId, entity)) {
				break;
			}
		}
	}

	private static boolean checkEntityBackpackIdMatchAndRemoveIfItDoes(IBackpackWrapper newBackpackWrapper, UUID newBackpackId, EntityItem entity) {
		if (!(entity.getItem().getItem() instanceof BackpackItem)) {
			return false;
		}
		return BackpackWrapper.fromStack(entity.getItem()).getContentsUuid().map(backpackId -> {
			if (backpackId.equals(newBackpackId)) {
				newBackpackWrapper.removeContentsUUIDTag();
				newBackpackWrapper.onContentsNbtUpdated();
				return true;
			}
			return false;
		}).orElse(false);
	}
}
