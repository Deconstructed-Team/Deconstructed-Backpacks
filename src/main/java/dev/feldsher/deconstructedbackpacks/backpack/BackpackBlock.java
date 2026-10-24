package dev.feldsher.deconstructedbackpacks.backpack;

import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContext;
import dev.feldsher.deconstructedcore.util.StorageNbt;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * A placed backpack, one block per tier. It drops itself with its contents, as its item.
 * Waterlogging has no 1.12 counterpart; tanks, battery, upgrade particles and pickup of item entities come with the
 * stages that port them.
 */
public class BackpackBlock extends Block {
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	// BackpackShapes' body, side pouches and front pouch facing north, as the one box 1.12 selects with
	private static final AxisAlignedBB[] SHAPES = new AxisAlignedBB[4];

	static {
		double minX = 0.75, minY = 0, minZ = 4, maxX = 15.25, maxY = 14.5, maxZ = 13.25;
		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			// The blockstate turns the model by the facing's y rotation, clockwise seen from above: (x, z) -> (16 - z, x)
			double x1 = minX, z1 = minZ, x2 = maxX, z2 = maxZ;
			for (int i = 0; i < (facing.getHorizontalIndex() + 2) % 4; i++) {
				double nx1 = 16 - z2, nz1 = x1, nx2 = 16 - z1, nz2 = x2;
				x1 = nx1;
				z1 = nz1;
				x2 = nx2;
				z2 = nz2;
			}
			SHAPES[facing.getHorizontalIndex()] = new AxisAlignedBB(x1 / 16, minY / 16, z1 / 16, x2 / 16, maxY / 16, z2 / 16);
		}
	}

	private final Supplier<BackpackItem> backpackItem;

	public BackpackBlock(String name, Supplier<BackpackItem> backpackItem) {
		super(Material.CLOTH);
		this.backpackItem = backpackItem;
		setRegistryName(DeconstructedBackpacks.getRL(name));
		setTranslationKey(DeconstructedBackpacks.MOD_ID + "." + name);
		setHardness(0.8F);
		setResistance(0.8F);
		setSoundType(SoundType.CLOTH);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	public BackpackItem getBackpackItem() {
		return backpackItem.get();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		return state.withRotation(mirror.toRotation(state.getValue(FACING)));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return SHAPES[state.getValue(FACING).getHorizontalIndex()];
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new BackpackBlockEntity();
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof BackpackBlockEntity)) {
			return 0;
		}
		IItemHandler handler = ((BackpackBlockEntity) te).getBackpackWrapper().getInventoryHandler();
		return ItemHandlerHelper.calcRedstoneFromInventory(handler);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}
		if (hand != EnumHand.MAIN_HAND) {
			return false;
		}

		if (player.isSneaking() && player.getHeldItemMainhand().isEmpty()) {
			putInPlayersHandAndRemove(state, world, pos, player);
			return true;
		}

		new BackpackContext.Block(pos).open(player);
		return true;
	}

	private static void putInPlayersHandAndRemove(IBlockState state, World world, BlockPos pos, EntityPlayer player) {
		TileEntity te = world.getTileEntity(pos);
		ItemStack backpack = te instanceof BackpackBlockEntity ? ((BackpackBlockEntity) te).getBackpack() : ItemStack.EMPTY;

		player.setHeldItem(EnumHand.MAIN_HAND, backpack.copy());
		player.getCooldownTracker().setCooldown(backpack.getItem(), 5);
		world.removeTileEntity(pos);
		world.setBlockToAir(pos);

		SoundType soundType = state.getBlock().getSoundType(state, world, pos, player);
		world.playSound(null, pos, soundType.getBreakSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
	}

	// The backpack drops in breakBlock, from its tile entity, however the block goes
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		//noop
	}

	/** The same backpack without its contents: a copy with its UUID would share them, and the client doesn't have them anyway. */
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof BackpackBlockEntity)) {
			return new ItemStack(getBackpackItem());
		}
		ItemStack backpack = ((BackpackBlockEntity) te).getBackpack().copy();
		StorageNbt.removeStorageUuid(backpack);
		return backpack;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof BackpackBlockEntity && !world.isRemote) {
			spawnAsEntity(world, pos, ((BackpackBlockEntity) te).getBackpack().copy());
		}
		super.breakBlock(world, pos, state);
	}
}
