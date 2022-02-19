package net.gtminecraft.gitgames.server.minigame.states.impl;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.server.minigame.states.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.states.PlayableGameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class FinishedState extends PlayableGameState {

	public FinishedState() {
		super(GameStateUtils.FINISHED_STATE_PRIORITY);
	}

	@Override
	public void onEnable() {
		super.onEnable();	// Send the game update packet
		this.game.cancelDisconnections();
		this.game.endTeleport();
		this.game.deleteWorlds(true);
		this.minigameManager.nextState();
	}

	@Override
	public void onDisable() {
		this.minigameManager.unload();
	}

	@Override
	public AbstractGameState nextState() {
		return new InactiveState();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (this.game.isPlayer(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (this.game.isPlayer(e.getEntity().getUniqueId())) {
			e.setCancelled(true);
		}
	}
}
