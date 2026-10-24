package dev.feldsher.deconstructedbackpacks.backpack;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedcore.storage.StorageContents;
import dev.feldsher.deconstructedcore.storage.StorageContentsManager;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

/**
 * The contents of every backpack, by its UUID; the backpack stack carries only the UUID. On the server they're saved
 * a file per backpack, in the world's {@code data/deconstructedbackpacks}; the client has those the server sent it.
 */
public final class BackpackStorage {
	private static final StorageContentsManager CONTENTS = StorageContentsManager.create(DeconstructedBackpacks.MOD_ID);

	private BackpackStorage() {}

	/** The backpack's contents, the same live ones for every stack and block with its UUID. */
	public static StorageContents getContents(UUID backpackUuid) {
		return CONTENTS.getOrCreate(backpackUuid);
	}

	public static NBTTagCompound getOrCreateBackpackContents(UUID backpackUuid) {
		return getContents(backpackUuid).getNbt();
	}

	/** The client's copy gets a backpack's contents as the server sends them. */
	public static void setClientBackpackContents(UUID backpackUuid, NBTTagCompound contents) {
		CONTENTS.setClientContents(backpackUuid, contents);
	}

	public static void removeBackpackContents(UUID backpackUuid) {
		CONTENTS.remove(backpackUuid);
	}
}
