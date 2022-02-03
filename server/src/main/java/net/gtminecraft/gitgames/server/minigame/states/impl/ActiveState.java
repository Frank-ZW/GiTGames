package net.gtminecraft.gitgames.server.minigame.states.impl;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import net.gtminecraft.gitgames.server.event.MinigameEndEvent;
import net.gtminecraft.gitgames.server.minigame.states.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.states.PlayableGameState;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;

public class ActiveState extends PlayableGameState {

	public ActiveState(MinigameManager minigameManager) {
		super(minigameManager, GameStateUtils.ACTIVE_STATE_PRIORITY);
	}

	@Override
	public void onEnable() {
		super.onEnable();	// Send the update packet
		Bukkit.getPluginManager().registerEvents(this.minigame, this.plugin);
		this.minigame.startTeleport();
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this.minigame);
	}

	@Override
	public AbstractGameState nextState() {
		return new FinishedState(this.minigameManager);
	}

	@EventHandler
	public void onMinigameEnd(MinigameEndEvent e) {
		Bukkit.getScheduler().runTaskLater(this.plugin, this.minigameManager::nextState, e.isUrgent() ? 0 : 200);
	}
}
