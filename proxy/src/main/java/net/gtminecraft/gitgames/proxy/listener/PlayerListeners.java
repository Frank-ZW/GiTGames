package net.gtminecraft.gitgames.proxy.listener;

import lombok.AllArgsConstructor;
import net.gtminecraft.gitgames.compatability.Protocol;
import net.gtminecraft.gitgames.compatability.mechanics.PlayerStatus;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.data.PlayerData;
import net.gtminecraft.gitgames.proxy.data.ServerData;
import net.gtminecraft.gitgames.proxy.util.StringUtil;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

@AllArgsConstructor
public class PlayerListeners implements Listener {

	private final CoreProxyPlugin plugin;

	@EventHandler
	public void onPlayerLogin(PostLoginEvent e) {
		this.plugin.getPlayerManager().addPlayer(e.getPlayer());
	}

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent e) {
		this.plugin.getPlayerManager().removePlayer(e.getPlayer());
	}

	@EventHandler
	public void onServerSwitch(ServerSwitchEvent e) {
		ServerData serverData = this.plugin.getServerManager().getServerData(e.getFrom());
		if (serverData != null) {
			serverData.handlePendingDisconnection(e.getPlayer());
		}
	}

	@EventHandler
	public void onServerConnect(ServerConnectEvent e) {
		if (this.plugin.getSettings().isMinigame(e.getTarget().getName())) {
			return;
		}

		ProxiedPlayer player = e.getPlayer();
		ServerInfo server = this.plugin.getPlayerManager().removeDisconnection(player.getUniqueId());
		PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player);
		if (playerData != null && server != null) {
			e.setTarget(server);
			playerData.setStatus(PlayerStatus.PLAYING);
		}
	}
}
