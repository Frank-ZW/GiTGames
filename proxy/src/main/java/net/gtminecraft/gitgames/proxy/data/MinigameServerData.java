package net.gtminecraft.gitgames.proxy.data;

import lombok.Getter;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.mechanics.GameStatus;
import net.gtminecraft.gitgames.compatability.mechanics.GameType;
import net.gtminecraft.gitgames.compatability.mechanics.ServerType;
import net.gtminecraft.gitgames.compatability.wrapper.ChannelWrapper;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.md_5.bungee.api.config.ServerInfo;

public class MinigameServerData extends ServerData {

	@Getter
	@Setter
	private int gameKey;
	@Getter
	@Setter
	private int maxPlayers;
	@Getter
	@Setter
	private GameType gameType;
	@Getter
	@Setter
	private GameStatus gameStatus;

	public MinigameServerData(CoreProxyPlugin plugin, ServerInfo server, ChannelWrapper channelWrapper, int serverId) {
		super(plugin, server, channelWrapper, serverId, ServerType.MINIGAME);
		this.maxPlayers = 0;
		this.gameType = GameType.INACTIVE;
		this.gameStatus = GameStatus.WAITING;
	}
}
