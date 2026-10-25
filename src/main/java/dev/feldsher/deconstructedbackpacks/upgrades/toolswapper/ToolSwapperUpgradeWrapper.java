package dev.feldsher.deconstructedbackpacks.upgrades.toolswapper;

import com.google.common.collect.Multimap;
import dev.feldsher.deconstructedbackpacks.api.IAttackEntityResponseUpgrade;
import dev.feldsher.deconstructedbackpacks.api.IBlockClickResponseUpgrade;
import dev.feldsher.deconstructedbackpacks.api.IBlockToolSwapUpgrade;
import dev.feldsher.deconstructedbackpacks.api.IEntityToolSwapUpgrade;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.registry.tool.SwordRegistry;
import dev.feldsher.deconstructedbackpacks.registry.tool.ToolRegistry;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.inventory.InventoryHandler;
import dev.feldsher.deconstructedcore.upgrades.FilterLogic;
import dev.feldsher.deconstructedcore.upgrades.UpgradeWrapperBase;
import dev.feldsher.deconstructedcore.util.InventoryHelper;
import dev.feldsher.deconstructedcore.util.StorageNbt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * On a block the player starts mining, swaps in the backpack's fastest tool that can harvest it, unless the hand
 * already holds one; on an entity attacked, its best sword, or else axe. The swapped out item goes into the backpack.
 * On the advanced one's key, it swaps in a tool that works on the block or entity looked at, going round all of them
 * that do on each press.
 */
