package net.gtminecraft.gitgames.proxy.data.manager;

import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.data.PlayerData;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

	private CoreProxyPlugin plugin;
	private final Map<UUID, PlayerData> players = new HashMap<>();
	private final Map<UUID, ServerInfo> disconnections = new HashMap<>();

	public PlayerManager(CoreProxyPlugin plugin) {
		this.plugin = plugin;
	}

	public void disable() {
		this.plugin.getProxy().getPlayers().forEach(this::removePlayer);
		this.disconnections.clear();
		this.plugin = null;
	}

	public void removeDisconnections(Collection<UUID> players) {
		this.disconnections.keySet().removeAll(players);
	}

	public void removeDisconnections(ServerInfo server) {
		this.disconnections.values().remove(server);
	}

	@Nullable
	public ServerInfo removeDisconnection(UUID uniqueId) {
		return this.disconnections.remove(uniqueId);
	}

	public void addPlayer(ProxiedPlayer player) {
		this.players.put(player.getUniqueId(), new PlayerData(player));
	}

	public void removePlayer(ProxiedPlayer player) {
		PlayerData playerData = this.players.remove(player.getUniqueId());
		if (playerData != null && playerData.getStatus().isPlaying()) {
			this.disconnections.put(player.getUniqueId(), player.getServer().getInfo());
		}
	}

	@Nullable
	public PlayerData getPlayerData(ProxiedPlayer player) {
		return this.getPlayerData(player.getUniqueId());
	}

	@Nullable
	public PlayerData getPlayerData(UUID uniqueId) {
		return this.players.get(uniqueId);
	}
}
