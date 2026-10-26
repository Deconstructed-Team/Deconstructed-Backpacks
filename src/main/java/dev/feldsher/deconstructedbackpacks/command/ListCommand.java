package dev.feldsher.deconstructedbackpacks.command;

import dev.feldsher.deconstructedbackpacks.backpack.AccessLogRecord;
import dev.feldsher.deconstructedbackpacks.backpack.BackpackAccessLogger;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/** {@code list [player]}: the backpacks opened, by anyone or by the player, the last opened first. */
public class ListCommand extends CommandBase {
	// the chat's colors as the client draws them, in TextFormatting's order: 1.12 chat has no other colors
	private static final int[] FORMATTING_COLORS = {0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
			0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF};

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return DbpCommand.OP_LEVEL;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.deconstructedbackpacks.list.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException {
		if (args.length > 1) {
			throw new WrongUsageException(getUsage(sender));
		}
		List<AccessLogRecord> logs = new ArrayList<>(args.length == 0 ? BackpackAccessLogger.getAllBackpackLogs() : BackpackAccessLogger.getBackpackLogsForPlayer(args[0]));
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		logs.sort(Comparator.comparing(AccessLogRecord::getAccessTime).reversed());
		sender.sendMessage(new TextComponentTranslation("commands.deconstructedbackpacks.list.header"));
		logs.forEach(alr -> {
			ITextComponent message = new TextComponentString("");
			message.appendSibling(new TextComponentString(alr.getBackpackName()).setStyle(new Style().setColor(TextFormatting.GREEN)
					.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/dbp give @p " + alr.getBackpackUuid()))
					.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("chat.deconstructedbackpacks.backpack_uuid.tooltip", alr.getBackpackUuid().toString())))));
			message.appendText(", ");
			message.appendSibling(colored(new TextComponentTranslation("commands.deconstructedbackpacks.list.cloth_color"), alr.getClothColor()));
			message.appendText(" ");
			message.appendSibling(colored(new TextComponentTranslation("commands.deconstructedbackpacks.list.trim_color"), alr.getTrimColor()));
			message.appendText(", " + alr.getPlayerName() + ", " + dateFormat.format(new Date(alr.getAccessTime())));
			sender.sendMessage(message);
		});
	}

	/** In the chat color nearest to the backpack's, with the color itself on hover. */
	private static ITextComponent colored(ITextComponent component, int color) {
		return component.setStyle(new Style().setColor(getNearestFormatting(color))
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(String.format("#%06X", color & 0xFFFFFF)))));
	}

	private static TextFormatting getNearestFormatting(int color) {
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;
		int nearest = 0;
		int nearestDistance = Integer.MAX_VALUE;
		for (int i = 0; i < FORMATTING_COLORS.length; i++) {
			int dr = r - (FORMATTING_COLORS[i] >> 16 & 0xFF);
			int dg = g - (FORMATTING_COLORS[i] >> 8 & 0xFF);
			int db = b - (FORMATTING_COLORS[i] & 0xFF);
			int distance = dr * dr + dg * dg + db * db;
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearest = i;
			}
		}
		return TextFormatting.fromColorIndex(nearest);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, BackpackAccessLogger.getPlayerNames()) : Collections.emptyList();
	}
}
