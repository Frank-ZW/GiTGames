package net.gtminecraft.gitgames.proxy.data;

import lombok.Getter;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.mechanics.*;
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
	private AbstractGameClassifier gameType;
	@Getter
	@Setter
	private GameStatus gameStatus;

	public MinigameServerData(CoreProxyPlugin plugin, ServerInfo server, ChannelWrapper channelWrapper, int serverId) {
		super(plugin, server, channelWrapper, serverId, ServerType.MINIGAME);
		this.maxPlayers = 0;
		this.gameType = GameClassifiers.INACTIVE_CLASSIFIER;
		this.gameStatus = GameStatus.WAITING;
	}
}
