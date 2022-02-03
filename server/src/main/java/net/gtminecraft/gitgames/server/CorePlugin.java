package net.gtminecraft.gitgames.server;

import lombok.Getter;
import net.gtminecraft.gitgames.server.config.ConfigSettings;
import net.gtminecraft.gitgames.server.connection.manager.ConnectionManager;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public final class CorePlugin extends JavaPlugin {

	@Getter
	private NamespacedKey key;
	@Getter
	private Chat chat;
	private Advancement netherAdvancement;
	private Advancement endAdvancement;
	@Getter
	private ConfigSettings settings;
	@Getter
	private ConnectionManager connectionManager;
	@Getter
	private MinigameManager minigameManager;
	@Getter
	private static CorePlugin instance;

	@Override
	public void onEnable() {
		instance = this;
		this.settings = new ConfigSettings(this);
		if (this.settings.isInitialStartup()) {
			Bukkit.getLogger().info(ChatColor.YELLOW + "Before this plugin can connect with the Proxy, shutdown the server and enter the name of the server as it appears in Bungee's config.yml file as well as the IP address and port. Restart the server once those changes have been made.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		this.key = new NamespacedKey(this, "game_data");
		Iterator<Advancement> iterator = Bukkit.advancementIterator();
		while (iterator.hasNext()) {
			Advancement advancement = iterator.next();
			if (advancement.getKey().getKey().equals("story/enter_the_nether")) {
				this.netherAdvancement = advancement;
			}

			if (advancement.getKey().getKey().equals("story/enter_the_end")) {
				this.endAdvancement = advancement;
			}

			if (this.netherAdvancement != null && this.endAdvancement != null) {
				break;
			}
		}

		RegisteredServiceProvider<Chat> provider = Bukkit.getServicesManager().getRegistration(Chat.class);
		if (provider != null) {
			this.chat = provider.getProvider();
		}

		this.settings.readSettings();
		this.connectionManager = new ConnectionManager(this);
		this.minigameManager = new MinigameManager(this);
		Bukkit.getPluginManager().registerEvents(this.minigameManager, this);
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		this.settings.saveSettings();
		this.minigameManager.disable();
		this.connectionManager.disable();
		instance = null;
	}

	public void grandAdvancement(@NotNull Player player, @NotNull Advancement advancement) {
		AdvancementProgress progress = player.getAdvancementProgress(advancement);
		for (String s : progress.getRemainingCriteria()) {
			progress.awardCriteria(s);
		}
	}

	public void grantNetherAdvancement(@NotNull Player player) {
		this.grandAdvancement(player, this.netherAdvancement);
	}

	public void grantEndAdvancement(@NotNull Player player) {
		this.grandAdvancement(player, this.endAdvancement);
	}
}
