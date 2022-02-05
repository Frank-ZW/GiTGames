package net.gtminecraft.gitgames.server.minigame.states.impl;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.server.minigame.states.AbstractGameState;

public class InactiveState extends AbstractGameState {

	public InactiveState() {
		super(GameStateUtils.INACTIVE_STATE_PRIORITY);
	}

	@Override
	public void onEnable() {
		super.onEnable();
	}

	@Override
	public void onDisable() {}

	@Override
	public AbstractGameState nextState() {
		return new QueuingState();
	}
}
