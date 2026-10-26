package dev.feldsher.deconstructedbackpacks.command;

import dev.feldsher.deconstructedbackpacks.backpack.AccessLogRecord;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackAccessLogger;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackItem;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.BackpackWrapper;
import dev.feldsher.deconstructedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** {@code give <players> <backpackUuid>}: a backpack with the contents of one opened before, looking as it did then. */
public class GiveCommand extends CommandBase {
	@Override
	public String getName() {
		return "give";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return DbpCommand.OP_LEVEL;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.deconstructedbackpacks.give.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 2) {
			throw new WrongUsageException(getUsage(sender));
		}
		List<EntityPlayerMP> players = getPlayers(server, sender, args[0]);
		UUID backpackUuid;
		try {
			backpackUuid = UUID.fromString(args[1]);
		} catch (IllegalArgumentException e) {
			throw new CommandException("commands.deconstructedbackpacks.give.not_found", args[1]);
		}
		AccessLogRecord alr = BackpackAccessLogger.getBackpackLog(backpackUuid)
				.orElseThrow(() -> new CommandException("commands.deconstructedbackpacks.give.not_found", args[1]));
		Item item = ForgeRegistries.ITEMS.getValue(alr.getBackpackItemRegistryName());
		if (!(item instanceof BackpackItem)) {
			throw new CommandException("commands.deconstructedbackpacks.give.not_found", args[1]);
		}

		ItemStack backpack = new ItemStack(item);
		if (!backpack.getDisplayName().equals(alr.getBackpackName())) {
			backpack.setStackDisplayName(alr.getBackpackName());
		}
		IBackpackWrapper backpackWrapper = BackpackWrapper.fromStack(backpack);
		backpackWrapper.setColors(alr.getClothColor(), alr.getTrimColor());
		backpackWrapper.setColumnsTaken(alr.getColumnsTaken(), false);
		backpackWrapper.setContentsUuid(backpackUuid);

		players.forEach(player -> ItemHandlerHelper.giveItemToPlayer(player, backpack.copy()));

		if (players.size() == 1) {
			notifyCommandListener(sender, this, "commands.deconstructedbackpacks.give.success", players.get(0).getDisplayName());
		} else {
			notifyCommandListener(sender, this, "commands.deconstructedbackpacks.give.success", players.size());
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		}
		if (args.length == 2) {
			return getListOfStringsMatchingLastWord(args, BackpackAccessLogger.getBackpackUuids().stream().map(UUID::toString).collect(Collectors.toList()));
		}
		return Collections.emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 0;
	}
}
