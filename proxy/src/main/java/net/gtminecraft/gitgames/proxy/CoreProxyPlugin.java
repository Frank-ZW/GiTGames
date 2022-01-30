package net.gtminecraft.gitgames.proxy;

import lombok.Getter;
import net.gtminecraft.gitgames.proxy.command.PlayCommand;
import net.gtminecraft.gitgames.proxy.command.ServerStatusCommand;
import net.gtminecraft.gitgames.proxy.command.ShutdownCommand;
import net.gtminecraft.gitgames.proxy.config.ConfigSettings;
import net.gtminecraft.gitgames.proxy.data.manager.PlayerManager;
import net.gtminecraft.gitgames.proxy.listener.PlayerListeners;
import net.gtminecraft.gitgames.proxy.runnable.NetworkShutdownRunnable;
import net.gtminecraft.gitgames.proxy.data.manager.ServerManager;
import net.gtminecraft.gitgames.proxy.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class CoreProxyPlugin extends Plugin {

	private final List<Command> commands = Arrays.asList(new ServerStatusCommand(this), new ShutdownCommand(this), new PlayCommand(this));

	@Getter
	private PlayerManager playerManager;
	@Getter
	private ServerManager serverManager;
	@Getter
	private ConfigSettings settings;
	private ScheduledTask shutdown;
	private Runnable runnable;
	@Getter
	private static CoreProxyPlugin instance;

	@Override
	public void onEnable() {
		instance = this;
		if (!this.getDataFolder().exists() && !this.getDataFolder().mkdir()) {
			this.getProxy().getLogger().log(Level.SEVERE, "Unable to create plugin main directory");
			this.onDisable();
			return;
		}

		this.settings = new ConfigSettings(this);
		if (!this.settings.isConnected()) {
			this.onDisable();
			return;
		}

		for (Command command : this.commands) {
			this.getProxy().getPluginManager().registerCommand(this, command);
		}

		this.getProxy().getPluginManager().registerListener(this, new PlayerListeners(this));
		this.serverManager = new ServerManager(this);
		this.playerManager = new PlayerManager(this);
	}

	@Override
	public void onDisable() {
		this.getProxy().getPluginManager().unregisterListeners(this);
		this.getProxy().getPluginManager().unregisterCommands(this);
		this.playerManager.disable();
		this.serverManager.disable();
		instance = null;
	}

	public void cancelShutdownNow() {
		if (this.shutdown != null && this.runnable != null) {
			this.shutdown.cancel();
			this.shutdown = null;
			this.runnable = null;
		}
	}

	public void cancelShutdown(String who) {
		if (this.shutdown != null) {
			this.shutdown.cancel();
			this.runnable = null;
			this.shutdown = null;
			this.getProxy().broadcast(StringUtil.EMPTY_STRING);
			this.getProxy().broadcast(new TextComponent(ChatColor.YELLOW + "The network shutdown has been cancelled by " + ChatColor.LIGHT_PURPLE + who + ChatColor.YELLOW + "."));
			this.getProxy().broadcast(StringUtil.EMPTY_STRING);
		}
	}

	public void scheduleShutdown(int duration) {
		this.getProxy().broadcast(StringUtil.EMPTY_STRING);
		if (this.shutdown != null) {
			this.shutdown.cancel();
			this.getProxy().broadcast(new TextComponent(ChatColor.YELLOW + "The network shutdown has been rescheduled to " + ChatColor.LIGHT_PURPLE + duration + " second" + (duration == 1 ? "" : "s") + ChatColor.YELLOW + "."));
		} else {
			this.getProxy().broadcast(new TextComponent(ChatColor.YELLOW + "The network has been scheduled in " + ChatColor.LIGHT_PURPLE + duration + " second" + (duration == 1 ? "" : "s") + ChatColor.YELLOW + "."));
		}

		this.getProxy().broadcast(StringUtil.EMPTY_STRING);
		this.runnable = new NetworkShutdownRunnable(this, duration);
		this.shutdown = this.getProxy().getScheduler().schedule(this, this.runnable, 1L, 1L, TimeUnit.SECONDS);
	}
}
