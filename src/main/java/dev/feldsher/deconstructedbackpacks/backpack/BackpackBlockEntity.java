package dev.feldsher.deconstructedbackpacks.backpack;

import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedcore.upgrades.ITickableUpgrade;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

/**
 * A placed backpack: it holds the backpack stack, and through it the contents.
 * Fluid and energy handlers, the storage controller and render state for tanks and battery come with
 * the stages that port them.
 */
public class BackpackBlockEntity extends TileEntity implements ITickable {
	@Nullable
	private IBackpackWrapper backpackWrapper = null;

	public void setBackpack(ItemStack backpack) {
		backpackWrapper = BackpackWrapper.fromStack(backpack);
		backpackWrapper.setContentsChangeHandler(() -> {
			markDirty();
			notifyBlockUpdate();
		});
		backpackWrapper.setInventorySlotChangeHandler(this::markDirty);
	}

	public IBackpackWrapper getBackpackWrapper() {
		if (backpackWrapper == null) {
			setBackpack(new ItemStack(getBlockType() instanceof BackpackBlock ? ((BackpackBlock) getBlockType()).getBackpackItem() : net.minecraft.init.Items.AIR));
		}
		return backpackWrapper;
	}

	public ItemStack getBackpack() {
		return getBackpackWrapper().getBackpack();
	}

	private void notifyBlockUpdate() {
		if (world != null) {
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	@Override
	public void update() {
		if (world == null || world.isRemote || backpackWrapper == null) {
			return;
		}
		backpackWrapper.getUpgradeHandler().getWrappersThatImplement(ITickableUpgrade.class).forEach(upgrade -> upgrade.tick(null, world, pos));
		// What it shows changes with its contents, which hoppers and other stacks with them change as well
		if (world.getTotalWorldTime() % BackpackItem.RENDER_INFO_REFRESH_TICKS == 0) {
			backpackWrapper.refreshRenderInfo();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		setBackpackFromNbt(compound);
	}

	private void setBackpackFromNbt(NBTTagCompound nbt) {
		setBackpack(new ItemStack(nbt.getCompoundTag("backpackData")));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		writeBackpack(compound);
		return compound;
	}

	private void writeBackpack(NBTTagCompound ret) {
		if (backpackWrapper != null) {
			ret.setTag("backpackData", backpackWrapper.getBackpack().writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeBackpack(tag);
		return new SPacketUpdateTileEntity(pos, 0, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		setBackpackFromNbt(pkt.getNbtCompound());
		if (world != null) {
			world.markBlockRangeForRenderUpdate(pos, pos);
		}
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				|| capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && getBackpackWrapper().getFluidHandler().isPresent()
				|| super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getBackpackWrapper().getInventoryForInputOutput());
		}
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			IFluidHandler fluidHandler = getBackpackWrapper().getFluidHandler().orElse(null);
			return fluidHandler == null ? null : CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
		}
		return super.getCapability(capability, facing);
	}
}
