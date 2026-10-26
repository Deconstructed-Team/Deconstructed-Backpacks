package dev.feldsher.deconstructedbackpacks.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.command.CommandTreeBase;

import java.util.Collections;
import java.util.List;

/** {@code /dbp} (or {@code /deconstructedbackpacks}): finding backpacks by who opened them, and giving them back. */
public class DbpCommand extends CommandTreeBase {
	static final int OP_LEVEL = 2;

	public DbpCommand() {
		addSubcommand(new ListCommand());
		addSubcommand(new GiveCommand());
		addSubcommand(new RemoveNonPlayerCommand());
	}

	@Override
	public String getName() {
		return "dbp";
	}

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("deconstructedbackpacks");
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.deconstructedbackpacks.usage";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return OP_LEVEL;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender.canUseCommand(getRequiredPermissionLevel(), getName());
	}
}
