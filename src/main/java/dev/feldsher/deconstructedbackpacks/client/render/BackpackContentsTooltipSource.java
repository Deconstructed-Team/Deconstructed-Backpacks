package dev.feldsher.deconstructedbackpacks.client.render;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackStorage;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.network.PacketHandler;
import dev.feldsher.deconstructedbackpacks.network.RequestBackpackContentsMessage;
import dev.feldsher.deconstructedcore.api.IStorageWrapper;
import dev.feldsher.deconstructedcore.client.render.StorageContentsTooltip;
import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

/** A backpack's tooltip shows what it holds, with shift held. */
public class BackpackContentsTooltipSource implements StorageContentsTooltip.IContentsTooltipSource {
	@Override
	public Optional<IStorageWrapper> getStorageWrapper(ItemStack stack) {
		return stack.getItem() instanceof BackpackItem ? Optional.of(BackpackWrapper.fromStack(stack)) : Optional.empty();
	}

	@Override
	public boolean hasClientContents(UUID uuid) {
		return BackpackStorage.hasClientBackpackContents(uuid);
	}

	@Override
	public void requestContents(UUID uuid) {
		PacketHandler.INSTANCE.sendToServer(new RequestBackpackContentsMessage(uuid));
	}
}
