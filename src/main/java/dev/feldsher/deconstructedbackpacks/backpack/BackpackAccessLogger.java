package dev.feldsher.deconstructedbackpacks.backpack;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The last opening of each backpack, kept with the world (in {@code data/deconstructedbackpacks_access_log.dat}),
 * for the commands that list backpacks and give them back.
 */
public class BackpackAccessLogger {
	private static final int REFRESH_INTERVAL_SECONDS = 30;

	private static long lastCacheRefresh = 0;
	private static final Map<String, Set<AccessLogRecord>> playerLogCache = new HashMap<>();

	private BackpackAccessLogger() {}

	public static void logPlayerAccess(EntityPlayer player, Item backpackItem, UUID backpackUuid, String backpackName, int clothColor, int trimColor, int columnsTaken) {
		if (player.world.isRemote) {
			return;
		}
		//noinspection ConstantConditions - at this point the registry name of item exists for sure otherwise the player wouldn't be able to open the backpack
		getData().ifPresent(data -> data.put(new AccessLogRecord(backpackItem.getRegistryName(), backpackUuid, player.getGameProfile().getName(), backpackName,
				clothColor, trimColor, System.currentTimeMillis(), columnsTaken)));
	}

	public static Set<String> getPlayerNames() {
		initPlayerBackpackCache();
		return playerLogCache.keySet();
	}

	public static Collection<AccessLogRecord> getBackpackLogsForPlayer(String playerName) {
		initPlayerBackpackCache();
		return playerLogCache.getOrDefault(playerName, Collections.emptySet());
	}

	private static void initPlayerBackpackCache() {
		if (lastCacheRefresh + (REFRESH_INTERVAL_SECONDS * 1000) >= System.currentTimeMillis()) {
			return;
		}
		lastCacheRefresh = System.currentTimeMillis();

		playerLogCache.clear();
		getAccessLogs().values().forEach(alr -> playerLogCache.computeIfAbsent(alr.getPlayerName(), name -> new HashSet<>()).add(alr));
	}

	public static Collection<AccessLogRecord> getAllBackpackLogs() {
		return getAccessLogs().values();
	}

	public static Set<UUID> getBackpackUuids() {
		return getAccessLogs().keySet();
	}

	public static Optional<AccessLogRecord> getBackpackLog(UUID backpackUuid) {
		return Optional.ofNullable(getAccessLogs().get(backpackUuid));
	}

	private static Map<UUID, AccessLogRecord> getAccessLogs() {
		return getData().map(data -> data.records).orElse(Collections.emptyMap());
	}

	/** The overworld's: the same for every dimension, and gone with the world it was loaded for. */
	private static Optional<AccessLogData> getData() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		World overworld = server == null ? null : server.getWorld(0);
		if (overworld == null) {
			return Optional.empty();
		}
		MapStorage storage = overworld.getMapStorage();
		if (storage == null) {
			return Optional.empty();
		}
		AccessLogData data = (AccessLogData) storage.getOrLoadData(AccessLogData.class, AccessLogData.NAME);
		if (data == null) {
			data = new AccessLogData(AccessLogData.NAME);
			storage.setData(AccessLogData.NAME, data);
			lastCacheRefresh = 0;
		}
		return Optional.of(data);
	}

	public static class AccessLogData extends WorldSavedData {
		private static final String NAME = DeconstructedBackpacks.MOD_ID + "_access_log";
		private final Map<UUID, AccessLogRecord> records = new HashMap<>();

		public AccessLogData(String name) {
			super(name);
		}

		private void put(AccessLogRecord alr) {
			records.put(alr.getBackpackUuid(), alr);
			markDirty();
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			records.clear();
			for (NBTBase n : nbt.getTagList("accessLogRecords", Constants.NBT.TAG_COMPOUND)) {
				AccessLogRecord alr = AccessLogRecord.deserializeFromNBT((NBTTagCompound) n);
				records.put(alr.getBackpackUuid(), alr);
			}
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			NBTTagList accessLogsNbt = new NBTTagList();
			for (AccessLogRecord alr : records.values()) {
				accessLogsNbt.appendTag(alr.serializeToNBT());
			}
			nbt.setTag("accessLogRecords", accessLogsNbt);
			return nbt;
		}
	}
}
