package dev.feldsher.deconstructedbackpacks.registry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.registry.tool.SwordRegistry;
import dev.feldsher.deconstructedbackpacks.registry.tool.ToolRegistry;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The registry data: the mod's own files, then any in config/deconstructedbackpacks/registry, which add to them, as
 * 1.21's data packs do. A file goes to the loader of its name, or of its "type"; one with a "mod" is read only with
 * that mod loaded, and one "disabled" not at all. Read again each time a world starts.
 */
public class RegistryLoader {
	private static final String MOD_FILES_PATH = "/assets/" + DeconstructedBackpacks.MOD_ID + "/registry/";
	private static final String[] MOD_FILES = {"block_tools", "entity_tools", "swords", "compat/botania/block_tools"};
	private static final Map<String, IRegistryDataLoader> loaders = new HashMap<>();
	private static final Gson GSON = new Gson();

	static {
		registerParser(new ToolRegistry.BlockToolsLoader());
		registerParser(new ToolRegistry.EntityToolsLoader());
		registerParser(new SwordRegistry.SwordsLoader());
	}

	private RegistryLoader() {}

	public static void registerParser(IRegistryDataLoader parser) {
		loaders.put(parser.getName(), parser);
	}

	public static void load(File configDir) {
		loaders.values().forEach(IRegistryDataLoader::clear);
		for (String name : MOD_FILES) {
			try (InputStream stream = RegistryLoader.class.getResourceAsStream(MOD_FILES_PATH + name + ".json")) {
				if (stream != null) {
					loadRegistry(name, new InputStreamReader(stream, StandardCharsets.UTF_8));
				}
			}
			catch (IOException e) {
				DeconstructedBackpacks.LOGGER.error("Failed to read registry data {}", name, e);
			}
		}

		File[] files = new File(configDir, DeconstructedBackpacks.MOD_ID + "/registry").listFiles((dir, fileName) -> fileName.endsWith(".json"));
		if (files == null) {
			return;
		}
		Arrays.sort(files);
		for (File file : files) {
			try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
				loadRegistry(file.getName().substring(0, file.getName().length() - ".json".length()), reader);
			}
			catch (IOException e) {
				DeconstructedBackpacks.LOGGER.error("Failed to read registry data {}", file, e);
			}
		}
	}

	private static void loadRegistry(String name, Reader reader) {
		DeconstructedBackpacks.LOGGER.debug("Started loading registry data from {} ", name);
		JsonElement fullJson;
		try {
			fullJson = GSON.fromJson(reader, JsonElement.class);
		}
		catch (RuntimeException e) {
			DeconstructedBackpacks.LOGGER.error("Invalid json in registry data {}", name, e);
			return;
		}
		if (fullJson == null || !fullJson.isJsonObject()) {
			return;
		}
		JsonObject json = fullJson.getAsJsonObject();

		Optional<IRegistryDataLoader> loader = getLoader(name, json);
		if (!loader.isPresent()) {
			DeconstructedBackpacks.LOGGER.error("No loader defined for {}", name);
			return;
		}

		@Nullable String modId = JsonUtils.isString(json, "mod") ? JsonUtils.getString(json, "mod") : null;
		if (JsonUtils.getBoolean(json, "disabled", false) || (modId != null && !Loader.isModLoaded(modId))) {
			return;
		}

		try {
			loader.get().parse(json, modId);
			DeconstructedBackpacks.LOGGER.debug("Finished loading registry data for {}", name);
		}
		catch (RuntimeException e) {
			DeconstructedBackpacks.LOGGER.error("Caught exception while loading {}", name, e);
		}
	}

	private static Optional<IRegistryDataLoader> getLoader(String fileName, JsonObject json) {
		String parserName = JsonUtils.getString(json, "type", fileName);
		return Optional.ofNullable(loaders.get(parserName));
	}
}
