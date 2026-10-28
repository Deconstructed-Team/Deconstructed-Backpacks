package dev.feldsher.deconstructedbackpacks.client;

import dev.feldsher.deconstructedcore.client.render.StorageContentsTooltip;
import dev.feldsher.deconstructedbackpacks.client.render.BackpackContentsTooltipSource;
import dev.feldsher.deconstructedbackpacks.compat.ae2.Ae2Compat;
import dev.feldsher.deconstructedbackpacks.compat.ae2.TerminalStashExtension;
import dev.feldsher.deconstructedbackpacks.CommonProxy;
import dev.feldsher.deconstructedbackpacks.client.gui.BackpackScreen;
import dev.feldsher.deconstructedbackpacks.client.init.ModUpgradeTabs;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackBlockEntity;
import dev.feldsher.deconstructedbackpacks.client.render.BackpackBlockEntityRenderer;
import dev.feldsher.deconstructedbackpacks.client.render.BackpackLayerRenderer;
import dev.feldsher.deconstructedbackpacks.client.gui.BackpackSettingsScreen;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContainer;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackSettingsContainerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;

@SuppressWarnings("unused") // instantiated by @SidedProxy
public class ClientProxy extends CommonProxy {
	@Override
	public void init() {
		KeybindHandler.register();
		ClientRegistry.bindTileEntitySpecialRenderer(BackpackBlockEntity.class, new BackpackBlockEntityRenderer());
		ModUpgradeTabs.register();
		StorageContentsTooltip.registerSource(new BackpackContentsTooltipSource());
		if (Ae2Compat.isAvailable()) {
			TerminalStashExtension.register();
		}
	}

	@Override
	public void postInit() {
		BackpackLayerRenderer.addToRenderers(Minecraft.getMinecraft().getRenderManager());
	}

	@Override
	public Object createBackpackScreen(BackpackContainer container) {
		return new BackpackScreen(container);
	}

	@Override
	public Object createBackpackSettingsScreen(BackpackSettingsContainerMenu container) {
		return new BackpackSettingsScreen(container);
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().world;
	}
}
