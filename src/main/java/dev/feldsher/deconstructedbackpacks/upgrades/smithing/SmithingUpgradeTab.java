package dev.feldsher.deconstructedbackpacks.upgrades.smithing;

import dev.feldsher.deconstructedbackpacks.client.gui.SBPTranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.StorageScreenBase;
import dev.feldsher.deconstructedcore.client.gui.UpgradeSettingsTab;
import dev.feldsher.deconstructedcore.client.gui.utils.Dimension;
import dev.feldsher.deconstructedcore.client.gui.utils.GuiHelper;
import dev.feldsher.deconstructedcore.client.gui.utils.Position;
import dev.feldsher.deconstructedcore.client.gui.utils.TextureBlitData;
import dev.feldsher.deconstructedcore.client.gui.utils.TranslationHelper;
import dev.feldsher.deconstructedcore.client.gui.utils.UV;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

/** The item and material slots, the result, and an armor stand wearing or holding the result. */
public class SmithingUpgradeTab extends UpgradeSettingsTab<SmithingUpgradeContainer> {
	private static final TextureBlitData ARROW = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(56, 221), new Dimension(14, 15));
	private static final TextureBlitData RED_CROSS = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(113, 216), new Dimension(15, 15));
	private static final byte ARMOR_STAND_SHOW_ARMS = 4;
	private static final byte ARMOR_STAND_NO_BASE_PLATE = 8;
	private static final float ARMOR_STAND_BODY_ROTATION = 30.0F;
	private static final float ARMOR_STAND_TILT = 25.0F;
	private static final int ARMOR_STAND_SCALE = 25;
	private final EntityArmorStand armorStandPreview;

	public SmithingUpgradeTab(SmithingUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen) {
		super(upgradeContainer, position, screen, SBPTranslationHelper.INSTANCE.translUpgrade("smithing"),
				new TextComponentTranslation(SBPTranslationHelper.INSTANCE.translUpgradeKey("smithing") + TranslationHelper.TOOLTIP_SUFFIX));
		openTabDimension = new Dimension(103, 100);

		armorStandPreview = new EntityArmorStand(minecraft.world);
		armorStandPreview.getDataManager().set(EntityArmorStand.STATUS, (byte) (ARMOR_STAND_SHOW_ARMS | ARMOR_STAND_NO_BASE_PLATE));
		updateArmorStandPreview();
		getContainer().setOnResultChangedHandler(this::updateArmorStandPreview);
	}

	private void updateArmorStandPreview() {
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			armorStandPreview.setItemStackToSlot(slot, ItemStack.EMPTY);
		}
		ItemStack stack = getContainer().getResultSlot().getStack();
		if (!stack.isEmpty()) {
			EntityEquipmentSlot slot = stack.getItem() instanceof ItemArmor ? ((ItemArmor) stack.getItem()).armorType : EntityEquipmentSlot.OFFHAND;
			armorStandPreview.setItemStackToSlot(slot, stack.copy());
		}
	}

	@Override
	protected void renderBg(Minecraft minecraft, int mouseX, int mouseY) {
		super.renderBg(minecraft, mouseX, mouseY);
		if (getContainer().isOpen()) {
			renderSlotBg(getContainer().getBaseSlot());
			renderSlotBg(getContainer().getAdditionalSlot());
			renderSlotBg(getContainer().getResultSlot());
		}
	}

	private void renderSlotBg(Slot slot) {
		GuiHelper.renderSlotsBackground(slot.xPos + screen.getGuiLeft() - 1, slot.yPos + screen.getGuiTop() - 1, 1, 1);
	}

	@Override
	protected void renderWidget(int mouseX, int mouseY, float partialTicks) {
		super.renderWidget(mouseX, mouseY, partialTicks);
		if (!isOpen) {
			return;
		}

		Slot resultSlot = getContainer().getResultSlot();
		int additionalSlotX = getContainer().getAdditionalSlot().xPos + screen.getGuiLeft();
		int resultSlotX = resultSlot.xPos + screen.getGuiLeft();
		int arrowX = additionalSlotX + 18 + (resultSlotX - (additionalSlotX + 18)) / 2 - ARROW.getWidth() / 2 - 1;
		int arrowY = resultSlot.yPos + screen.getGuiTop() + 1;
		GuiHelper.blit(arrowX, arrowY, ARROW);
		if (hasRecipeError()) {
			GuiHelper.blit(arrowX, arrowY, RED_CROSS);
		}

		renderArmorStand(x + getWidth() / 2, y + getHeight() - 6);
	}

	/** As vanilla draws the player in the inventory, but turned and tilted the same way always, as the smithing screen's stand. */
	private void renderArmorStand(int posX, int posY) {
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY, 50.0F);
		GlStateManager.scale(-ARMOR_STAND_SCALE, ARMOR_STAND_SCALE, ARMOR_STAND_SCALE);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(ARMOR_STAND_TILT, 1.0F, 0.0F, 0.0F);
		armorStandPreview.renderYawOffset = ARMOR_STAND_BODY_ROTATION;
		armorStandPreview.prevRenderYawOffset = ARMOR_STAND_BODY_ROTATION;
		armorStandPreview.rotationYaw = ARMOR_STAND_BODY_ROTATION;
		armorStandPreview.rotationYawHead = ARMOR_STAND_BODY_ROTATION;
		armorStandPreview.prevRotationYawHead = ARMOR_STAND_BODY_ROTATION;
		RenderManager renderManager = minecraft.getRenderManager();
		renderManager.setPlayerViewY(180.0F);
		renderManager.setRenderShadow(false);
		renderManager.renderEntity(armorStandPreview, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
		renderManager.setRenderShadow(true);
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	@Override
	protected void moveSlotsToTab() {
		Slot baseSlot = getContainer().getBaseSlot();
		baseSlot.xPos = x - screen.getGuiLeft() + 4;
		baseSlot.yPos = y - screen.getGuiTop() + 1 + 24;

		Slot additionalSlot = getContainer().getAdditionalSlot();
		additionalSlot.xPos = baseSlot.xPos + 18;
		additionalSlot.yPos = baseSlot.yPos;

		Slot resultSlot = getContainer().getResultSlot();
		resultSlot.xPos = x - screen.getGuiLeft() + getWidth() - 2 - 3 - 18;
		resultSlot.yPos = baseSlot.yPos;
	}

	private boolean hasRecipeError() {
		return getContainer().getBaseSlot().getHasStack() && getContainer().getAdditionalSlot().getHasStack() && !getContainer().getResultSlot().getHasStack();
	}
}
