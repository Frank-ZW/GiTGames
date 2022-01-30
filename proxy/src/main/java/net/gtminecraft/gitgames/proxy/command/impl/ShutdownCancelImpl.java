package net.gtminecraft.gitgames.proxy.command.impl;

import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.command.types.AbstractDefaultShutdown;
import net.gtminecraft.gitgames.proxy.data.ServerData;
import net.gtminecraft.gitgames.proxy.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public class ShutdownCancelImpl extends AbstractDefaultShutdown {

	public ShutdownCancelImpl(CoreProxyPlugin plugin) {
		super(plugin, StringUtil.SHUTDOWN_CANCEL_COMMAND);
	}

	@Override
	public void accept(@NotNull CommandSender sender, String[] args) {
		if (!this.hasPermission(sender)) {
			sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
			return;
		}

		switch (args.length) {
			case 1 -> {
				this.plugin.cancelShutdown(sender.getName());
				this.plugin.getServerManager().acceptToAll(serverData -> serverData.cancelShutdown(sender.getName()));
				sender.sendMessage(new TextComponent(ChatColor.GREEN + "All scheduled shutdown tasks have been cancelled."));
			}
			case 2 -> {
				if ("proxy".equalsIgnoreCase(args[1])) {
					sender.sendMessage(new TextComponent(ChatColor.GREEN + "If there was a shutdown scheduled for the network, it has been cancelled."));
				} else {
					ServerData serverData = this.plugin.getServerManager().getServerData(args[1]);
					if (serverData == null) {
						sender.sendMessage(new TextComponent(ChatColor.RED + "A server with the specified name does not exist."));
						return;
					}

					serverData.cancelShutdown(sender.getName());
					sender.sendMessage(new TextComponent(ChatColor.GREEN + "If there was a shutdown scheduled for " + serverData.getServer().getName() + ", it has been cancelled."));
				}
			}
			default -> sender.sendMessage(new TextComponent(ChatColor.RED + "This command does not exist. To cancel a pre-existing shutdown task, type /shutdown cancel [proxy | server name]"));
		}
	}
}