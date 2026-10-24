package dev.feldsher.deconstructedbackpacks.common;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import dev.feldsher.deconstructedcore.upgrades.IPickupResponseUpgrade;
import dev.feldsher.deconstructedcore.upgrades.UpgradeHandler;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(modid = DeconstructedBackpacks.MOD_ID)
public class CommonEventHandler {
	private CommonEventHandler() {}

	/**
	 * An item entity the player walks over goes to their backpacks' pickup upgrades first. When they take any of it,
	 * the vanilla pickup is cancelled, so that the player doesn't also pick up what was left from before.
	 */
	@SubscribeEvent
	public static void onItemPickup(EntityItemPickupEvent event) {
		EntityItem itemEntity = event.getItem();
		if (itemEntity.getItem().isEmpty() || itemEntity.cannotPickup()) {
			return;
		}

		EntityPlayer player = event.getEntityPlayer();
		World world = player.world;
		AtomicReference<ItemStack> remainingStackSimulated = new AtomicReference<>(itemEntity.getItem().copy());
		PlayerInventoryProvider.runOnBackpacks(player, (backpack, slot) -> {
			remainingStackSimulated.set(runPickupOnPickupResponseUpgrades(world, null, BackpackWrapper.fromStack(backpack).getUpgradeHandler(), remainingStackSimulated.get(), true));
			return remainingStackSimulated.get().isEmpty();
		});

		if (remainingStackSimulated.get().getCount() != itemEntity.getItem().getCount()) {
			AtomicReference<ItemStack> remainingStack = new AtomicReference<>(itemEntity.getItem().copy());
			PlayerInventoryProvider.runOnBackpacks(player, (backpack, slot) -> {
				remainingStack.set(runPickupOnPickupResponseUpgrades(world, player, BackpackWrapper.fromStack(backpack).getUpgradeHandler(), remainingStack.get(), false));
				return remainingStack.get().isEmpty();
			});
			itemEntity.setItem(remainingStack.get());
			if (remainingStack.get().isEmpty()) {
				itemEntity.setDead();
			}
			event.setCanceled(true);
		}
	}

	public static ItemStack runPickupOnPickupResponseUpgrades(World world, @Nullable EntityPlayer player, UpgradeHandler upgradeHandler, ItemStack remainingStack, boolean simulate) {
		for (IPickupResponseUpgrade pickupUpgrade : upgradeHandler.getWrappersThatImplement(IPickupResponseUpgrade.class)) {
			int countBeforePickup = remainingStack.getCount();
			ItemStack before = remainingStack;
			remainingStack = pickupUpgrade.pickup(world, remainingStack, simulate);
			if (!simulate && player != null && remainingStack.getCount() != countBeforePickup) {
				world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
						(world.rand.nextFloat() - world.rand.nextFloat()) * 1.4F + 2.0F);
				player.addStat(StatList.getObjectsPickedUpStats(before.getItem()), countBeforePickup - remainingStack.getCount());
			}
			if (remainingStack.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}
		return remainingStack;
	}
}
