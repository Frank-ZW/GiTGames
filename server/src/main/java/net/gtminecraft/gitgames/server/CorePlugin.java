package net.gtminecraft.gitgames.server;

import lombok.Getter;
import net.gtminecraft.gitgames.server.config.ConfigSettings;
import net.gtminecraft.gitgames.server.connection.manager.ConnectionManager;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import net.gtminecraft.gitgames.service.AbstractCorePlugin;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Iterator;

public final class CorePlugin extends AbstractCorePlugin {

	@Getter
	private ConfigSettings settings;
	@Getter
	private MinigameManager minigameManager;
	@Getter
	private ConnectionManager connectionManager;

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
}
