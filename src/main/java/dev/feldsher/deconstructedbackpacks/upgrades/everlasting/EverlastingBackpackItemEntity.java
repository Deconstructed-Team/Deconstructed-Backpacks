package dev.feldsher.deconstructedbackpacks.upgrades.everlasting;

import dev.feldsher.deconstructedbackpacks.backpack.FireResistantItemEntity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * A dropped backpack with the everlasting upgrade: it never despawns, nothing hurts it, and from water, lava or below
 * the world it floats up and then stays where it stopped.
 */
public class EverlastingBackpackItemEntity extends FireResistantItemEntity {
	private static final double FLOAT_UP_ACCELERATION = 0.06;
	private static final double MAX_FLOAT_UP_SPEED = 0.7;
	private static final double GRAVITY = 0.04;
	private static final double HANGING_PICKUP_RANGE = 4;
	private boolean wasFloatingUp = false;

	@SuppressWarnings("unused") // as an entity's, for loading
	public EverlastingBackpackItemEntity(World world) {
		super(world);
		makeEverlasting();
	}

	public EverlastingBackpackItemEntity(World world, EntityItem original, ItemStack stack) {
		super(world, original, stack);
		makeEverlasting();
	}

	private void makeEverlasting() {
		lifespan = Integer.MAX_VALUE;
		setEntityInvulnerable(true);
	}

	@Override
	public void onUpdate() {
		if (!world.isRemote && rand.nextInt(20) == 0) {
			((WorldServer) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, posX + 0.5 - rand.nextFloat(), posY + rand.nextFloat() * 0.5, posZ + 0.5 - rand.nextFloat(),
					0, 0, 0.1, 0, 1.0, new int[0]);
		}
		if (!hasNoGravity()) {
			if (isInWater() || isInLava() || posY < 1) {
				// Up as a bubble column carries it, the gravity applied after this undone
				motionY = Math.min(MAX_FLOAT_UP_SPEED, motionY + FLOAT_UP_ACCELERATION) + GRAVITY;
				wasFloatingUp = true;
			} else if (wasFloatingUp) {
				setNoGravity(true);
				motionX = 0;
				motionY = 0;
				motionZ = 0;
			}
		}
		super.onUpdate();
		if (!world.isRemote && !isDead && hasNoGravity()) {
			pickUpByNearbyPlayers();
		}
	}

	/** Left hanging where it floated up to, as over the void, it's hard to reach: players pick it up from further away. */
	private void pickUpByNearbyPlayers() {
		for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox().grow(HANGING_PICKUP_RANGE))) {
			if (isDead) {
				return;
			}
			onCollideWithPlayer(player);
		}
	}

	@Override
	public boolean isImmuneToExplosions() {
		return true;
	}

	@Override
	protected void outOfWorld() {
		//noop: vanilla removes it, and it floats up instead
	}
}
