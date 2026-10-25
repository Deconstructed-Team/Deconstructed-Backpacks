package dev.feldsher.deconstructedbackpacks.init;

import dev.feldsher.deconstructedbackpacks.upgrades.everlasting.EverlastingBackpackItemEntity;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.FireResistantItemEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID)
public class ModEntities {
	private ModEntities() {}

	@SubscribeEvent
	public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
		// Tracked as vanilla tracks item entities
		event.getRegistry().register(EntityEntryBuilder.create()
				.entity(FireResistantItemEntity.class)
				.id(DeconstructedBackpacks.getRL("fire_resistant_item"), 0)
				.name(DeconstructedBackpacks.MOD_ID + ".fire_resistant_item")
				.tracker(64, 20, true)
				.build());
		event.getRegistry().register(EntityEntryBuilder.create()
				.entity(EverlastingBackpackItemEntity.class)
				.id(DeconstructedBackpacks.getRL("everlasting_backpack_item"), 1)
				.name(DeconstructedBackpacks.MOD_ID + ".everlasting_backpack_item")
				.tracker(64, 20, true)
				.build());
	}
}
