package dev.feldsher.deconstructedbackpacks.common;

import javax.annotation.Nullable;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.block.state.IBlockState;
import dev.feldsher.deconstructedbackpacks.Config;
import dev.feldsher.deconstructedbackpacks.DeconstructedBackpacks;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackStorage;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import dev.feldsher.deconstructedbackpacks.init.ModItems;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.upgrades.jukebox.JukeboxUpgradeItem;
import dev.feldsher.deconstructedcore.upgrades.jukebox.JukeboxUpgradeWrapper;
import dev.feldsher.deconstructedcore.util.InventoryHelper;
import dev.feldsher.deconstructedcore.util.RandHelper;
import dev.feldsher.deconstructedcore.util.WeightedElement;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Monsters that spawn with a backpack: the better its tier, the more loot it holds and the better the armor, potion
 * effects and health its wearer gets. Some play a jukebox upgrade with a disc in it. A player who kills the mob may get
 * the backpack; else its contents go with the mob.
 */
public class EntityBackpackAdditionHandler {
	private static final int MAX_DIFFICULTY = 3;
	private static final float MAX_LOCAL_DIFFICULTY = 6.75f;

	private static final String SPAWNED_WITH_BACKPACK = "spawnedWithBackpack";
	private static final Set<Entity> TRAVELLING = Collections.newSetFromMap(new WeakHashMap<>());
	private static final String SPAWNED_WITH_JUKEBOX_UPGRADE = DeconstructedBackpacks.MOD_ID + ":jukebox";
	private static final UUID HEALTH_BONUS_ID = UUID.fromString("9ebd2a3e-1d6d-4a73-9c5d-4f6c2e0b8a71");

	// netherite armor where Future MC adds it, else diamond in its place
	private static final List<WeightedElement<Supplier<Item>>> HELMET_CHANCES = armorChances("netherite_helmet", Items.DIAMOND_HELMET, Items.GOLDEN_HELMET, Items.IRON_HELMET, Items.LEATHER_HELMET);
	private static final List<WeightedElement<Supplier<Item>>> LEGGINGS_CHANCES = armorChances("netherite_leggings", Items.DIAMOND_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.IRON_LEGGINGS, Items.LEATHER_LEGGINGS);
	private static final List<WeightedElement<Supplier<Item>>> BOOTS_CHANCES = armorChances("netherite_boots", Items.DIAMOND_BOOTS, Items.GOLDEN_BOOTS, Items.IRON_BOOTS, Items.LEATHER_BOOTS);

	private static final List<WeightedElement<BackpackAddition>> BACKPACK_CHANCES = Arrays.asList(
			new WeightedElement<>(1, new BackpackAddition(ModItems.NETHERITE_BACKPACK, 4, 0, 0, 1)),
			new WeightedElement<>(5, new BackpackAddition(ModItems.DIAMOND_BACKPACK, 3, 0, 0, 2)),
			new WeightedElement<>(25, new BackpackAddition(ModItems.GOLD_BACKPACK, 2, 1, 1, 2)),
			new WeightedElement<>(125, new BackpackAddition(ModItems.IRON_BACKPACK, 1, 2, 2, 2)),
			new WeightedElement<>(250, new BackpackAddition(ModItems.COPPER_BACKPACK, 1, 2, 3, 2)),
			new WeightedElement<>(625, new BackpackAddition(ModItems.BACKPACK, 0, 3, 3, 2))
	);

	private static final List<List<WeightedElement<BackpackAddition>>> DIFFICULTY_BACKPACK_CHANCES = Arrays.asList(
			BACKPACK_CHANCES,
			BACKPACK_CHANCES.subList(1, 5),
			BACKPACK_CHANCES.subList(2, 5)
	);

	private static final Map<Item, Float> DROP_CHANCE_MULTIPLIER = new HashMap<>();

	static {
		DROP_CHANCE_MULTIPLIER.put(ModItems.BACKPACK, 1F);
		DROP_CHANCE_MULTIPLIER.put(ModItems.COPPER_BACKPACK, 1.25F);
		DROP_CHANCE_MULTIPLIER.put(ModItems.IRON_BACKPACK, 1.5F);
		DROP_CHANCE_MULTIPLIER.put(ModItems.GOLD_BACKPACK, 3F);
		DROP_CHANCE_MULTIPLIER.put(ModItems.DIAMOND_BACKPACK, 4.5F);
		DROP_CHANCE_MULTIPLIER.put(ModItems.NETHERITE_BACKPACK, 6F);
	}

