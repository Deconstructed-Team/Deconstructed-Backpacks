package dev.feldsher.deconstructedbackpacks.network;

import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.util.PlayerInventoryProvider;
import dev.feldsher.deconstructedcore.upgrades.IUpgradeWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Map;

/** A toggle upgrade key: the upgrade in that slot of the first carried backpack is switched on or off. */
public class UpgradeToggleMessage implements IMessage {
	private int upgradeSlot;

	@SuppressWarnings("unused") // instantiated by SimpleNetworkWrapper
	public UpgradeToggleMessage() {
	}

	public UpgradeToggleMessage(int upgradeSlot) {
		this.upgradeSlot = upgradeSlot;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		upgradeSlot = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(upgradeSlot);
	}

	public static class Handler implements IMessageHandler<UpgradeToggleMessage, IMessage> {
		@Override
		public IMessage onMessage(UpgradeToggleMessage message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> PlayerInventoryProvider.runOnBackpacks(player, (backpack, slot) -> {
				Map<Integer, IUpgradeWrapper> slotWrappers = BackpackWrapper.fromStack(backpack).getUpgradeHandler().getSlotWrappers();
				IUpgradeWrapper upgradeWrapper = slotWrappers.get(message.upgradeSlot);
				if (upgradeWrapper != null && upgradeWrapper.canBeDisabled()) {
					upgradeWrapper.setEnabled(!upgradeWrapper.isEnabled());
					String translKey = upgradeWrapper.isEnabled() ? "gui.deconstructedbackpacks.status.upgrade_switched_on" : "gui.deconstructedbackpacks.status.upgrade_switched_off";
					player.sendStatusMessage(new TextComponentTranslation(translKey, upgradeWrapper.getUpgradeStack().getDisplayName()), true);
				}
				return true;
			}));
			return null;
		}
	}
}
