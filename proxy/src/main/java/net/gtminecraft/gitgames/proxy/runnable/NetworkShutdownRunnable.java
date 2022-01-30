package net.gtminecraft.gitgames.proxy.runnable;

import lombok.Getter;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Arrays;
import java.util.List;

public class NetworkShutdownRunnable implements Runnable {

	private final CoreProxyPlugin plugin;
	private final ProxyServer proxy;
	@Getter
	private int countdown;
	private static final List<Integer> timestamps = Arrays.asList(21600, 10800, 3600, 1800, 900, 600, 300, 180, 120, 60, 45, 30, 15, 10, 5, 4, 3, 2, 1);

	public NetworkShutdownRunnable(CoreProxyPlugin plugin, int countdown) {
		this.plugin = plugin;
		this.proxy = plugin.getProxy();
		this.countdown = countdown;
	}

	@Override
	public void run() {
		if (timestamps.contains(this.countdown)) {
			this.proxy.broadcast(new TextComponent(ChatColor.YELLOW + "The network will shutdown in " + ChatColor.LIGHT_PURPLE + this.countdown + " second" + (this.countdown == 1 ? "" : "s") + ChatColor.YELLOW + "."));
		}

		if (this.countdown-- <= 0) {
			this.proxy.stop(ChatColor.RED + "The network has been shutdown!");
			this.plugin.cancelShutdownNow();
		}
	}
}
