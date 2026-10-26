package dev.feldsher.deconstructedbackpacks.command;

import dev.feldsher.deconstructedbackpacks.backpack.BackpackStorage;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/** {@code removeNonPlayer <onlyWithEmptyInventory>}: removes the contents of backpacks no player ever opened. */
public class RemoveNonPlayerCommand extends CommandBase {
	@Override
	public String getName() {
		return "removeNonPlayer";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return DbpCommand.OP_LEVEL;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.deconstructedbackpacks.remove_non_player.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 1) {
			throw new WrongUsageException(getUsage(sender));
		}
		int numberRemoved = BackpackStorage.removeNonPlayerBackpackContents(parseBoolean(args[0]));
		sender.sendMessage(new TextComponentTranslation("commands.deconstructedbackpacks.remove_non_player.success", numberRemoved));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, "true", "false") : Collections.emptyList();
	}
}
