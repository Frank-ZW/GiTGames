package net.gtminecraft.gitgames.proxy.command;

import net.gtminecraft.gitgames.compatability.mechanics.PlayerStatus;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.data.PlayerData;
import net.gtminecraft.gitgames.proxy.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public final class PlayCommand extends Command {

	private final CoreProxyPlugin plugin;

	public PlayCommand(CoreProxyPlugin plugin) {
		super("play");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer player)) {
			sender.sendMessage(StringUtil.PLAYERS_ONLY);
			return;
		}

		ServerInfo server = player.getServer().getInfo();
		if (this.plugin.getSettings().isMinigame(server.getName())) {
			sender.sendMessage(StringUtil.RUN_ON_INVALID_SERVER);
			return;
		}

		PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player);
		if (playerData == null) {
			player.sendMessage(StringUtil.ERROR_RETRIEVING_DATA);
			return;
		}

		switch (playerData.getStatus()) {
			case PLAYING -> sender.sendMessage(new TextComponent(ChatColor.RED + "You cannot run this command in a minigame."));
			case SPECTATING -> sender.sendMessage(new TextComponent(ChatColor.RED + "You cannot run this command while spectating."));
			default -> {
				if (args.length == 1) {
					String[] subargs = args[0].trim().toLowerCase().split("_");
					if (subargs.length != 2) {
						sender.sendMessage(new TextComponent(ChatColor.RED + "You must specify the name of the minigame and the total number of players."));
						return;
					}

					int maxPlayers;
					try {
						maxPlayers = Integer.parseInt(subargs[1]);
					} catch (NumberFormatException e) {
						sender.sendMessage(new TextComponent(ChatColor.RED + "The number of players entered must be a whole number."));
						return;
					}

					this.plugin.getServerManager().queuePlayer(playerData, subargs[0], maxPlayers);
				} else {
					sender.sendMessage(new TextComponent(ChatColor.RED + "To play a minigame, type /play <minigame name>_<number of players>."));
				}
			}
		}
	}
}
