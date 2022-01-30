package net.gtminecraft.gitgames.server.minigame.states;

import net.gtminecraft.gitgames.compatability.mechanics.GameStateUtils;
import net.gtminecraft.gitgames.server.minigame.AbstractGameState;
import net.gtminecraft.gitgames.server.minigame.GameState;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import net.gtminecraft.gitgames.service.event.MinigameEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class CountdownState extends GameState {

	public CountdownState(MinigameManager minigameManager) {
		super(minigameManager, GameStateUtils.COUNTDOWN_STATE_PRIORITY);
	}

	@Override
	public void onEnable() {
		this.minigame.onPreCountdown();
		new CountdownRunnable().runTaskTimer(this.plugin, 0, 20L);
	}

	@Override
	public void onDisable() {}

	@Override
	public AbstractGameState nextState() {
		return new ActiveState(this.minigameManager);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (this.minigame.isPlayer(player.getUniqueId())) {
			this.minigame.cancelCountdown();
			this.minigameManager.setState(new QueuingState(this.minigameManager));
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
				Bukkit.getPluginManager().callEvent(new MinigameEvent(minigame, MinigameEvent.Action.START));
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
