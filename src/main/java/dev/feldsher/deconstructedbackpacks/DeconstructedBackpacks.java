package dev.feldsher.deconstructedbackpacks;

import dev.feldsher.deconstructedbackpacks.compat.baubles.BaublesCompat;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import dev.feldsher.deconstructedbackpacks.command.DbpCommand;
import net.minecraftforge.fml.common.Loader;
import dev.feldsher.deconstructedbackpacks.registry.RegistryLoader;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContainer;
import dev.feldsher.deconstructedbackpacks.common.gui.GuiHandler;
import dev.feldsher.deconstructedbackpacks.compat.FutureMcCompat;
import dev.feldsher.deconstructedbackpacks.crafting.OreDependentRecipes;
import dev.feldsher.deconstructedbackpacks.init.ModUpgradeContainers;
import dev.feldsher.deconstructedbackpacks.network.PacketHandler;
import dev.feldsher.deconstructedcore.compat.bogosorter.BogoSorterCompat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = DeconstructedBackpacks.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
		dependencies = "required-after:deconstructedcore")
public class DeconstructedBackpacks {
	public static final String MOD_ID = Tags.MOD_ID;
	public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

	@Mod.Instance(MOD_ID)
	public static DeconstructedBackpacks instance;

	@SidedProxy(clientSide = "dev.feldsher.deconstructedbackpacks.client.ClientProxy", serverSide = "dev.feldsher.deconstructedbackpacks.CommonProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LOGGER.info("{} {} loading", Tags.MOD_NAME, Tags.VERSION);
		PacketHandler.init();
		BaublesCompat.init();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
		ModUpgradeContainers.register();
		BogoSorterCompat.registerStorageContainers(BackpackContainer.class);
		proxy.init();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit();
		// Every mod has registered its ores by now
		OreDependentRecipes.removeRecipesWithoutOres();
		FutureMcCompat.addSmithingRecipes();
	}

	/** The registry data, read again for each world, so that changes to it apply without a restart. */
	@Mod.EventHandler
	public void serverAboutToStart(FMLServerAboutToStartEvent event) {
		RegistryLoader.load(Loader.instance().getConfigDir());
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new DbpCommand());
	}

	public static ResourceLocation getRL(String regName) {
		return new ResourceLocation(MOD_ID, regName);
	}
}
