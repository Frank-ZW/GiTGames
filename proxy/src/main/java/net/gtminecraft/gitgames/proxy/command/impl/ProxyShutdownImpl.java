package net.gtminecraft.gitgames.proxy.command.impl;

import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.command.types.AbstractDefaultShutdown;
import net.gtminecraft.gitgames.proxy.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

public class ProxyShutdownImpl extends AbstractDefaultShutdown {

	public ProxyShutdownImpl(CoreProxyPlugin plugin) {
		super(plugin, StringUtil.SHUTDOWN_START_COMMAND);
	}

	@Override
	public void accept(@NotNull CommandSender sender, String[] args) {
		if (!this.hasPermission(sender)) {
			sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
			return;
		}

		switch (args.length) {
			case 1 -> this.plugin.scheduleShutdown(300);
			case 2 -> {
				int countdown;
				try {
					countdown = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					sender.sendMessage(new TextComponent(ChatColor.RED + "The countdown duration entered must be a positive, whole number."));
					return;
				}
				if (countdown < 1) {
					sender.sendMessage(new TextComponent(ChatColor.RED + "The countdown duration entered must be a number greater than zero."));
					return;
				}

				this.plugin.scheduleShutdown(countdown);
			}

			default -> sender.sendMessage(new TextComponent(ChatColor.RED + "This command does not exist."));
		}
	}
}