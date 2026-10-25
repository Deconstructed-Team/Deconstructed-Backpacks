package dev.feldsher.deconstructedbackpacks.backpack;

import net.minecraft.util.math.Vec3d;
import dev.feldsher.deconstructedcore.upgrades.jukebox.ServerStorageSoundHandler;
import dev.feldsher.deconstructedbackpacks.upgrades.everlasting.EverlastingUpgradeItem;
import dev.feldsher.deconstructedbackpacks.upgrades.everlasting.EverlastingBackpackItemEntity;
import dev.feldsher.deconstructedbackpacks.util.InventoryInteractionHelper;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContainer;
import dev.feldsher.deconstructedbackpacks.common.gui.BackpackContext;
import dev.feldsher.deconstructedbackpacks.common.gui.GuiHandler;
import dev.feldsher.deconstructedbackpacks.init.ModBlocks;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import dev.feldsher.deconstructedcore.upgrades.ITickableUpgrade;
import dev.feldsher.deconstructedcore.util.StorageNbt;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * A backpack: a storage carried as an item, opened from the hand.
 * Stashing items into it from a slot comes with the
 * stages that port them.
 */
public class BackpackItem extends Item {
	/** How often a backpack's stack, carried or placed, catches up with what its contents show. */
	public static final int RENDER_INFO_REFRESH_TICKS = 10;
	private final IntSupplier numberOfSlots;
	private final IntSupplier numberOfUpgradeSlots;
	private boolean fireResistant = false;

	public BackpackItem(String name, IntSupplier numberOfSlots, IntSupplier numberOfUpgradeSlots) {
		this.numberOfSlots = numberOfSlots;
		this.numberOfUpgradeSlots = numberOfUpgradeSlots;
		setRegistryName(DeconstructedBackpacks.getRL(name));
		setTranslationKey(DeconstructedBackpacks.MOD_ID + "." + name);
		setMaxStackSize(1);
		setCreativeTab(ModItems.CREATIVE_TAB);
	}

	/** As 1.21's fire resistant items: dropped, it doesn't burn in fire or lava. */
	public BackpackItem setFireResistant() {
		fireResistant = true;
		return this;
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}

	/** Dropped with the everlasting upgrade, it's indestructible; fire resistant ones don't burn. */
	@Nullable
	@Override
	public Entity createEntity(World world, Entity location, ItemStack stack) {
		if (!(location instanceof EntityItem)) {
			return null;
		}
		if (hasEverlastingUpgrade(stack)) {
			return new EverlastingBackpackItemEntity(world, (EntityItem) location, stack);
		}
		return fireResistant ? new FireResistantItemEntity(world, (EntityItem) location, stack) : null;
	}

	private static boolean hasEverlastingUpgrade(ItemStack stack) {
		return BackpackWrapper.fromStack(stack).getUpgradeHandler().hasUpgrade(EverlastingUpgradeItem.TYPE);
	}

	public static void setColors(ItemStack backpackStack, int mainColor, int accentColor) {
		StorageNbt.getOrCreateTag(backpackStack).setInteger(StorageNbt.MAIN_COLOR, mainColor);
		StorageNbt.getOrCreateTag(backpackStack).setInteger(StorageNbt.ACCENT_COLOR, accentColor);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (!isInCreativeTab(tab)) {
			return;
		}
		items.add(new ItemStack(this));

		if (this != ModItems.BACKPACK) {
			return;
		}

		for (EnumDyeColor color : EnumDyeColor.values()) {
			ItemStack stack = new ItemStack(this);
			setColors(stack, color.getColorValue(), color.getColorValue());
			items.add(stack);
		}
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		super.addInformation(stack, world, tooltip, flag);
		if (flag.isAdvanced()) {
			BackpackWrapper.fromStack(stack).getContentsUuid()
					.ifPresent(uuid -> tooltip.add(TextFormatting.DARK_GRAY + "UUID: " + uuid));
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);

		if (!world.isRemote) {
			int slot = hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : GuiHandler.OFFHAND_SLOT;
			new BackpackContext.Item(slot).open(player);
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	/**
	 * Sneaking, a backpack runs its inventory interaction upgrades on the inventory clicked; with none to run, it's
	 * placed as a block facing the player.
	 */
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!player.isSneaking()) {
			return EnumActionResult.PASS;
		}
		if (InventoryInteractionHelper.tryInventoryInteraction(pos, world, player.getHeldItem(hand), facing, player)) {
			return EnumActionResult.SUCCESS;
		}

		IBlockState clickedState = world.getBlockState(pos);
		BlockPos placePos = clickedState.getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing);
		ItemStack backpack = player.getHeldItem(hand);
		BackpackBlock block = getBlock();
		if (!player.canPlayerEdit(placePos, facing, backpack) || !world.mayPlace(block, placePos, false, facing, player)) {
			return EnumActionResult.FAIL;
		}

