package net.gtminecraft.gitgames.server.minigame.states.impl;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.compatability.mechanics.PlayerStatus;
import net.gtminecraft.gitgames.compatability.packet.PacketPlayerDataUpdate;
import net.gtminecraft.gitgames.server.minigame.states.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.states.GameState;
import net.gtminecraft.gitgames.server.runnable.CountdownRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CountdownState extends GameState {

	public CountdownState() {
		super(GameStateUtils.COUNTDOWN_STATE_PRIORITY);
	}

	@Override
	public void onEnable() {
		this.game.startCountdown(new CountdownRunnable(this.minigameManager, this.game.getNumPlayers() >= this.minigameManager.getMinPlayers() ? 10 : 20));
	}

	@Override
	public void onDisable() {}

	@Override
	public AbstractGameState nextState() {
		return new ActiveState();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (!this.game.getPlayers().remove(player.getUniqueId())) {
			return;
		}

		this.plugin.getConnectionManager().write(new PacketPlayerDataUpdate(PlayerStatus.INACTIVE, player.getUniqueId()));
		if (this.game.getNumPlayers() < this.minigameManager.getMinPlayers()) {
			this.game.cancelCountdown();
			this.minigameManager.setState(this.game.getNumPlayers() == 0 ? new FinishedState() : new QueuingState());
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		if (this.game.isPlayer(player.getUniqueId()) && this.game.getNumPlayers() >= (this.minigameManager.getMinPlayers() + this.minigameManager.getMaxPlayers()) / 2) {
			this.game.getCountdown().setCountdown(10);
		}
	}
}
