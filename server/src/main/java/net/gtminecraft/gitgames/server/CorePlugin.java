package net.gtminecraft.gitgames.server;

import lombok.Getter;
import net.gtminecraft.gitgames.server.config.ConfigSettings;
import net.gtminecraft.gitgames.server.connection.manager.ConnectionManager;
import net.gtminecraft.gitgames.server.map.manager.MapLoaderManager;
import net.gtminecraft.gitgames.server.minigame.manager.MinigameManager;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;
import java.util.Random;

public final class CorePlugin extends JavaPlugin {

	@Getter
	private final Random random = new Random();
	@Getter
	private NamespacedKey key;
	@Getter
	private Chat chat;
	@Getter
	private Advancement netherAdvancement;
	@Getter
	private Advancement endAdvancement;
	@Getter
	private ConfigSettings settings;
	@Getter
	private ConnectionManager connectionManager;
	@Getter
	private MinigameManager minigameManager;
	@Getter
	private MapLoaderManager mapLoaderManager;
	@Getter
	private static CorePlugin instance;

	@Override
	public void onEnable() {
		instance = this;
		this.settings = new ConfigSettings(this);
		if (this.settings.isInitialStartup()) {
			Bukkit.getLogger().warning("Before this plugin can connect with the Proxy, shutdown the server and enter the name of the server as it appears in Bungee's config.yml file as well as the IP address and port. Restart the server once those changes have been made.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		this.mapLoaderManager = new MapLoaderManager(this);
		if (!this.mapLoaderManager.isSuccess()) {
			Bukkit.getLogger().warning("Failed to load in map data from the configuration file. The plugin will automatically disable.");
			return;
		}

		this.key = new NamespacedKey(this, "game_data");
		this.loadAdvancements();

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
		if (this.minigameManager != null) {
			this.minigameManager.disable();
		}

		if (this.connectionManager != null) {
			this.connectionManager.disable();
		}

		instance = null;
	}

	private void loadAdvancements() {
		Iterator<Advancement> iterator = Bukkit.advancementIterator();
		while (iterator.hasNext()) {
			Advancement advancement = iterator.next();
			if (advancement.getKey().getNamespace().equals(NamespacedKey.MINECRAFT)) {
				if (advancement.getKey().getKey().equals("story/enter_the_nether")) {
					this.netherAdvancement = advancement;
				} else if (advancement.getKey().getKey().equals("story/enter_the_end")) {
					this.endAdvancement = advancement;
				}
			}

			if (this.netherAdvancement != null && this.endAdvancement != null) {
				break;
			}
		}
	}
}