public class ToolSwapperUpgradeWrapper extends UpgradeWrapperBase<ToolSwapperUpgradeWrapper, ToolSwapperUpgradeItem>
		implements IBlockClickResponseUpgrade, IAttackEntityResponseUpgrade, IBlockToolSwapUpgrade, IEntityToolSwapUpgrade {
	private static final String SHOULD_SWAP_WEAPON_TAG = "shouldSwapWeapon";
	private static final String TOOL_SWAP_MODE_TAG = "toolSwapMode";
	private static final double MIN_GOOD_DESTROY_SPEED = 1.5;

	private final FilterLogic filterLogic;
	@Nullable
	private String toolCacheFor = null;
	private final Queue<ItemStack> toolCache = new ArrayDeque<>();

	private Block lastMinedBlock = Blocks.AIR;

	public ToolSwapperUpgradeWrapper(IStorageWrapper backpackWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(backpackWrapper, upgrade, upgradeSaveHandler);
		filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount(), "filter_attributes");
	}

	@Override
	public boolean onBlockClick(EntityPlayer player, BlockPos pos) {
		ToolSwapMode toolSwapMode = getToolSwapMode();
		if (player.isCreative() || player.isSpectator() || toolSwapMode == ToolSwapMode.NO_SWAP) {
			return false;
		}

		ItemStack mainHandItem = player.getHeldItemMainhand();
		if (mainHandItem.getItem() instanceof BackpackItem || (toolSwapMode == ToolSwapMode.ONLY_TOOLS && isSword(mainHandItem))
				|| (!isSword(mainHandItem) && isNotTool(mainHandItem)) || !filterLogic.matchesFilter(mainHandItem)) {
			return false;
		}

		World world = player.world;
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		double mainToolSpeed = 0;
		if (isGoodAtBreakingBlock(world, pos, state, mainHandItem)) {
			if (lastMinedBlock == block) {
				return true;
			}
			mainToolSpeed = mainHandItem.getDestroySpeed(state);
		}

		lastMinedBlock = block;

		return tryToSwapTool(player, world, pos, state, mainToolSpeed, mainHandItem);
	}

	private boolean tryToSwapTool(EntityPlayer player, World world, BlockPos pos, IBlockState state, double mainHandItemSpeed, ItemStack mainHandItem) {
		InventoryHandler backpackInventory = storageWrapper.getInventoryHandler();
		int selectedSlot = -1;
		ItemStack selectedTool = ItemStack.EMPTY;
		double bestSpeed = mainHandItemSpeed;
		for (int slot = 0; slot < backpackInventory.getSlots(); slot++) {
			ItemStack stack = backpackInventory.getStackInSlot(slot);
			if (!stack.isEmpty() && filterLogic.matchesFilter(stack) && isGoodAtBreakingBlock(world, pos, state, stack)) {
				float destroySpeed = stack.getDestroySpeed(state);
				if (bestSpeed < destroySpeed) {
					bestSpeed = destroySpeed;
					selectedSlot = slot;
					selectedTool = stack;
				}
			}
		}
		if (!selectedTool.isEmpty() && hasSpaceInBackpackOrCanPlaceInTheSlotOfSwappedTool(backpackInventory, mainHandItem, selectedTool, selectedSlot)) {
			player.setHeldItem(EnumHand.MAIN_HAND, backpackInventory.extractItem(selectedSlot, 1, false));
			backpackInventory.insertItem(mainHandItem, false);
			return true;
		}
		return false;
	}

	private boolean hasSpaceInBackpackOrCanPlaceInTheSlotOfSwappedTool(InventoryHandler backpackInventory, ItemStack mainHandItem, ItemStack tool, int selectedSlot) {
		return backpackInventory.insertItem(mainHandItem, true).isEmpty()
				|| (tool.getCount() == 1 && backpackInventory.isItemValid(selectedSlot, mainHandItem));
	}

	/** Fast at it, and of a tool class and harvest level that harvests it, or an item that harvests it as shears do cobweb. */
	private static boolean isGoodAtBreakingBlock(World world, BlockPos pos, IBlockState state, ItemStack stack) {
		return stack.getDestroySpeed(state) > MIN_GOOD_DESTROY_SPEED && (ForgeHooks.canToolHarvestBlock(world, pos, stack) || stack.canHarvestBlock(state));
	}

	@Override
	public boolean onAttackEntity(EntityPlayer player) {
		if (!shouldSwapWeapon()) {
			return false;
		}

		ItemStack mainHandItem = player.getHeldItemMainhand();
		if (isSword(mainHandItem)) {
			return true;
		}
		if (mainHandItem.getItem() instanceof BackpackItem || isNotTool(mainHandItem) || !filterLogic.matchesFilter(mainHandItem)) {
			return false;
		}
		return tryToSwapInWeapon(player, mainHandItem);
	}

	private static boolean isNotTool(ItemStack stack) {
		return stack.isEmpty() || stack.getItem().getToolClasses(stack).isEmpty() && !(stack.getItem() instanceof ItemHoe) && !isShearsItem(stack);
	}

	private static boolean isSword(ItemStack stack) {
		return SwordRegistry.isSword(stack);
	}

	private boolean tryToSwapInWeapon(EntityPlayer player, ItemStack mainHandItem) {
		BestWeapons bestWeapons = new BestWeapons();
		bestWeapons.update(mainHandItem);

		InventoryHandler backpackInventory = storageWrapper.getInventoryForUpgradeProcessing();
		InventoryHelper.iterate(backpackInventory, (slot, stack) -> {
			if (filterLogic.matchesFilter(stack)) {
				bestWeapons.update(stack);
			}
		});

		if (!bestWeapons.sword.isEmpty()) {
			return swapWeapon(player, mainHandItem, backpackInventory, bestWeapons.sword);
		} else if (!bestWeapons.axe.isEmpty()) {
			return swapWeapon(player, mainHandItem, backpackInventory, bestWeapons.axe);
		}
		return false;
	}

	/** The swords and axes with the most attack damage. */
	private static class BestWeapons {
		private ItemStack axe = ItemStack.EMPTY;
		private double axeDamage = 0;
		private ItemStack sword = ItemStack.EMPTY;
		private double swordDamage = 0;

		private void update(ItemStack stack) {
			if (stack.isEmpty()) {
				return;
			}
			double damageValue = getAttackDamage(stack);
			if (stack.getItem().getToolClasses(stack).contains("axe")) {
				if (damageValue > axeDamage) {
					axe = stack;
					axeDamage = damageValue;
				}
			} else if (isSword(stack) && damageValue > swordDamage) {
				sword = stack;
				swordDamage = damageValue;
			}
		}

		private static double getAttackDamage(ItemStack stack) {
			IAttributeInstance attribute = new AttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
			Multimap<String, AttributeModifier> modifiers = stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
			for (AttributeModifier modifier : modifiers.get(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
				attribute.removeModifier(modifier);
				attribute.applyModifier(modifier);
			}
			return attribute.getAttributeValue();
		}
	}

	private boolean swapWeapon(EntityPlayer player, ItemStack mainHandItem, InventoryHandler backpackInventory, ItemStack weapon) {
		if (weapon == mainHandItem) {
			return true;
		}

		ItemStack weaponCopy = ItemHandlerHelper.copyStackWithSize(weapon, 1);
		InventoryHelper.extractFromInventory(weaponCopy, backpackInventory, false);
		if (backpackInventory.insertItem(mainHandItem, true).isEmpty()) {
			player.setHeldItem(EnumHand.MAIN_HAND, weaponCopy);
			backpackInventory.insertItem(mainHandItem, false);
			return true;
		}
		backpackInventory.insertItem(weaponCopy, false);
		return false;
	}

	@Override
	public boolean hideSettingsTab() {
		return !upgradeItem.hasSettingsTab();
	}

	public FilterLogic getFilterLogic() {
		return filterLogic;
	}

	public boolean shouldSwapWeapon() {
		NBTTagCompound tag = upgrade.getTagCompound();
		return tag == null || !tag.hasKey(SHOULD_SWAP_WEAPON_TAG) || tag.getBoolean(SHOULD_SWAP_WEAPON_TAG);
	}

	public void setSwapWeapon(boolean shouldSwapWeapon) {
		StorageNbt.getOrCreateTag(upgrade).setBoolean(SHOULD_SWAP_WEAPON_TAG, shouldSwapWeapon);
		save();
	}

	public ToolSwapMode getToolSwapMode() {
		NBTTagCompound tag = upgrade.getTagCompound();
		return tag == null ? ToolSwapMode.ANY : ToolSwapMode.fromName(tag.getString(TOOL_SWAP_MODE_TAG));
	}

	public void setToolSwapMode(ToolSwapMode toolSwapMode) {
		StorageNbt.getOrCreateTag(upgrade).setString(TOOL_SWAP_MODE_TAG, toolSwapMode.getName());
		save();
	}

	@Override
	public boolean onEntityInteract(World world, Entity entity, EntityPlayer player) {
		if (!upgradeItem.shouldSwapToolOnKeyPress()) {
			return false;
		}
		ResourceLocation entityName = EntityList.getKey(entity);
		return tryToSwapTool(player, stack -> itemWorksOnEntity(stack, entity), entityName == null ? entity.getClass().getName() : entityName.toString());
	}

	private static boolean itemWorksOnEntity(ItemStack stack, Entity entity) {
		if (isShearsItem(stack) && entity instanceof IShearable && ((IShearable) entity).isShearable(stack, entity.world, entity.getPosition())) {
			return true;
		}
		return ToolRegistry.isToolForEntity(stack, entity);
	}

	@Override
	public boolean onBlockInteract(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!upgradeItem.shouldSwapToolOnKeyPress()) {
			return false;
		}
		ResourceLocation blockName = state.getBlock().getRegistryName();
		return tryToSwapTool(player, stack -> itemWorksOnBlock(world, pos, state, stack), blockName == null ? null : blockName.toString());
	}

	/**
	 * Swaps in a tool that works on the target, the first one not given for it before; when all have been, the one
	 * given longest ago, so that pressing again goes round them. What the hand held goes into the backpack, or to the
	 * player when it doesn't fit.
	 */
	private boolean tryToSwapTool(EntityPlayer player, Predicate<ItemStack> isToolValid, @Nullable String target) {
		ItemStack mainHandStack = player.getHeldItemMainhand();
		if (mainHandStack.getItem() instanceof BackpackItem) {
			return false;
		}
		if (toolCacheFor == null || !toolCacheFor.equals(target)) {
			toolCache.clear();
			toolCacheFor = target;
		}

		InventoryHandler backpackInventory = storageWrapper.getInventoryForUpgradeProcessing();
		if (isToolValid.test(mainHandStack) && toolCache.stream().noneMatch(st -> ItemStack.areItemsEqual(st, mainHandStack))) {
			toolCache.offer(mainHandStack);
		}
		ItemStack tool = findToolToSwap(backpackInventory, isToolValid);
		if (tool.isEmpty()) {
			return false;
		}

		tool = ItemHandlerHelper.copyStackWithSize(tool, 1);
		player.setHeldItem(EnumHand.MAIN_HAND, InventoryHelper.extractFromInventory(tool, backpackInventory, false));
		ItemStack remaining = backpackInventory.insertItem(mainHandStack, false);
		if (!remaining.isEmpty()) {
			ItemHandlerHelper.giveItemToPlayer(player, remaining);
		}
		toolCache.offer(tool);
		return true;
	}

	private ItemStack findToolToSwap(InventoryHandler backpackInventory, Predicate<ItemStack> isValidTool) {
		Set<ItemStack> alreadyGivenBefore = new HashSet<>();
		ItemStack toolFound = ItemStack.EMPTY;
		for (int slot = 0; slot < backpackInventory.getSlots() && toolFound.isEmpty(); slot++) {
			ItemStack stack = backpackInventory.getStackInSlot(slot);
			if (stack.isEmpty()) {
				continue;
			}
			if (!hasEquivalentItem(toolCache, stack)) {
				if (isValidTool.test(stack)) {
					toolFound = stack;
				}
			} else {
				alreadyGivenBefore.add(stack);
			}
		}

		if (toolFound.isEmpty() && !alreadyGivenBefore.isEmpty()) {
			while (toolCache.peek() != null) {
				ItemStack itemStack = toolCache.poll();
				if (hasEquivalentItem(alreadyGivenBefore, itemStack)) {
					toolFound = itemStack;
					break;
				}
			}
		}
		return toolFound;
	}

	private static boolean hasEquivalentItem(Collection<ItemStack> stacks, ItemStack stack) {
		for (ItemStack givenTool : stacks) {
			if (ItemStack.areItemsEqual(givenTool, stack)) {
				return true;
			}
		}
		return false;
	}

	/** A shovel or hoe on the grass or dirt it turns, shears on what they shear, or a tool the registry gives the block. */
	private static boolean itemWorksOnBlock(World world, BlockPos pos, IBlockState state, ItemStack stack) {
		Block block = state.getBlock();
		boolean airAbove = world.isAirBlock(pos.up());
		if (airAbove && block == Blocks.GRASS && stack.getItem().getToolClasses(stack).contains("shovel")) {
			return true;
		}
		if (airAbove && stack.getItem() instanceof ItemHoe && (block == Blocks.GRASS || block == Blocks.GRASS_PATH
				|| block == Blocks.DIRT && state.getValue(BlockDirt.VARIANT) != BlockDirt.DirtType.PODZOL)) {
			return true;
		}
		if (isShearsItem(stack) && block instanceof IShearable && ((IShearable) block).isShearable(stack, world, pos)) {
			return true;
		}
		return ToolRegistry.isToolForBlock(stack, world, state, pos);
	}

	private static boolean isShearsItem(ItemStack stack) {
		return stack.getItem() instanceof ItemShears;
	}

	@Override
	public boolean canProcessBlockInteract() {
		return upgradeItem.shouldSwapToolOnKeyPress();
	}

	@Override
	public boolean canProcessEntityInteract() {
		return upgradeItem.shouldSwapToolOnKeyPress();
	}
}