		IBlockState placementState = block.getDefaultState().withProperty(BackpackBlock.FACING, player.getHorizontalFacing().getOpposite());
		if (!world.setBlockState(placePos, placementState, 11)) {
			return EnumActionResult.FAIL;
		}

		if (!world.isRemote) {
			stopBackpackSounds(backpack, world, placePos);
		}
		TileEntity te = world.getTileEntity(placePos);
		if (te instanceof BackpackBlockEntity) {
			((BackpackBlockEntity) te).setBackpack(getBackpackCopy(player, backpack));
			te.markDirty();
		}

		SoundType soundtype = placementState.getBlock().getSoundType(placementState, world, placePos, player);
		world.playSound(player, placePos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
		if (!player.isCreative()) {
			backpack.shrink(1);
		}
		return EnumActionResult.SUCCESS;
	}

	/** The disc it played following the player stops: it plays from where it's placed once it's played again. */
	private static void stopBackpackSounds(ItemStack backpack, World world, BlockPos pos) {
		BackpackWrapper.fromStack(backpack).getContentsUuid()
				.ifPresent(uuid -> ServerStorageSoundHandler.stopPlayingDisc(world, new Vec3d(pos).add(0.5, 0.5, 0.5), uuid));
	}

	/** In creative the player keeps the backpack, so the placed one is a copy with contents of its own. */
	private ItemStack getBackpackCopy(EntityPlayer player, ItemStack backpack) {
		if (!player.isCreative()) {
			return backpack.copy();
		}
		return BackpackWrapper.fromStack(backpack).cloneBackpack();
	}

	public BackpackBlock getBlock() {
		return ModBlocks.getBlock(this);
	}

	/** The upgrades of every backpack a player carries tick, wherever in the inventory it is. */
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (world.isRemote || !(entity instanceof EntityPlayer)) {
			return;
		}
		EntityPlayer player = (EntityPlayer) entity;
		if (player.isSpectator() || player.isDead) {
			return;
		}
		IBackpackWrapper wrapper = BackpackWrapper.fromStack(stack);
		// A backpack made into this tier other than by our recipes, as on Future MC's smithing table, keeps the slot
		// counts of the one it was made from; so does one whose tier the config has grown since
		wrapper.ensureSlotNumbersAtLeast(getNumberOfSlots(), getNumberOfUpgradeSlots());
		wrapper.getUpgradeHandler().getWrappersThatImplement(ITickableUpgrade.class)
				.forEach(upgrade -> upgrade.tick(player, world, player.getPosition()));
		// Another stack with these contents, or an automation, may have changed what this one shows
		if (world.getTotalWorldTime() % RENDER_INFO_REFRESH_TICKS == 0) {
			wrapper.refreshRenderInfo();
		}
	}

	public int getNumberOfSlots() {
		return numberOfSlots.getAsInt();
	}

	public int getNumberOfUpgradeSlots() {
		return numberOfUpgradeSlots.getAsInt();
	}

	@Nullable
	@Override
	public EntityEquipmentSlot getEquipmentSlot(ItemStack stack) {
		return EntityEquipmentSlot.CHEST;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged;
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
		return !(player.openContainer instanceof BackpackContainer && ((BackpackContainer) player.openContainer).getStorageWrapper().getBackpack() == item);
	}
}
