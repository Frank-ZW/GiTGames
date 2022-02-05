package net.gtminecraft.gitgames.proxy.command;

import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class ForceEndCommand extends Command {

	private final CoreProxyPlugin plugin;

	public ForceEndCommand(CoreProxyPlugin plugin) {
		super("forceend", StringUtil.FORCE_END_COMMAND);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!this.hasPermission(sender)) {
			sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
			return;
		}

		if (args.length != 1) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "To forcefully end an ongoing game, type /forceend <game key> or /forceend <server name>"));
			return;
		}

		try {
			int gameKey = Integer.parseInt(args[0]);
			if (this.plugin.getServerManager().getMinigamesManager().forceEnd(gameKey)) {
				sender.sendMessage(new TextComponent(ChatColor.GREEN + "A game with a game key of " + gameKey + " has been forcefully ended."));
			} else {
				sender.sendMessage(new TextComponent(ChatColor.RED + "There are no servers hosting an active game with a game key of " + gameKey + "."));
			}
		} catch (NumberFormatException e) {
			if (this.plugin.getServerManager().getMinigamesManager().forceEnd(args[0])) {
				sender.sendMessage(new TextComponent(ChatColor.GREEN + "If there was an active game running on " + args[0].toLowerCase() + ", it has been forcefully ended."));
			} else {
				sender.sendMessage(new TextComponent(ChatColor.RED + "Failed to find a server registered to the network with that name."));
			}
		}
	}
}
