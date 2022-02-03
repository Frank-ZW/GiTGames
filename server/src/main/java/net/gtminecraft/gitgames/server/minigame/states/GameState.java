package net.gtminecraft.gitgames.server.minigame;

import net.gtminecraft.gitgames.compatability.packet.PacketGameUpdate;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import net.gtminecraft.gitgames.service.mechanics.AbstractMinigame;

public abstract class GameState extends AbstractGameState {

	protected final AbstractMinigame minigame;

	public GameState(MinigameManager minigameManager, int priority) {
		super(minigameManager, priority);
		this.minigame = minigameManager.getMinigame();
	}

	@Override
	public void writeUpdate() {
		this.plugin.getConnectionManager().write(new PacketGameUpdate(this.priority, this.minigame.getPlayers()));
	}
}
