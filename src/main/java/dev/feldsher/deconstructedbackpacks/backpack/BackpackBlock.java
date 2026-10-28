package dev.feldsher.deconstructedbackpacks.backpack;

import dev.feldsher.deconstructedcore.client.render.UpgradeRenderRegistry;
import java.util.Random;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import dev.feldsher.deconstructedcore.renderdata.RenderInfo;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraft.block.properties.IProperty;
import dev.feldsher.deconstructedcore.api.IStorageFluidHandler;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import dev.feldsher.deconstructedcore.util.FluidContainerHelper;
import java.util.Optional;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedcore.upgrades.jukebox.ServerStorageSoundHandler;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.Style;
import dev.feldsher.deconstructedcore.upgrades.infinity.InfinityUpgradeItem;
import dev.feldsher.deconstructedbackpacks.upgrades.everlasting.EverlastingUpgradeItem;
import net.minecraft.init.Blocks;
import net.minecraft.world.Explosion;
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
import net.minecraft.entity.Entity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * A placed backpack, one block per tier. It drops itself with its contents, as its item.
 * Waterlogging has no 1.12 counterpart; tanks, battery, upgrade particles and pickup of item entities come with the
 * stages that port them.
 */
public class BackpackBlock extends Block {
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	/** What the backpack shows, for its model: tanks and battery in place of pouches, the fluids, the charge. */
	public static final IUnlistedProperty<RenderInfo> RENDER_INFO = new IUnlistedProperty<RenderInfo>() {
		@Override
		public String getName() {
			return "render_info";
		}

		@Override
		public boolean isValid(RenderInfo value) {
			return value != null;
		}

		@Override
		public Class<RenderInfo> getType() {
			return RenderInfo.class;
		}

		@Override
		public String valueToString(RenderInfo value) {
			return value.serialize().toString();
		}
	};

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
		return new ExtendedBlockState(this, new IProperty[] {FACING}, new IUnlistedProperty[] {RENDER_INFO});
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (state instanceof IExtendedBlockState && te instanceof BackpackBlockEntity) {
			return ((IExtendedBlockState) state).withProperty(RENDER_INFO, ((BackpackBlockEntity) te).getBackpackWrapper().getRenderInfo());
		}
		return state;
	}

	/** What its working upgrades show, as a furnace's flames, from the middle of its back face. */
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof BackpackBlockEntity) || Minecraft.getMinecraft().isGamePaused()) {
			return;
		}
		float yRot = (float) Math.toRadians(state.getValue(FACING).getHorizontalAngle());
		UpgradeRenderRegistry.render(((BackpackBlockEntity) te).getBackpackWrapper().getRenderInfo().getUpgradeRenderData(), world, rand,
				offset -> offset.add(0, 0, 0.41).rotateYaw(-yRot).add(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
	}

	/** The tanks' glass lets through what's behind it. */
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
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
		return BackpackShapes.getEnvelope(source, pos, state);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
			@Nullable Entity entity, boolean isActualState) {
		for (AxisAlignedBB box : BackpackShapes.getShape(world, pos, state)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, box);
		}
	}

	/** Aimed at only where one of the shape's boxes is, not anywhere in the box around them. */
	@Nullable
	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		RayTraceResult closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (AxisAlignedBB box : BackpackShapes.getShape(world, pos, state)) {
			RayTraceResult result = rayTrace(pos, start, end, box);
			if (result != null) {
				double distance = result.hitVec.squareDistanceTo(start);
				if (distance < closestDistance) {
					closest = result;
					closestDistance = distance;
				}
			}
		}
		return closest;
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

	/** With the everlasting upgrade, explosions don't break it, as they don't break bedrock. */
	@Override
	public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof BackpackBlockEntity && ((BackpackBlockEntity) te).getBackpackWrapper().getUpgradeHandler().hasUpgrade(EverlastingUpgradeItem.TYPE)) {
			return Blocks.BEDROCK.getExplosionResistance(null);
		}
		return super.getExplosionResistance(world, pos, exploder, explosion);
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
			if (lacksInfinityPermission(world, pos, player)) {
				player.sendStatusMessage(new TextComponentTranslation("gui.deconstructedbackpacks.status.infinity_upgrade_only_admin_pickup")
						.setStyle(new Style().setColor(TextFormatting.RED)), true);
				return true;
			}
			putInPlayersHandAndRemove(state, world, pos, player);
			return true;
		}

		if (tryFluidInteraction(world, pos, player)) {
			return true;
		}

		new BackpackContext.Block(pos).open(player);
		return true;
	}

	/** A fluid container in hand is emptied into the backpack's tanks, or else filled from them, one of the stack. */
	private static boolean tryFluidInteraction(World world, BlockPos pos, EntityPlayer player) {
		ItemStack heldItem = player.getHeldItemMainhand();
		TileEntity te = world.getTileEntity(pos);
		if (heldItem.isEmpty() || FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(heldItem, 1)) == null || !(te instanceof BackpackBlockEntity)) {
			return false;
		}
		Optional<IStorageFluidHandler> fluidHandler = ((BackpackBlockEntity) te).getBackpackWrapper().getFluidHandler();
		if (!fluidHandler.isPresent()) {
			return false;
		}
		FluidActionResult result = FluidContainerHelper.emptyContainer(heldItem, fluidHandler.get(), Integer.MAX_VALUE, true);
		if (!result.isSuccess()) {
			result = FluidContainerHelper.fillContainer(heldItem, fluidHandler.get(), Integer.MAX_VALUE, true);
		}
		if (result.isSuccess()) {
			FluidContainerHelper.settleResult(player, heldItem, result.getResult(), stack -> player.setHeldItem(EnumHand.MAIN_HAND, stack));
		}
		return true;
	}

	/** An infinity upgrade's backpack is picked up and broken only by players with its permission level. */
	public static boolean lacksInfinityPermission(World world, BlockPos pos, EntityPlayer player) {
		TileEntity te = world.getTileEntity(pos);
		return te instanceof BackpackBlockEntity && ((BackpackBlockEntity) te).getBackpackWrapper().getUpgradeHandler().getTypeWrappers(InfinityUpgradeItem.TYPE)
				.stream().anyMatch(w -> !InfinityUpgradeItem.hasPermission(player, w.getPermissionLevel()));
	}

	private static void putInPlayersHandAndRemove(IBlockState state, World world, BlockPos pos, EntityPlayer player) {
		TileEntity te = world.getTileEntity(pos);
		ItemStack backpack = te instanceof BackpackBlockEntity ? ((BackpackBlockEntity) te).getBackpack() : ItemStack.EMPTY;

		BackpackWrapper.fromStack(backpack).getContentsUuid()
				.ifPresent(uuid -> ServerStorageSoundHandler.stopPlayingDisc(world, new Vec3d(pos).add(0.5, 0.5, 0.5), uuid));
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
