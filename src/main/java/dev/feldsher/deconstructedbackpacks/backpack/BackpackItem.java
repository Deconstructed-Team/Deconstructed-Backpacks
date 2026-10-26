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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
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

import dev.feldsher.deconstructedbackpacks.common.gui.BackpackSettingsContainerMenu;
import dev.feldsher.deconstructedbackpacks.network.BackpackContentsMessage;
import dev.feldsher.deconstructedbackpacks.network.PacketHandler;
import dev.feldsher.deconstructedbackpacks.network.RequestBackpackContentsMessage;
import dev.feldsher.deconstructedcore.api.IStashStorageItem;
import dev.feldsher.deconstructedcore.util.ColorHelper;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.InventoryHandler;
import dev.feldsher.deconstructedcore.settings.memory.MemorySettingsCategory;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * A backpack: a storage carried as an item, opened from the hand. Items can be stashed into it in any inventory
 * screen; to show which, the client asks for the contents of a backpack it hasn't opened.
 */
public class BackpackItem extends Item implements IStashStorageItem {
	private static final long CONTENTS_REQUEST_INTERVAL_MS = 5000;
	private static final Map<UUID, Long> CONTENTS_REQUEST_TIMES = new HashMap<>();
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
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
		return new BackpackItemCapabilities(stack);
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}

	/**
	 * Dropped with the everlasting upgrade, it's indestructible; fire resistant ones don't burn. Forge asks this as each
	 * dropped backpack joins the world, so that's also where a duplicate next to the original loses its contents.
	 */
	@Nullable
	@Override
	public Entity createEntity(World world, Entity location, ItemStack stack) {
		if (!(location instanceof EntityItem)) {
			return null;
		}
		if (!world.isRemote) {
			UUIDDeduplicator.dedupeBackpackItemEntityInArea((EntityItem) location);
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

		ItemStack stack = new ItemStack(this);
		setColors(stack, ColorHelper.calculateColor(BackpackWrapper.DEFAULT_MAIN_COLOR, BackpackWrapper.DEFAULT_MAIN_COLOR, Arrays.asList(EnumDyeColor.YELLOW, EnumDyeColor.LIME)),
				ColorHelper.calculateColor(BackpackWrapper.DEFAULT_ACCENT_COLOR, BackpackWrapper.DEFAULT_ACCENT_COLOR, Arrays.asList(EnumDyeColor.BLUE, EnumDyeColor.BLACK)));
		items.add(stack);
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
	public ItemStack stash(ItemStack storageStack, ItemStack stack, boolean simulate) {
		return BackpackWrapper.fromStack(storageStack).getInventoryForUpgradeProcessing().insertItem(stack, simulate);
	}

	@Override
	public StashResult getItemStashable(ItemStack storageStack, ItemStack stack) {
		IBackpackWrapper wrapper = BackpackWrapper.fromStack(storageStack);
		Optional<UUID> uuid = wrapper.getContentsUuid();
		// Without contents yet it's empty, once they're made; the client asks for those it hasn't got
		if (!uuid.isPresent() || FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && !hasClientContents(uuid.get())) {
			return StashResult.SPACE;
		}
		if (wrapper.getInventoryForUpgradeProcessing().insertItem(stack, true).getCount() == stack.getCount()) {
			return StashResult.NO_SPACE;
		}
		InventoryHandler inventory = wrapper.getInventoryHandler();
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			if (inventory.getStackInSlot(slot).getItem() == stack.getItem()) {
				return StashResult.MATCH_AND_SPACE;
			}
		}
		return wrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).matchesFilter(stack) ? StashResult.MATCH_AND_SPACE : StashResult.SPACE;
	}

	private static boolean hasClientContents(UUID uuid) {
		if (BackpackStorage.hasClientBackpackContents(uuid)) {
			return true;
		}
		long now = System.currentTimeMillis();
		Long lastRequest = CONTENTS_REQUEST_TIMES.get(uuid);
		if (lastRequest == null || now - lastRequest > CONTENTS_REQUEST_INTERVAL_MS) {
			CONTENTS_REQUEST_TIMES.put(uuid, now);
			PacketHandler.INSTANCE.sendToServer(new RequestBackpackContentsMessage(uuid));
		}
		return false;
	}

	/** The client sees the new contents, unless the backpack is open, where its container keeps them in step. */
	@Override
	public void onStashed(EntityPlayerMP player, ItemStack storageStack) {
		BackpackWrapper.fromStack(storageStack).getContentsUuid().ifPresent(uuid -> {
			if (!isOpen(player, uuid)) {
				PacketHandler.INSTANCE.sendTo(new BackpackContentsMessage(uuid, BackpackStorage.getOrCreateBackpackContents(uuid)), player);
			}
		});
	}

	private static boolean isOpen(EntityPlayer player, UUID uuid) {
		IStorageWrapper openWrapper = player.openContainer instanceof BackpackContainer ? ((BackpackContainer) player.openContainer).getStorageWrapper()
				: player.openContainer instanceof BackpackSettingsContainerMenu ? ((BackpackSettingsContainerMenu) player.openContainer).getStorageWrapper() : null;
		return openWrapper != null && openWrapper.getContentsUuid().map(uuid::equals).orElse(false);
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
		return !(player.openContainer instanceof BackpackContainer && ((BackpackContainer) player.openContainer).getStorageWrapper().getBackpack() == item);
	}
}