	private static final List<ApplicableEffect> APPLICABLE_EFFECTS = Arrays.asList(
			new ApplicableEffect(Arrays.asList(MobEffects.RESISTANCE, MobEffects.REGENERATION), 1),
			new ApplicableEffect(MobEffects.FIRE_RESISTANCE),
			new ApplicableEffect(MobEffects.ABSORPTION),
			new ApplicableEffect(MobEffects.HEALTH_BOOST),
			new ApplicableEffect(MobEffects.SPEED),
			new ApplicableEffect(MobEffects.STRENGTH));

	private static List<Item> musicDiscs = null;

	private EntityBackpackAdditionHandler() {}

	private static List<WeightedElement<Supplier<Item>>> armorChances(String futureMcNetherite, Item diamond, Item golden, Item iron, Item leather) {
		Supplier<Item> netherite = () -> {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("futuremc", futureMcNetherite));
			return item != null ? item : diamond;
		};
		return Arrays.asList(
				new WeightedElement<>(1, netherite),
				new WeightedElement<>(3, () -> diamond),
				new WeightedElement<>(9, () -> golden),
				new WeightedElement<>(27, () -> iron),
				new WeightedElement<>(81, () -> leather)
		);
	}

	static void addBackpack(EntityMob monster, World world) {
		Config.EntityBackpackAdditionsConfig config = Config.SERVER.entityBackpackAdditions;
		Random rnd = world.rand;
		ResourceLocation entityName = EntityList.getKey(monster);
		if (entityName == null || !config.canWearBackpack(entityName) || rnd.nextDouble() >= config.chance) {
			return;
		}

		BlockPos pos = new BlockPos(monster);
		float localDifficulty = world.getDifficultyForLocation(pos).getAdditionalDifficulty();
		int index = MathHelper.clamp((int) Math.floor(DIFFICULTY_BACKPACK_CHANCES.size() / MAX_LOCAL_DIFFICULTY * localDifficulty - 0.1f), 0, DIFFICULTY_BACKPACK_CHANCES.size() - 1);
		List<WeightedElement<BackpackAddition>> chances = DIFFICULTY_BACKPACK_CHANCES.get(index).stream()
				.filter(chance -> ModItems.getBackpacks().contains(chance.getElement().backpackItem)).collect(Collectors.toList());

		RandHelper.getRandomWeightedElement(rnd, chances).ifPresent(backpackAddition -> {
			ItemStack backpack = new ItemStack(backpackAddition.backpackItem);
			int minDifficulty = backpackAddition.minDifficulty;
			int difficulty = Math.max(minDifficulty, rnd.nextInt(MAX_DIFFICULTY + 1));
			equipBackpack(monster, entityName, backpack, difficulty, config.playJukebox && rnd.nextInt(4) == 0, rnd);
			applyPotions(monster, difficulty, minDifficulty, rnd);
			raiseHealth(monster, minDifficulty);
			if (config.equipWithArmor) {
				equipArmorPiece(monster, world, pos, rnd, minDifficulty, backpackAddition.getChances(HELMET_CHANCES, backpackAddition.helmetFrom), EntityEquipmentSlot.HEAD);
				equipArmorPiece(monster, world, pos, rnd, minDifficulty, backpackAddition.getChances(LEGGINGS_CHANCES, backpackAddition.legsAndBootsFrom), EntityEquipmentSlot.LEGS);
				equipArmorPiece(monster, world, pos, rnd, minDifficulty, backpackAddition.getChances(BOOTS_CHANCES, backpackAddition.legsAndBootsFrom), EntityEquipmentSlot.FEET);
			}
			monster.addTag(SPAWNED_WITH_BACKPACK);
		});
	}

	private static void equipArmorPiece(EntityMob monster, World world, BlockPos pos, Random rnd, int minDifficulty, List<WeightedElement<Supplier<Item>>> armorChances, EntityEquipmentSlot slot) {
		RandHelper.getRandomWeightedElement(rnd, armorChances).ifPresent(armorPiece -> {
			ItemStack armorStack = new ItemStack(armorPiece.get());
			if (rnd.nextInt(6 - minDifficulty) == 0) {
				float additionalDifficulty = world.getDifficultyForLocation(pos).getClampedAdditionalDifficulty();
				int enchantmentLevel = (int) (5F + additionalDifficulty * 18F + minDifficulty * 6);
				armorStack = EnchantmentHelper.addRandomEnchantment(rnd, armorStack, enchantmentLevel, false);
			}
			monster.setItemStackToSlot(slot, armorStack);
		});
	}

	private static void equipBackpack(EntityMob monster, ResourceLocation entityName, ItemStack backpack, int difficulty, boolean playMusicDisc, Random rnd) {
		EntityList.EntityEggInfo egg = EntityList.ENTITY_EGGS.get(entityName);
		if (egg != null) {
			IBackpackWrapper wrapper = BackpackWrapper.fromStack(backpack);
			wrapper.setColors(egg.primaryColor | 0xFF_000000, egg.secondaryColor | 0xFF_000000);
			if (Config.SERVER.entityBackpackAdditions.addLoot) {
				addLoot(entityName, wrapper, difficulty);
			}
			if (playMusicDisc) {
				wrapper.getInventoryHandler(); //just to assign uuid and real upgrade handler
				if (wrapper.getUpgradeHandler().getSlots() > 0 && !getMusicDiscs().isEmpty()) {
					monster.addTag(SPAWNED_WITH_JUKEBOX_UPGRADE);
					addJukeboxUpgradeAndRandomDisc(wrapper, rnd);
				}
			}
		}
		monster.setItemStackToSlot(EntityEquipmentSlot.CHEST, backpack);
		monster.setDropChance(EntityEquipmentSlot.CHEST, 0);
	}

	private static void addJukeboxUpgradeAndRandomDisc(IStorageWrapper w, Random rnd) {
		boolean advancedJukebox = rnd.nextFloat() < 0.25;
		w.getUpgradeHandler().setStackInSlot(0, new ItemStack(advancedJukebox ? ModItems.ADVANCED_JUKEBOX_UPGRADE : ModItems.JUKEBOX_UPGRADE));
		List<JukeboxUpgradeWrapper> wrappers = w.getUpgradeHandler().getTypeWrappers(JukeboxUpgradeItem.TYPE);
		if (!wrappers.isEmpty()) {
			JukeboxUpgradeWrapper wrapper = wrappers.get(0);
			int numberOfDiscs = advancedJukebox ? rnd.nextInt(Math.max(1, wrapper.getDiscInventory().getSlots() / 3)) + 1 : 1;
			List<Item> discs = getMusicDiscs();
			for (int i = 0; i < numberOfDiscs; i++) {
				wrapper.getDiscInventory().insertItem(i, new ItemStack(discs.get(rnd.nextInt(discs.size()))), false);
			}
		}
	}

	private static List<Item> getMusicDiscs() {
		if (musicDiscs == null) {
			musicDiscs = new ArrayList<>();
			for (Item item : ForgeRegistries.ITEMS) {
				//noinspection ConstantConditions - registered items have registry names
				if (item instanceof ItemRecord && !Config.SERVER.entityBackpackAdditions.isDiscBlocked(item.getRegistryName())) {
					musicDiscs.add(item);
				}
			}
			musicDiscs = Collections.unmodifiableList(musicDiscs);
		}
		return musicDiscs;
	}

	private static void raiseHealth(EntityMob monster, int minDifficulty) {
		if (!Config.SERVER.entityBackpackAdditions.buffHealth) {
			return;
		}
		IAttributeInstance maxHealth = monster.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		double healthAddition = maxHealth.getBaseValue() * minDifficulty;
		if (healthAddition > 0.1D && maxHealth.getModifier(HEALTH_BONUS_ID) == null) {
			maxHealth.applyModifier(new AttributeModifier(HEALTH_BONUS_ID, DeconstructedBackpacks.MOD_ID + ":backpack_bearer_health_bonus", healthAddition, 0));
		}
		monster.setHealth(monster.getMaxHealth());
	}

	private static void applyPotions(EntityMob monster, int difficulty, int minDifficulty, Random rnd) {
		if (Config.SERVER.entityBackpackAdditions.buffWithPotionEffects) {
			RandHelper.getNRandomElements(APPLICABLE_EFFECTS, difficulty + 2, rnd).forEach(applicableEffect -> {
				int amplifier = Math.min(Math.max(minDifficulty, rnd.nextInt(difficulty + 1)), applicableEffect.maxAmplifier);
				monster.addPotionEffect(new PotionEffect(applicableEffect.getRandomEffect(rnd), 30 * 60 * 20, amplifier));
			});
		}
	}

	private static void addLoot(ResourceLocation entityName, IBackpackWrapper backpackWrapper, int difficulty) {
		if (difficulty != 0) {
			Config.SERVER.entityBackpackAdditions.getLootTableName(entityName).ifPresent(lootTableName -> {
				float lootFactor = (float) difficulty / MAX_DIFFICULTY;
				backpackWrapper.setLoot(lootTableName, lootFactor);
			});
		}
	}

	static void handleBackpackDrop(LivingDropsEvent event) {
		EntityLivingBase mob = event.getEntityLiving();
		if (!mob.getTags().contains(SPAWNED_WITH_BACKPACK)) {
			return;
		}
		ItemStack backpack = mob.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (!(backpack.getItem() instanceof BackpackItem)) {
			return;
		}
		if (shouldDropBackpack(event, mob, backpack)) {
			putJukeboxItemsInContainerAndRemoveStorageUuid(mob, backpack);

			EntityItem backpackEntity = new EntityItem(mob.world, mob.posX, mob.posY, mob.posZ, backpack);
			backpackEntity.setDefaultPickupDelay();
			event.getDrops().add(backpackEntity);
			mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemStack.EMPTY);
			mob.removeTag(SPAWNED_WITH_BACKPACK);
		} else {
			removeContentsUuid(backpack);
		}
	}

	/** The jukebox and its discs go in the dropped backpack's inventory as it's first opened, not to mob contents kept for it. */
	private static void putJukeboxItemsInContainerAndRemoveStorageUuid(EntityLivingBase mob, ItemStack backpack) {
		if (!mob.removeTag(SPAWNED_WITH_JUKEBOX_UPGRADE)) {
			return;
		}
		List<ItemStack> inventoryItems = new ArrayList<>();
		IBackpackWrapper backpackWrapper = BackpackWrapper.fromStack(backpack);
		backpackWrapper.getUpgradeHandler().getTypeWrappers(JukeboxUpgradeItem.TYPE).forEach(wrapper -> {
			wrapper.stop(mob);
			InventoryHelper.iterate(wrapper.getDiscInventory(), (slot, stack) -> {
				if (!stack.isEmpty()) {
					inventoryItems.add(wrapper.getDiscInventory().extractItem(slot, stack.getCount(), false));
				}
			});
		});
		InventoryHelper.iterate(backpackWrapper.getUpgradeHandler(), (slot, stack) -> {
			if (!stack.isEmpty()) {
				inventoryItems.add(backpackWrapper.getUpgradeHandler().extractItem(slot, stack.getCount(), false));
			}
		});
		backpackWrapper.removeContentsUuid();
		backpackWrapper.setExtraItems(inventoryItems);
	}

	private static boolean shouldDropBackpack(LivingDropsEvent event, EntityLivingBase mob, ItemStack backpack) {
		Config.EntityBackpackAdditionsConfig config = Config.SERVER.entityBackpackAdditions;
		Entity killer = event.getSource().getTrueSource();
		if (!(killer instanceof EntityPlayer) || (!config.dropToFakePlayers && killer instanceof FakePlayer)) {
			return false;
		}
		float lootingChanceMultiplier = DROP_CHANCE_MULTIPLIER.getOrDefault(backpack.getItem(), 1F);
		return mob.world.rand.nextFloat() < (config.backpackDropChance + event.getLootingLevel() * config.lootingChanceIncreasePerLevel) * lootingChanceMultiplier;
	}

	/** Its potion effects would linger in the cloud it leaves. */
	static void removeBeneficialEffects(EntityCreeper creeper) {
		if (creeper.getTags().contains(SPAWNED_WITH_BACKPACK)) {
			new ArrayList<>(creeper.getActivePotionEffects()).stream().map(PotionEffect::getPotion).filter(potion -> !potion.isBadEffect())
					.forEach(creeper::removePotionEffect);
		}
	}

	/** A mob going to another dimension is removed here, and goes on there as a copy, with the same backpack. */
	static void onTravelToDimension(Entity entity) {
		if (!entity.world.isRemote && entity.getTags().contains(SPAWNED_WITH_BACKPACK)) {
			TRAVELLING.add(entity);
		}
	}

	/**
	 * The contents go with the mob, as it despawns: not as its chunk unloads, the mob being saved with the backpack,
	 * nor as it goes to another dimension. Its death sees to them with its drops.
	 */
	static void removeBackpackUuid(Entity entity) {
		if (entity.world.isRemote || !(entity instanceof EntityMob) || !entity.getTags().contains(SPAWNED_WITH_BACKPACK)) {
			return;
		}
		if (!entity.isDead || TRAVELLING.remove(entity)) {
			return;
		}
		removeContentsUuid(((EntityMob) entity).getItemStackFromSlot(EntityEquipmentSlot.CHEST));
	}

	private static void removeContentsUuid(ItemStack stack) {
		if (stack.getItem() instanceof BackpackItem) {
			BackpackWrapper.fromStack(stack).getContentsUuid().ifPresent(BackpackStorage::removeBackpackContents);
		}
	}

	static void onLivingUpdate(EntityLivingBase entity) {
		if (entity.world.isRemote || !entity.getTags().contains(SPAWNED_WITH_JUKEBOX_UPGRADE)) {
			return;
		}
		ItemStack backpack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (!(backpack.getItem() instanceof BackpackItem)) {
			return;
		}
		IBackpackWrapper backpackWrapper = BackpackWrapper.fromStack(backpack);
		backpackWrapper.getUpgradeHandler().getTypeWrappers(JukeboxUpgradeItem.TYPE).forEach(wrapper -> {
			if (wrapper.isPlaying()) {
				wrapper.tick(entity, entity.world, entity.getPosition());
			} else {
				wrapper.play(entity);
			}
		});
		// the notes it plays with, as a carried backpack's are refreshed
		if (entity.world.getTotalWorldTime() % BackpackItem.RENDER_INFO_REFRESH_TICKS == 0) {
			backpackWrapper.refreshRenderInfo();
		}
	}

	/**
	 * What 1.21's entity leave event is to it: a mob gone from the world, despawned, killed or unloaded, takes its backpack's
	 * contents with it.
	 */
	static class RemovalListener implements IWorldEventListener {
		@Override
		public void onEntityRemoved(Entity entity) {
			removeBackpackUuid(entity);
		}

		@Override
		public void onEntityAdded(Entity entity) {}

		@Override
		public void notifyBlockUpdate(World world, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {}

		@Override
		public void notifyLightSet(BlockPos pos) {}

		@Override
		public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}

		@Override
		public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent sound, SoundCategory category, double x, double y, double z, float volume, float pitch) {}

		@Override
		public void playRecord(SoundEvent sound, BlockPos pos) {}

		@Override
		public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}

		@Override
		public void spawnParticle(int id, boolean ignoreRange, boolean minimiseParticleLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {}

		@Override
		public void broadcastSound(int soundID, BlockPos pos, int data) {}

		@Override
		public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {}

		@Override
		public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}
	}

	/** A tier, its rarity's minimum difficulty, and from where in the armor chances its wearer's pieces come. */
	private static class BackpackAddition {
		private final Item backpackItem;
		private final int minDifficulty;
		private final int helmetFrom;
		private final int legsAndBootsFrom;
		private final int armorKinds;

		private BackpackAddition(Item backpackItem, int minDifficulty, int helmetFrom, int legsAndBootsFrom, int armorKinds) {
			this.backpackItem = backpackItem;
			this.minDifficulty = minDifficulty;
			this.helmetFrom = helmetFrom;
			this.legsAndBootsFrom = legsAndBootsFrom;
			this.armorKinds = armorKinds;
		}

		private List<WeightedElement<Supplier<Item>>> getChances(List<WeightedElement<Supplier<Item>>> armorChances, int from) {
			return armorChances.subList(from, from + armorKinds);
		}
	}

	private static class ApplicableEffect {
		private final List<Potion> effects;
		private final int maxAmplifier;

		private ApplicableEffect(Potion effect) {
			this(Collections.singletonList(effect), Integer.MAX_VALUE);
		}

		private ApplicableEffect(List<Potion> effects, int maxAmplifier) {
			this.effects = effects;
			this.maxAmplifier = maxAmplifier;
		}

		private Potion getRandomEffect(Random random) {
			return effects.get(random.nextInt(effects.size()));
		}
	}
}
