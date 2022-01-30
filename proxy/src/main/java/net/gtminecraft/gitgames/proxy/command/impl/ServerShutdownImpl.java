package net.gtminecraft.gitgames.proxy.command.impl;

import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.command.types.AbstractServerShutdown;
import net.gtminecraft.gitgames.proxy.data.ServerData;
import net.gtminecraft.gitgames.proxy.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public class ServerShutdownImpl extends AbstractServerShutdown {

	public ServerShutdownImpl(CoreProxyPlugin plugin) {
		super(plugin, StringUtil.SHUTDOWN_START_COMMAND);
	}

	@Override
	public void accept(@NotNull CommandSender sender, @NotNull ServerData serverData, String[] args) {
		if (!this.hasPermission(sender)) {
			sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
			return;
		}

		int countdown = switch (args.length) {
			case 2:
				yield 300;
			case 3:
				try {
					yield Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					sender.sendMessage(new TextComponent(ChatColor.RED + "The countdown duration entered must be a positive, whole number."));
					yield -1;
				}
			default:
				yield -1;
		};

		if (countdown < 1) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "The countdown duration must be a positive, whole number."));
			return;
		}

		serverData.scheduleShutdown(countdown);
		sender.sendMessage(new TextComponent(ChatColor.GREEN + "Started a new shutdown scheduler for " + serverData.getServer().getName() + ". If there was a pre-existing shutdown scheduled, it has been over-written."));
	}
}