package net.gtminecraft.gitgames.server.runnable;

import net.gtminecraft.gitgames.server.minigame.AbstractMinigame;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class CountdownRunnable extends BukkitRunnable {

	private final AbstractMinigame minigame;
	private final MinigameManager minigameManager;
	private int countdown;
	private final List<Integer> timestamps = Arrays.asList(20, 15, 10, 5, 4, 3, 2, 1);

	public CountdownRunnable(MinigameManager minigameManager, int countdown) {
		this.minigameManager = minigameManager;
		this.minigame = minigameManager.getMinigame();
		this.countdown = countdown;
	}

	@Override
	public void run() {
		if (this.timestamps.contains(this.countdown)) {
			this.minigame.sendTitleWithEffect(Component.text(this.translateCountdown(this.countdown)), Effect.CLICK2);
		}

		if (this.countdown-- <= 0) {
			this.minigameManager.nextState();
			this.cancel();
		}
	}

	public void setCountdown(int countdown) {
		if (this.countdown < countdown) {
			this.countdown = countdown;
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
