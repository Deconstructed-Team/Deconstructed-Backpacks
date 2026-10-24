package dev.feldsher.deconstructedbackpacks.backpack;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The contents of every backpack, by its UUID. The backpack stack carries only the UUID.
 * The server's copy is saved with the world; the client has one of its own that only the open container fills.
 */
@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID)
public class BackpackStorage extends WorldSavedData {
	private static final String SAVED_DATA_NAME = DeconstructedBackpacks.MOD_ID;

	private final Map<UUID, NBTTagCompound> backpackContents = new HashMap<>();
	private static final BackpackStorage clientStorageCopy = new BackpackStorage();

	public BackpackStorage() {
		super(SAVED_DATA_NAME);
	}

	// WorldSavedData is instantiated by name
	@SuppressWarnings("unused")
	public BackpackStorage(String name) {
		super(name);
	}

	public static BackpackStorage get() {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			if (server != null) {
				// The map storage is shared by every dimension
				World overworld = server.getWorld(0);
				MapStorage storage = overworld.getMapStorage();
				//noinspection ConstantConditions - only null on a world that has none, never the server's overworld
				BackpackStorage backpackStorage = (BackpackStorage) storage.getOrLoadData(BackpackStorage.class, SAVED_DATA_NAME);
				if (backpackStorage == null) {
					backpackStorage = new BackpackStorage();
					storage.setData(SAVED_DATA_NAME, backpackStorage);
				}
				return backpackStorage;
			}
		}
		return clientStorageCopy;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		backpackContents.clear();
		NBTTagList contentsList = nbt.getTagList("backpackContents", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < contentsList.tagCount(); i++) {
			NBTTagCompound uuidContentsPair = contentsList.getCompoundTagAt(i);
			UUID uuid = uuidContentsPair.getUniqueId("uuid");
			if (uuid != null) {
				backpackContents.put(uuid, uuidContentsPair.getCompoundTag("contents"));
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList backpackContentsNbt = new NBTTagList();
		for (Map.Entry<UUID, NBTTagCompound> entry : backpackContents.entrySet()) {
			NBTTagCompound uuidContentsPair = new NBTTagCompound();
			uuidContentsPair.setUniqueId("uuid", entry.getKey());
			uuidContentsPair.setTag("contents", entry.getValue());
			backpackContentsNbt.appendTag(uuidContentsPair);
		}
		compound.setTag("backpackContents", backpackContentsNbt);
		return compound;
	}

	public NBTTagCompound getOrCreateBackpackContents(UUID backpackUuid) {
		return backpackContents.computeIfAbsent(backpackUuid, uuid -> {
			markDirty();
			return new NBTTagCompound();
		});
	}

	public void removeBackpackContents(UUID backpackUuid) {
		backpackContents.remove(backpackUuid);
		markDirty();
	}

	@SubscribeEvent
	public static void onClientWorldLoad(WorldEvent.Load evt) {
		if (evt.getWorld().isRemote) {
			clientStorageCopy.backpackContents.clear();
		}
	}
}
