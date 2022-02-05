package net.gtminecraft.gitgames.server.minigame.states.impl;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.compatability.mechanics.PlayerStatus;
import net.gtminecraft.gitgames.compatability.packet.PacketPlayerDataUpdate;
import net.gtminecraft.gitgames.server.minigame.states.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.states.GameState;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class CountdownState extends GameState {

	public CountdownState() {
		super(GameStateUtils.COUNTDOWN_STATE_PRIORITY);
	}

	@Override
	public void onEnable() {
		this.minigame.startCountdown(new CountdownRunnable());
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
		if (!this.minigame.getPlayers().remove(player.getUniqueId())) {
			return;
		}

		this.plugin.getConnectionManager().write(new PacketPlayerDataUpdate(PlayerStatus.INACTIVE, player.getUniqueId()));
		if (this.minigame.getNumPlayers() < this.minigameManager.getMaxPlayers()) {
			this.minigame.cancelCountdown();
			this.minigameManager.setState(this.minigame.getNumPlayers() == 0 ? new FinishedState() : new QueuingState());
		}
	}

	private final class CountdownRunnable extends BukkitRunnable {

		private int countdown = 15;
		private final List<Integer> timestamps = Arrays.asList(15, 10, 5, 4, 3, 2, 1);

		@Override
		public void run() {
			if (this.timestamps.contains(this.countdown)) {
				minigame.sendTitleWithEffect(Component.text(this.translateCountdown(this.countdown)), Effect.CLICK2);
			}

			if (this.countdown-- <= 0) {
				minigameManager.nextState();
				this.cancel();
			}
		}

		private String translateCountdown(int countdown) {
			return switch (countdown) {
				case 1, 2 -> ChatColor.RED.toString() + countdown;
				case 3, 4 -> ChatColor.GOLD.toString() + countdown;
				default -> ChatColor.GREEN.toString() + countdown;
			};
		}
	}
}
