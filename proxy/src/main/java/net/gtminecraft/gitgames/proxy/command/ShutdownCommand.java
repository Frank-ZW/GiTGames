package net.gtminecraft.gitgames.proxy.command;

import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.command.impl.ProxyShutdownImpl;
import net.gtminecraft.gitgames.proxy.command.impl.ServerShutdownImpl;
import net.gtminecraft.gitgames.proxy.command.impl.ShutdownCancelImpl;
import net.gtminecraft.gitgames.proxy.command.types.AbstractDefaultShutdown;
import net.gtminecraft.gitgames.proxy.command.types.AbstractServerShutdown;
import net.gtminecraft.gitgames.proxy.command.types.AbstractShutdown;
import net.gtminecraft.gitgames.proxy.data.ServerData;
import net.gtminecraft.gitgames.proxy.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.util.Map;

public final class ShutdownCommand extends Command {

	private static final BaseComponent INVALID_RESPONSE = new TextComponent(ChatColor.RED + "This command does not exist. To shutdown the proxy or server, type /shutdown [proxy | server] [countdown]");

	private final CoreProxyPlugin plugin;
	private final Map<String, AbstractShutdown> subcommands;

	public ShutdownCommand(CoreProxyPlugin plugin) {
		super("shutdown", StringUtil.SHUTDOWN_COMMAND);
		this.plugin = plugin;
		this.subcommands = Map.of("cancel", new ShutdownCancelImpl(plugin), "server", new ServerShutdownImpl(plugin), "proxy", new ProxyShutdownImpl(plugin));
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!this.hasPermission(sender)) {
			sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
			return;
		}

		if (args.length == 0) {
			sender.sendMessage(ShutdownCommand.INVALID_RESPONSE);
			return;
		}

		switch (args[0].toLowerCase()) {
			case "cancel" -> ((AbstractDefaultShutdown) this.subcommands.get("cancel")).accept(sender, args);
			case "server" -> {
				ServerData serverData = this.plugin.getServerManager().getServerData(args[1]);
				if (serverData == null) {
					sender.sendMessage(new TextComponent(ChatColor.RED + "The specified server either does not exist or is not registered to the plugin's network."));
					return;
				}

				((AbstractServerShutdown) this.subcommands.get("server")).accept(sender, serverData, args);
			}
			case "proxy" -> ((AbstractDefaultShutdown) this.subcommands.get("proxy")).accept(sender, args);
//            case "status" -> {}
			default -> sender.sendMessage(ShutdownCommand.INVALID_RESPONSE);
		}
	}
}
