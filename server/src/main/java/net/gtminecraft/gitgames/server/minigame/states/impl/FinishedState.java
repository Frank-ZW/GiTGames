package net.gtminecraft.gitgames.server.minigame.states;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.server.minigame.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.PlayableGameState;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class FinishedState extends PlayableGameState {

	public FinishedState(MinigameManager minigameManager) {
		super(minigameManager, GameStateUtils.FINISHED_STATE_PRIORITY);
	}

	@Override
	public void onEnable() {
		super.onEnable();	// Send the game update packet
		this.minigame.endTeleport();
		this.minigame.deleteWorlds(true);
		this.minigameManager.nextState();
	}

	@Override
	public void onDisable() {
		this.minigameManager.setMaxPlayers(0);
		this.minigameManager.setMinigame(null);
		this.minigameManager.clearSpectatorQueue();
	}

	@Override
	public AbstractGameState nextState() {
		return new InactiveState(this.minigameManager);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (this.minigame.isPlayer(e.getPlayer().getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (this.minigame.isPlayer(e.getEntity().getUniqueId())) {
			e.setCancelled(true);
		}
	}
}
