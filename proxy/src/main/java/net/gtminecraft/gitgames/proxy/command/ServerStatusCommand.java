package net.gtminecraft.gitgames.proxy.command;

import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;

import java.util.Map;
import java.util.Set;

public class ServerStatusCommand extends Command {

	private final CoreProxyPlugin plugin;
	private static final BaseComponent INVALID_RESPONSE = new TextComponent(ChatColor.RED + "This command does not exist. To view the status of every server on the network, type /serverstatus.");

	public ServerStatusCommand(CoreProxyPlugin plugin) {
		super("serverstatus", StringUtil.SERVER_STATUS_COMMAND);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!this.hasPermission(sender)) {
			sender.sendMessage(StringUtil.INSUFFICIENT_PERMISSION);
			return;
		}

		if (args.length != 0) {
			sender.sendMessage(ServerStatusCommand.INVALID_RESPONSE);
			return;
		}

		int index = 0;
		StringBuilder builder = new StringBuilder();
		Set<Map.Entry<String, ServerInfo>> entries = this.plugin.getProxy().getServersCopy().entrySet();
		for (Map.Entry<String, ServerInfo> entry : entries) {
			builder.append(ChatColor.YELLOW).append(entry.getKey()).append(this.plugin.getServerManager().isConnected(entry.getKey()) ? ChatColor.GREEN : ChatColor.RED).append(" ●");
			if (index++ != entries.size() - 1) {
				builder.append(ChatColor.YELLOW).append(", ");
			}
		}

		sender.sendMessage(new TextComponent(builder.toString()));
	}
}
