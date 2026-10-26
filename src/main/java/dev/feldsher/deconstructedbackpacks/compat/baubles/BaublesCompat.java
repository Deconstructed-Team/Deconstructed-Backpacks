package dev.feldsher.deconstructedbackpacks.compat.baubles;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import baubles.api.cap.IBaublesItemHandler;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

/**
 * Backpacks worn in Baubles' body slot, as 1.21's go in the Curios back slot: their upgrades work as in the inventory,
 * they're drawn on the back, open with the key, and another player may open them as a worn one.
 */
public class BaublesCompat {
	private static final String MOD_ID = "baubles";

	private BaublesCompat() {}

	public static void init() {
		if (!Loader.isModLoaded(MOD_ID)) {
			return;
		}
		PlayerInventoryProvider.addExtraInventory(new BaublesInventory());
		MinecraftForge.EVENT_BUS.register(new CapabilityAttacher());
	}

	public static class CapabilityAttacher {
		@SubscribeEvent
		public void attachBaubleCapability(AttachCapabilitiesEvent<ItemStack> event) {
			if (event.getObject().getItem() instanceof BackpackItem) {
				event.addCapability(DeconstructedBackpacks.getRL("bauble"), BackpackBauble.PROVIDER);
			}
		}
	}

	private static class BackpackBauble implements IBauble {
		private static final BackpackBauble INSTANCE = new BackpackBauble();
		private static final ICapabilityProvider PROVIDER = new ICapabilityProvider() {
			@Override
			public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
				return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
			}

			@Nullable
			@Override
			public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
				return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE ? BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(INSTANCE) : null;
			}
		};

		@Override
		public BaubleType getBaubleType(ItemStack stack) {
			return BaubleType.BODY;
		}

		@Override
		public void onWornTick(ItemStack stack, EntityLivingBase wearer) {
			if (wearer instanceof EntityPlayer && stack.getItem() instanceof BackpackItem) {
				((BackpackItem) stack.getItem()).tickCarried(stack, (EntityPlayer) wearer);
			}
		}

		/** Baubles sends it to the wearer and those who see them only when it's changed, as when its look or upgrades do. */
		@Override
		public boolean willAutoSync(ItemStack stack, EntityLivingBase wearer) {
			return true;
		}
	}

	private static class BaublesInventory implements PlayerInventoryProvider.ExtraInventory {
		@Override
		public int getSlots(EntityPlayer player) {
			IBaublesItemHandler handler = BaublesApi.getBaublesHandler(player);
			return handler == null ? 0 : handler.getSlots();
		}

		@Override
		public ItemStack getStackInSlot(EntityPlayer player, int slot) {
			return BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
		}

		@Override
		public void setStackInSlot(EntityPlayer player, int slot, ItemStack stack) {
			BaublesApi.getBaublesHandler(player).setStackInSlot(slot, stack);
		}

		@Override
		public boolean isAccessibleByAnotherPlayer() {
			return true;
		}
	}
}
