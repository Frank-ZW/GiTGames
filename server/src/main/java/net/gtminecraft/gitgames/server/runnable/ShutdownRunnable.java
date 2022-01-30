package net.gtminecraft.gitgames.server.runnable;

import lombok.Getter;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class ShutdownRunnable extends BukkitRunnable {

	private final CorePlugin plugin;
	@Getter
	private int countdown;
	private static final List<Integer> timestamps = Arrays.asList(21600, 10800, 3600, 1800, 900, 600, 300, 180, 120, 60, 45, 30, 15, 10, 5, 4, 3, 2, 1);

	public ShutdownRunnable(CorePlugin plugin, int countdown) {
		this.plugin = plugin;
		this.countdown = countdown;
	}

	@Override
	public void run() {
		if (timestamps.contains(this.countdown)) {
			Bukkit.broadcast(Component.text(ChatColor.YELLOW + "The server will shutdown in " + ChatColor.LIGHT_PURPLE + this.countdown + " second" + (this.countdown == 1 ? "" : "s") + ChatColor.YELLOW + "."));
		}

		if (this.countdown-- <= 0) {
			Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(ChatColor.RED + "The server has shutdown!"));
			Bukkit.getScheduler().callSyncMethod(this.plugin, () -> {
				Bukkit.getServer().shutdown();
				return null;
			});

			this.cancel();
		}
	}
}
