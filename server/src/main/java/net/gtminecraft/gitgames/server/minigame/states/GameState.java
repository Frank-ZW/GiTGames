package net.gtminecraft.gitgames.server.minigame.states;

import net.gtminecraft.gitgames.compatability.packet.PacketGameUpdate;
import net.gtminecraft.gitgames.server.minigame.AbstractGame;

public abstract class GameState extends AbstractGameState {

	protected final AbstractGame game;

	public GameState(int priority) {
		super(priority);
		this.game = minigameManager.getGame();
	}

	@Override
	public void writeUpdate() {
		this.plugin.getConnectionManager().write(new PacketGameUpdate(this.priority, this.game.getPlayers()));
	}
}
