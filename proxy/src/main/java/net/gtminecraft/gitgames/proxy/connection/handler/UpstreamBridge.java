package net.gtminecraft.gitgames.proxy.connection.handler;

import lombok.AllArgsConstructor;
import net.gtminecraft.gitgames.compatability.PacketHandler;
import net.gtminecraft.gitgames.compatability.exception.KeepAliveTimeoutException;
import net.gtminecraft.gitgames.compatability.mechanics.*;
import net.gtminecraft.gitgames.compatability.packet.*;
import net.gtminecraft.gitgames.compatability.wrapper.ChannelWrapper;
import net.gtminecraft.gitgames.compatability.wrapper.PacketWrapper;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.data.MinigameServerData;
import net.gtminecraft.gitgames.proxy.data.PlayerData;
import net.gtminecraft.gitgames.proxy.data.ServerData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@AllArgsConstructor
public class UpstreamBridge extends PacketHandler {

	private final CoreProxyPlugin plugin;
	private final ServerInfo server;
	private final ServerData serverData;
	private final ChannelWrapper wrapper;

	public void handle(PacketWrapper packetWrapper) {}

	public void handle(PacketKeepAlive packet) throws Exception {
		if (packet.getId() != this.serverData.getId()) {
			throw new KeepAliveTimeoutException("Received wrong keep alive response from the client");
		} else {
			this.serverData.setId(-1L);
		}
	}

	public void handle(PacketServerDisconnect packet) {
		if (packet.getAction() == PacketServerDisconnect.DisconnectAction.REQUEST_SHUTDOWN) {
			ServerInfo server = this.plugin.getSettings().fallbackServer(this.server.getName());
			if (server != null && !this.server.getPlayers().isEmpty()) {
				this.serverData.setReceivedDisconnectionRequest(true);
				this.serverData.pendingDisconnections(this.server.getPlayers());
				for (ProxiedPlayer player : this.server.getPlayers()) {
					if (!server.equals(player.getServer().getInfo())) {
						player.connect(server);
					}
				}
			} else {
				this.wrapper.close(new PacketServerDisconnect(PacketServerDisconnect.DisconnectAction.CONFIRM_SHUTDOWN));
			}
		}
	}

	@Override
	public void handle(PacketPlayerDisconnect packet) {
		this.plugin.getPlayerManager().removeDisconnection(packet.getPlayer());
	}

	@Override
	public void handle(PacketGameUpdate packet) throws Exception {
		MinigameServerData minigameServerData = (MinigameServerData) this.serverData;
		GameStatus status = GameStateUtils.gameStatusByPriority(packet.getPriority());
		if (status == null) {
			throw new Exception("Error retrieving game status with priority of " + packet.getPriority());
		}

		minigameServerData.setGameStatus(status);
		switch (packet.getPriority()) {
			case GameStateUtils.INACTIVE_STATE_PRIORITY:
				minigameServerData.setGameType(GameType.INACTIVE);
				break;
			case GameStateUtils.ACTIVE_STATE_PRIORITY:
				for (UUID uniqueId : packet.getPlayers()) {
					PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(uniqueId);
					if (playerData != null) {
						playerData.setStatus(PlayerStatus.PLAYING);
					}
				}

				break;
			case GameStateUtils.FINISHED_STATE_PRIORITY:
				ServerData serverData = this.plugin.getServerManager().getRandomServer(ServerType.LOBBY);
				for (UUID uniqueId : packet.getPlayers()) {
					PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(uniqueId);
					if (playerData != null) {
						if (serverData == null) {
							playerData.getPlayer().disconnect(new TextComponent(ChatColor.RED + "An error occurred while sending you back to the lobby. If this occurs multiple times, contact an administrator."));
							continue;
						}

						playerData.setStatus(PlayerStatus.INACTIVE);
						playerData.connect(serverData.getServer());
					}
				}

				this.plugin.getServerManager().getMinigamesManager().requeue(minigameServerData);
				break;
		}

		this.plugin.getLogger().info(ChatColor.GREEN + this.server.getName() + " now reached stage " + packet.getPriority());
	}

	@Override
	public void handle(PacketPlayerConnect packet) {
		if (packet.getServerType() == ServerType.LOBBY) {
			ServerData serverData = this.plugin.getServerManager().getRandomServer(ServerType.LOBBY);
			for (UUID uniqueId : packet.getPlayers()) {
				PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(uniqueId);
				if (playerData != null) {
					if (serverData == null) {
						playerData.getPlayer().sendMessage(new TextComponent(ChatColor.RED + "An error occurred while sending you back to the lobby. If this occurs multiple times, contact an administrator."));
						continue;
					}

					playerData.setStatus(packet.getPlayerStatus());
					playerData.connect(serverData.getServer());
				}
			}
		}
	}

	@Override
	public void handle(PacketPlayerDataUpdate packet) {
		for (UUID uniqueId : packet.getPlayers()) {
			PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(uniqueId);
			if (playerData != null) {
				playerData.setStatus(packet.getStatus());
			}
		}
	}
}
