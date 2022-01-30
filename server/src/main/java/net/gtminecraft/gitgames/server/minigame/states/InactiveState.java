package net.gtminecraft.gitgames.server.minigame.states;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.server.minigame.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;

public class InactiveState extends AbstractGameState {

	public InactiveState(MinigameManager minigameManager) {
		super(minigameManager, GameStateUtils.INACTIVE_STATE_PRIORITY);
	}

	@Override
	public void onEnable() {
		super.onEnable();
	}

	@Override
	public void onDisable() {}

	@Override
	public AbstractGameState nextState() {
		return new QueuingState(this.minigameManager);
	}
}
